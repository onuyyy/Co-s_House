package com.bird.cos.service.home;

import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.post.PostRepository;
import com.bird.cos.service.home.dto.HomeEventDto;
import com.bird.cos.service.home.dto.HomePostDto;
import com.bird.cos.service.home.dto.HomeProductDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {
    private final ProductRepository productRepository;
    private final PostRepository postRepository;

    public HomeService(ProductRepository productRepository, PostRepository postRepository) {
        this.productRepository = productRepository;
        this.postRepository = postRepository;
    }

    public List<HomeProductDto> todayDeals(int limit) {
        return productRepository.findTodayDeals(PageRequest.of(0, Math.max(1, limit)));
    }

    public List<HomeProductDto> popularProducts(int limit) {
        return productRepository.findPopular(PageRequest.of(0, Math.max(1, limit)));
    }

    public List<HomePostDto> topPosts(int limit) {
        return postRepository.findTopPublic(PageRequest.of(0, Math.max(1, limit)));
    }

    public List<HomeEventDto> highlightEvents() {
        return List.of(
                new HomeEventDto(
                        "이벤트",
                        "가을맞이 리빙페어",
                        "브랜드 단독 특가와 10% 쿠폰",
                        "/events/fall",
                        "/uploads/23374d07-efbf-4c52-9981-092b0dd2d3e5.jpg"
                ),
                new HomeEventDto(
                        "한정 프로모션",
                        "주말 타임딜",
                        "토·일 단 이틀! 70% 타임 세일",
                        "/events/weekend",
                        "/uploads/b9412931-0aaf-4e7c-9c9d-a9c3647c5573.jpg"
                ),
                new HomeEventDto(
                        "멤버십 혜택",
                        "신규 가입 웰컴 기프트",
                        "첫 구매 시 적립금과 5% 쿠폰 지급",
                        "/events/welcome",
                        null
                )
        );
    }
}
