package com.bird.cos.service.home;

import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.post.Post;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.post.PostRepository;
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
}
