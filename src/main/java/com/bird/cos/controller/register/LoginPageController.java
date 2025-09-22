package com.bird.cos.controller.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.exception.UnauthorizedException;
import com.bird.cos.service.auth.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.bird.cos.security.AuthorityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import com.bird.cos.repository.user.UserRepository;

@Controller
@RequestMapping("/controller/register")
@RequiredArgsConstructor
public class LoginPageController {

    private final AuthService authService;
    private final AuthorityService authorityService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    

    @GetMapping("/login")
    public String loginPage() {
        return "register/login"; // templates/register/login.html
    }

    // Thymeleaf 폼 전송을 위한 로그인 엔드포인트 (application/x-www-form-urlencoded)
    @PostMapping("/login-form")
    public String loginForm(@RequestParam("loginEmail") String loginEmail,
                            @RequestParam("password") String password,
                            HttpSession session,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        try {
            // 표준 인증 플로우
            Authentication result = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginEmail, password)
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(result);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            // 세션 저장(기존 호환)
            User user = userRepository.findByUserEmail(loginEmail).orElseThrow();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userEmail", user.getUserEmail());
            session.setAttribute("userName", user.getUserName());
            session.setAttribute("user", user);

            // 어드민이면 어드민 페이지로, 아니면 홈으로
            if (result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("admin_role"))) {
                return "redirect:/api/admin";
            }
            return "redirect:/";
        } catch (UnauthorizedException e) {
            return "redirect:/controller/register/login?error=1";
        }
    }
}
