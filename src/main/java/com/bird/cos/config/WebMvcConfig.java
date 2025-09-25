package com.bird.cos.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct; // Spring Boot 3.x용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 스프링 설정 클래스임을 명시
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class); // 로거 추가

    @Value("${file.upload-dir}") // application.yml의 file.upload-dir 값을 주입받음
    private String uploadDir;

    @PostConstruct // 애플리케이션 시작 시 이 메소드 실행
    public void init() {
        log.info("### 파일 업로드 경로 확인: {}", uploadDir); // 경로를 로그로 출력
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // "/images/**" URL로 요청이 들어오면
//        // "file:///실제_파일_저장_경로/" 에서 파일을 찾도록 매핑합니다.
//        registry.addResourceHandler("/images/**")
//                .addResourceLocations("file:///" + uploadDir); // file:/// 주의 (슬래시 3개)
//    }
}