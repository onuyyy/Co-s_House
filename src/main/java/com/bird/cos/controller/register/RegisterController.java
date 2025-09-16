package com.bird.cos.controller.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.user.RegisterRequest;
import com.bird.cos.dto.user.UserResponse;
import com.bird.cos.dto.user.LoginRequest;
import com.bird.cos.exception.UnauthorizedException;
import com.bird.cos.service.auth.AuthService;
import com.bird.cos.service.register.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RestController
@RequestMapping("/controller/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final AuthService authService;


    // 회원가입 201 Created
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = registerService.register(req);
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest req, HttpSession session) throws UnauthorizedException {
        User user = authService.login(req.loginEmail(), req.password());

        //세션 사용자 저장
        session.setAttribute("userEmail", user.getUserEmail());
        session.setAttribute("userName", user.getUserName());
        session.setAttribute("user", user);

        // Spring Security 인증 컨텍스트에 저장(세션으로 지속) - 역할 부여(DB의 userRole 기반)
        java.util.List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        // 기본 사용자 권한 항상 부여
        authorities.add(new SimpleGrantedAuthority("user_role"));
        // 관리자인 경우 관리자 권한 추가 부여
        if ("admin_role".equalsIgnoreCase(user.getUserRole())) {
            authorities.add(new SimpleGrantedAuthority("admin_role"));
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUserEmail(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        return "로그인 유저 : " + user.getUserEmail();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        log.info("[RegisterController.logout] - 로그아웃");
        session.invalidate();
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(HttpSession session) throws UnauthorizedException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new UnauthorizedException("not logged in");
        }
        return UserResponse.from(user);
    }
}
