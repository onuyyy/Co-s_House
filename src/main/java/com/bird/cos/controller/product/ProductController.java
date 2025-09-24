package com.bird.cos.controller.product;

import com.bird.cos.domain.product.Product;
import com.bird.cos.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {
    @Autowired
    ProductService productService;

    //카테고리별 조회
    @GetMapping("/product/category/{categoryId}")
    public String showProductsByCategory(Model model, @PathVariable Long categoryId) {

        List<Product> products = productService.getProductsByCategory(categoryId);

        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    //특정 카테고리의 상품을 세일가격 기준 오름차순으로 조회
    @GetMapping("/product/category/{categoryId}/price-asc")
    public String showProductsByCategoryOrderByPriceAsc(Model model, @PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryOrderBySalePriceAsc(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    //특정 카테고리의 상품을 세일가격 기준 내림차순으로 조회
    @GetMapping("/product/category/{categoryId}/price-desc")
    public String showProductsByCategoryOrderByPriceDesc(Model model, @PathVariable Long categoryId) {
        List<Product> products = productService.getProductsByCategoryOrderBySalePriceDesc(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }

    // 특정 카테고리의 상품을 평균평점 기준 내림차순으로 조회 (평점 높은순)
    @GetMapping("/product/category/{categoryId}/rating-desc")
    public String showProductsByCategoryOrderByRating(Model model, @PathVariable Long categoryId) {
        // Service에 정의된, 평점순으로 조회하는 메서드를 호출합니다.
        List<Product> products = productService.getProductsByCategoryOrderByRatingDesc(categoryId);
        model.addAttribute("products", products);
        model.addAttribute("totalCount", products != null ? products.size() : 0);
        return "product/productList";
    }


    //상품 페이지
    @GetMapping("/product")
    public String selectProduct(Model model) {
        //실제 데이터를 받아오는 기능
        List <Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        //실제 데이터 카운트하는 기능
        int totalCount = products.size();
        model.addAttribute("totalCount", totalCount);

        return "product/product";
    }

    //상세페이지
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        Optional<Product> productOpt = productService.getProductById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            System.out.println("Product detail: " + product.getProductTitle());

            model.addAttribute("product", product);
            model.addAttribute("options", product.getOptions());
            model.addAttribute("brandId", product.getBrand().getBrandId());
            model.addAttribute("brand", product.getBrand());
            model.addAttribute("basePrice", product.getSalePrice());

            return "product/productDetail";
        } else {
            return "redirect:/";
        }
    }

}
