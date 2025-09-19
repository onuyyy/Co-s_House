package com.bird.cos.service.brand;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.repository.brand.BrandRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor; // 혹은 @Autowired 사용

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;


    public Brand findById(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 브랜드를 찾을 수 없습니다: " + brandId));
    }
}