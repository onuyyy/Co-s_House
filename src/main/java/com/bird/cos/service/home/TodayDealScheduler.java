package com.bird.cos.service.home;

import com.bird.cos.domain.product.Product;
import com.bird.cos.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("!test")
@Slf4j
@Component
@RequiredArgsConstructor
public class TodayDealScheduler {

    private static final int TARGET_DEAL_COUNT = 4;

    private final ProductRepository productRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void rotateDailyDeals() {
        refreshTodayDeals("cron");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDealsOnStartup() {
        refreshTodayDeals("startup");
    }

    private void refreshTodayDeals(String trigger) {
        List<Product> currentDeals = productRepository.findByIsTodayDealTrue();
        if ("startup".equals(trigger) && !currentDeals.isEmpty()) {
            log.info("기존 오늘의 딜 {}건이 있어 startup 트리거 실행을 건너뜁니다.", currentDeals.size());
            return;
        }
        currentDeals.forEach(product -> product.setIsTodayDeal(false));

        List<Product> candidates = productRepository.findTopCandidatesForTodayDeals(PageRequest.of(0, TARGET_DEAL_COUNT));
        if (candidates.isEmpty()) {
            log.warn("오늘의 딜 후보가 없어 {} 트리거에서 기존 딜만 초기화했습니다.", trigger);
            return;
        }

        candidates.forEach(product -> product.setIsTodayDeal(true));
        log.info("{} 트리거로 오늘의 딜을 {}건 갱신했습니다.", trigger, candidates.size());
    }
}
