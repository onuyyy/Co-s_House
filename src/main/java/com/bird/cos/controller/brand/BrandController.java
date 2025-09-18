package com.bird.cos.controller.brand;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.product.Product;
import com.bird.cos.service.brand.BrandService;
import com.bird.cos.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BrandController{

    @Autowired
    private ProductService productService; // ProductService 주입
    private final BrandService brandService;

    // 브랜드의 모든 상품 조회
    @GetMapping("/brands/{brandId}")
    public String showProductsByBrand(Model model, @PathVariable Long brandId) {

        Brand brand = brandService.findById(brandId);
        if(brand == null) {}
        List<Product> products = productService.getProductsByBrandId(brandId);
        model.addAttribute("brand", brand);
        model.addAttribute("products", products);

        return "product/brandDetail";
    }

    // 브랜드의 상품을 세일가격 기준 오름차순 조회
    @GetMapping("/brands/{brandId}/price-asc")
    public String showProductsByBrandOrderByPriceAsc(Model model, @PathVariable Long brandId) {
        Brand brand = brandService.findById(brandId);
        model.addAttribute("brand", brand);
        List<Product> products = productService.getProductsByBrandOrderBySalePriceAsc(brandId);
        model.addAttribute("products", products);
        return "product/brandDetail";
    }

    // 브랜드의 상품을 세일가격 기준 내림차순 조회
    @GetMapping("/brands/{brandId}/price-desc")
    public String showProductsByBrandOrderByPriceDesc(Model model, @PathVariable Long brandId) {
        Brand brand = brandService.findById(brandId);
        model.addAttribute("brand", brand);
        List<Product> products = productService.getProductsByBrandOrderBySalePriceDesc(brandId);
        model.addAttribute("products", products);
        return "product/brandDetail";
    }

    //브랜드의 상품을 평점순으로 내림차순 조회
    @GetMapping("/brands/{brandId}/rating-desc")
    public String showProductsByBrandOrderByRating(Model model, @PathVariable Long brandId) {
        Brand brand = brandService.findById(brandId);
        model.addAttribute("brand", brand);
        List<Product> products = productService.getProductsByBrandOrderByRatingDesc(brandId);
        model.addAttribute("products", products);
        return "product/brandDetail";
    }


}