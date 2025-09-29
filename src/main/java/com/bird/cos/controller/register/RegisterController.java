package com.bird.cos.controller.register;

import com.bird.cos.domain.user.User;
import com.bird.cos.dto.user.RegisterRequest;
import com.bird.cos.dto.user.UserResponse;
import com.bird.cos.dto.user.LoginRequest;
import com.bird.cos.exception.ErrorCode;
import com.bird.cos.exception.UnauthorizedException;
import com.bird.cos.service.auth.AuthService;
import com.bird.cos.service.register.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.security.AuthorityService;

@Slf4j
@RestController
@RequestMapping("/controller/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final AuthService authService;
    private final AuthorityService authorityService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final UserRepository userRepository;

    // 회원가입 201 Created
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = registerService.register(req);
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest req,
                        HttpSession session,
                        HttpServletRequest request,
                        HttpServletResponse response) throws UnauthorizedException {
        // 표준 인증 플로우: AuthenticationManager를 통해 인증 수행
        Authentication result = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.loginEmail(), req.password())
        );

        // SecurityContext 저장(세션에 지속)
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(result);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        // 세션 사용자 정보(기존 호환): 도메인 User를 세션에 보관
        User user = userRepository.findByUserEmail(req.loginEmail()).orElseThrow();
        session.setAttribute("userEmail", user.getUserEmail());
        session.setAttribute("userName", user.getUserName());
        session.setAttribute("user", user);

        return "로그인 유저 : " + user.getUserEmail();
    }

  /*  @PostMapping("/logout")
    //@ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        log.info("[RegisterController.logout] - 로그아웃");
        session.invalidate();
    }*/

    @GetMapping("/me")
    public UserResponse getCurrentUser(HttpSession session) throws UnauthorizedException {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new UnauthorizedException(ErrorCode.UNAUTHORIZED);
        }
        return UserResponse.from(user);
    }
}
