package com.bird.cos.controller.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.exception.UnauthorizedException;
import com.bird.cos.service.auth.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/controller/register")
@RequiredArgsConstructor
public class LoginPageController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // templates/login.html
    }

    // Thymeleaf 폼 전송을 위한 로그인 엔드포인트 (application/x-www-form-urlencoded)
    @PostMapping("/login-form")
    public String loginForm(@RequestParam("loginEmail") String loginEmail,
                            @RequestParam("password") String password,
                            HttpSession session) {
        try {
            User user = authService.login(loginEmail, password);

            // 세션 저장
            session.setAttribute("userEmail", user.getUserEmail());
            session.setAttribute("userName", user.getUserName());
            session.setAttribute("user", user);

            // Security 컨텍스트 저장 (역할: user_role 기본, admin_role 추가 가능)
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("user_role"));
            if ("admin_role".equalsIgnoreCase(user.getUserRole())) {
                authorities.add(new SimpleGrantedAuthority("admin_role"));
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user.getUserEmail(), null, authorities);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // 어드민이면 어드민 페이지로, 아니면 홈으로
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("admin_role"))) {
                return "redirect:/api/admin/users";
            }
            return "redirect:/";
        } catch (UnauthorizedException e) {
            return "redirect:/controller/register/login?error=1";
        }
    }
}
