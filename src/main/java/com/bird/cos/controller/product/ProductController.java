package com.bird.cos.controller.product;

import com.bird.cos.domain.proudct.Product;
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

    @GetMapping("/product")
    public String selectProduct(Model model) {
        //실제 데이터를 받아오는 기능
        List <Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        //실제 데이터 카운트하는 기능
        int totalCount = products.size();
        model.addAttribute("totalCount", totalCount);

        return "product";
    }

    //상세페이지
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        Optional<Product> productOpt = productService.getProductById(productId);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            System.out.println("Product detail: " + product.getProductTitle());
            model.addAttribute("product", product);
            return "product-detail";
        } else {
            return "redirect:/";
        }
    }

}
