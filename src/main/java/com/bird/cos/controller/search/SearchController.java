package com.bird.cos.controller.search;

import com.bird.cos.domain.product.Product;
import com.bird.cos.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 48;

    private final ProductService productService;

    @GetMapping("/search")
    public String search(@RequestParam(name = "keyword", required = false) String keyword,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size,
                         Model model) {

        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : "";
        int pageIndex = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), MAX_SIZE);

        Page<Product> productPage = productService.searchProducts(normalizedKeyword, pageIndex, pageSize);
        List<Product> products = productPage.getContent();

        int totalPages = productPage.getTotalPages();
        List<Integer> pageNumbers = buildPageNumbers(pageIndex, totalPages);

        model.addAttribute("keyword", normalizedKeyword);
        model.addAttribute("hasKeyword", StringUtils.hasText(normalizedKeyword));
        model.addAttribute("products", products);
        model.addAttribute("productPage", productPage);
        model.addAttribute("totalCount", productPage.getTotalElements());
        model.addAttribute("currentPage", pageIndex);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("pageNumbers", pageNumbers);

        return "search/search-result";
    }

    private List<Integer> buildPageNumbers(int currentPage, int totalPages) {
        if (totalPages <= 0) {
            return List.of();
        }

        final int windowSize = 5;
        int zeroBasedPage = Math.max(currentPage, 1) - 1;
        int windowStart = (zeroBasedPage / windowSize) * windowSize + 1;
        int windowEnd = Math.min(windowStart + windowSize - 1, totalPages);

        List<Integer> pages = new ArrayList<>(windowEnd - windowStart + 1);
        for (int page = windowStart; page <= windowEnd; page++) {
            pages.add(page);
        }
        return pages;
    }
}
