package com.bird.cos.service.auth;

import com.bird.cos.domain.auth.EmailVerification;
import com.bird.cos.repository.auth.EmailVerificationRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailVerificationRepository verificationRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.verification-expiry-minutes:10}")
    private int expiryMinutes;

    @Value("${app.email.from:no-reply@todayhouse.com}")
    private String fromAddress;

    @Transactional
    public void sendVerificationCode(String email, String userName) {
        String normalizedEmail = email.toLowerCase();
        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        EmailVerification verification = verificationRepository.findByEmail(normalizedEmail)
                .map(existing -> {
                    existing.refresh(code, expiresAt);
                    return existing;
                })
                .orElseGet(() -> EmailVerification.builder()
                        .email(normalizedEmail)
                        .code(code)
                        .expiresAt(expiresAt)
                        .verified(false)
                        .build());

        verificationRepository.save(verification);
        sendVerificationMail(normalizedEmail, userName, code);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        LocalDateTime now = LocalDateTime.now();
        return verificationRepository.findByEmail(email.toLowerCase())
                .filter(verification -> !verification.isExpired(now))
                .filter(verification -> verification.getCode().equals(code))
                .map(verification -> {
                    verification.markVerified();
                    return true;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return verificationRepository.findByEmail(email.toLowerCase())
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    @Transactional
    public void consumeVerification(String email) {
        verificationRepository.deleteByEmail(email.toLowerCase());
    }

    private void sendVerificationMail(String to, String userName, String code) {
        Context ctx = new Context();
        ctx.setVariable("verificationCode", code);
        ctx.setVariable("expiryMinutes", expiryMinutes);
        ctx.setVariable("userName", displayName(userName));
        String html = templateEngine.process("mail/verification-email", ctx);

        String sender = StringUtils.hasText(fromAddress) ? fromAddress : to;

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setFrom(sender);
            helper.setSubject("코딩의집 이메일 인증번호 안내");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("메일 발송 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("이메일을 전송하지 못했습니다.", e);
        }
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String displayName(String userName) {
        return StringUtils.hasText(userName) ? userName : "회원님";
    }
}
