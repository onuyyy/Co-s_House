package com.bird.cos.domain.brand;

import com.bird.cos.domain.proudct.Product;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "BRAND")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "brand_name", length = 100, unique = true, nullable = false)
    private String brandName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "brand_description", columnDefinition = "TEXT")
    private String brandDescription;

    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

}