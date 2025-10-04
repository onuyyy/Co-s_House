package com.bird.cos.service.home;

import com.bird.cos.dto.events.EventCardResponse;
import com.bird.cos.repository.post.PostRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.service.event.EventService;
import com.bird.cos.service.home.dto.HomeEventDto;
import com.bird.cos.service.home.dto.HomePostDto;
import com.bird.cos.service.home.dto.HomeProductDto;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class HomeService {
    private static final String DEFAULT_EVENT_IMAGE = "/images/home.jpeg";

    private final ProductRepository productRepository;
    private final PostRepository postRepository;
    private final EventService eventService;
    private final ResourceLoader resourceLoader;

    public HomeService(ProductRepository productRepository,
                       PostRepository postRepository,
                       EventService eventService,
                       ResourceLoader resourceLoader) {
        this.productRepository = productRepository;
        this.postRepository = postRepository;
        this.eventService = eventService;
        this.resourceLoader = resourceLoader;
    }

    public List<HomeProductDto> todayDeals(int limit) {
        return productRepository.findTodayDeals(PageRequest.of(0, Math.max(1, limit)));
    }

    public List<HomeProductDto> popularProducts(int limit) {
        return productRepository.findPopular(PageRequest.of(0, Math.max(1, limit)));
    }

    public List<HomePostDto> getTopPublic() {
        Pageable pageable = PageRequest.of(0, 8);
        return postRepository.findTopPublic(pageable).stream()
                .map(p -> new HomePostDto(
                        p.getPostId(),
                        p.getTitle(),
                        p.getThumbnailUrl(),
                        p.getCommentCount()
                ))
                .toList();
    }


    public List<HomeEventDto> highlightEvents() {
        List<HomeEventDto> activeEventDtos = eventService.getActiveEventCards().stream()
                .filter(Objects::nonNull)
                .map(this::mapToHomeEvent)
                .limit(5)
                .toList();

        if (!activeEventDtos.isEmpty()) {
            return activeEventDtos;
        }

        return fallbackEvents();
    }

    private HomeEventDto mapToHomeEvent(EventCardResponse card) {
        String eyebrow = hasText(card.getTag()) ? card.getTag() : "이벤트";
        String title = hasText(card.getTitle()) ? card.getTitle() : "진행 중인 이벤트";

        String description = hasText(card.getSummary())
                ? card.getSummary()
                : (hasText(card.getPeriod()) ? card.getPeriod() : "자세한 내용은 이벤트 페이지에서 확인하세요.");

        String period = hasText(card.getPeriod()) ? card.getPeriod() : null;
        String linkUrl = hasText(card.getSlug()) ? "/events/" + card.getSlug() : "/events";
        String imageUrl = resolveLocalImage(card);
        if (imageUrl == null) {
            imageUrl = normalizeImageUrl(card.getImage());
        }
        if (imageUrl == null) {
            imageUrl = DEFAULT_EVENT_IMAGE;
        }

        return new HomeEventDto(eyebrow, title, description, period, linkUrl, imageUrl);
    }

    private List<HomeEventDto> fallbackEvents() {
        return List.of(
                new HomeEventDto(
                        "이벤트",
                        "가을맞이 리빙페어",
                        "브랜드 단독 특가와 10% 쿠폰",
                        "2024.09.01 ~ 2024.09.30",
                        "/events",
                        DEFAULT_EVENT_IMAGE
                ),
                new HomeEventDto(
                        "한정 프로모션",
                        "주말 타임딜",
                        "토·일 단 이틀! 70% 타임 세일",
                        "매주 주말 진행",
                        "/events",
                        DEFAULT_EVENT_IMAGE
                ),
                new HomeEventDto(
                        "멤버십 혜택",
                        "신규 가입 웰컴 기프트",
                        "첫 구매 시 적립금과 5% 쿠폰 지급",
                        null,
                        "/events",
                        DEFAULT_EVENT_IMAGE
                )
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalizeImageUrl(String imageUrl) {
        if (!hasText(imageUrl)) {
            return null;
        }
        String trimmed = imageUrl.trim();

        if (!looksLikeImagePath(trimmed)) {
            return null;
        }

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("//")) {
            return trimmed;
        }
        if (trimmed.startsWith("/")) {
            return trimmed;
        }
        return "/" + trimmed;
    }

    private boolean looksLikeImagePath(String value) {
        // 허용 확장자: png, jpg, jpeg, webp, gif, svg (쿼리스트링 허용)
        return value.matches("(?i).+\\.(png|jpe?g|webp|gif|svg)(\\?.*)?$");
    }

    private String resolveLocalImage(EventCardResponse card) {
        if (!hasText(card.getSlug())) {
            return null;
        }

        String sanitizedSlug = card.getSlug().replaceAll("[^a-zA-Z0-9-_]", "");
        if (!hasText(sanitizedSlug)) {
            return null;
        }

        String[] baseDirs = {"/images/events/", "/images/"};
        String[] extensions = {".png", ".jpg", ".jpeg", ".webp"};

        for (String baseDir : baseDirs) {
            for (String extension : extensions) {
                String candidate = baseDir + sanitizedSlug + extension;
                if (resourceExists(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private boolean resourceExists(String webPath) {
        Resource resource = resourceLoader.getResource("classpath:/static" + webPath);
        return resource.exists();
    }
}
