package com.bird.cos.repository.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.service.home.dto.HomeProductDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select new com.bird.cos.service.home.dto.HomeProductDto(p.productId, p.productTitle, p.originalPrice, p.salePrice, p.discountRate, p.averageRating, p.reviewCount) " +
            "from Product p where p.isTodayDeal = true order by p.discountRate desc nulls last, p.salesCount desc")
    List<HomeProductDto> findTodayDeals(Pageable pageable);

    @Query("select new com.bird.cos.service.home.dto.HomeProductDto(p.productId, p.productTitle, p.originalPrice, p.salePrice, p.discountRate, p.averageRating, p.reviewCount) " +
            "from Product p order by p.salesCount desc, p.viewCount desc")
    List<HomeProductDto> findPopular(Pageable pageable);
}
