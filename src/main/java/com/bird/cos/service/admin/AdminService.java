package com.bird.cos.service.admin;

import com.bird.cos.domain.brand.Brand;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductCategory;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserRole;
import com.bird.cos.dto.admin.*;
import com.bird.cos.repository.brand.BrandRepository;
import com.bird.cos.repository.common.CommonCodeRepository;
import com.bird.cos.repository.product.ProductCategoryRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserRepository;
import com.bird.cos.repository.user.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
@RequiredArgsConstructor
@Service
public class AdminService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final BrandRepository brandRepository;
    private final CommonCodeRepository commonCodeRepository;

    @Transactional(readOnly = true)
    public Page<UserManageResponse> getAllUsers(
            UserManageSearchType searchType, String searchValue, Pageable pageable) {
        
        Page<User> users;

        if (searchValue == null || searchValue.isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = switch (searchType) {
                case NAME -> userRepository.findUsersByUserNameContainingIgnoreCase(searchValue, pageable);
                case EMAIL -> userRepository.findUsersByUserEmailContainingIgnoreCase(searchValue, pageable);
                case NICKNAME -> userRepository.findUsersByUserNicknameContainingIgnoreCase(searchValue, pageable);
                case PHONE -> userRepository.findUsersByUserPhoneContainingIgnoreCase(searchValue, pageable);
                case ROLE -> userRepository.findUsersByRoleName(searchValue, pageable);
            };
        }

        return users.map(UserManageResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<UserManageResponse> getAdminList(
            UserManageSearchType searchType, String searchValue, Pageable pageable) {
        
        Page<User> admins;

        if (searchValue == null || searchValue.isEmpty()) {
            // ADMIN 또는 SUPER_ADMIN 역할만 조회
            admins = userRepository.findAdminUsers(pageable);
        } else {
            admins = switch (searchType) {
                case NAME -> userRepository.findAdminUsersByUserNameContaining(searchValue, pageable);
                case EMAIL -> userRepository.findAdminUsersByUserEmailContaining(searchValue, pageable);
                case NICKNAME -> userRepository.findAdminUsersByUserNicknameContaining(searchValue, pageable);
                case PHONE -> userRepository.findAdminUsersByUserPhoneContaining(searchValue, pageable);
                case ROLE -> userRepository.findUsersByRoleName(searchValue, pageable);
            };
        }

        return admins.map(UserManageResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<UserManageResponse> getUsersByRole(
            String roleName, UserManageSearchType searchType, String searchValue, Pageable pageable) {
        
        Page<User> users;

        if (searchValue == null || searchValue.isEmpty()) {
            users = userRepository.findUsersByRoleName(roleName, pageable);
        } else {
            users = switch (searchType) {
                case NAME -> userRepository.findUsersByRoleNameAndUserNameContaining(roleName, searchValue, pageable);
                case EMAIL -> userRepository.findUsersByRoleNameAndUserEmailContaining(roleName, searchValue, pageable);
                case NICKNAME -> userRepository.findUsersByRoleNameAndUserNicknameContaining(roleName, searchValue, pageable);
                case PHONE -> userRepository.findUsersByRoleNameAndUserPhoneContaining(roleName, searchValue, pageable);
                case ROLE -> userRepository.findUsersByRoleName(searchValue, pageable);
            };
        }

        return users.map(UserManageResponse::from);
    }

    @Transactional(readOnly = true)
    public UserManageResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return UserManageResponse.from(user);
    }

    public void updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이메일 중복 체크
        if (request.getUserEmail() != null && !request.getUserEmail().trim().isEmpty()) {
            String newEmail = request.getUserEmail().trim();
            if (!user.getUserEmail().equals(newEmail) && 
                userRepository.existsByUserEmailAndUserIdNot(newEmail, userId)) {
                throw new RuntimeException("이미 사용 중인 이메일입니다.");
            }
        }

        // 닉네임 중복 체크
        if (request.getUserNickname() != null && !request.getUserNickname().trim().isEmpty()) {
            String newNickname = request.getUserNickname().trim();
            if (!user.getUserNickname().equals(newNickname) && 
                userRepository.existsByUserNicknameAndUserIdNot(newNickname, userId)) {
                throw new RuntimeException("이미 사용 중인 닉네임입니다.");
            }
        }

        // 역할 변경
        if (request.getUserRoleId() != null) {
            UserRole newRole = userRoleRepository.findById(request.getUserRoleId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 역할입니다."));
            user.changeRole(newRole);
        }

        user.update(request);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("삭제할 사용자가 없습니다.");
        }
        userRepository.deleteById(userId);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductManageResponse> getProductList(
            ProductManageSearchType searchType, String searchValue, Pageable pageable) {
        
        Page<Product> products;

        if (searchValue == null || searchValue.isEmpty()) {
            products = productRepository.findAll(pageable);
        } else {
            products = switch (searchType) {
                case TITLE -> productRepository.findProductsByProductTitleContainingIgnoreCase(searchValue, pageable);
                case BRAND -> productRepository.findProductsByBrandNameContainingIgnoreCase(searchValue, pageable);
                case CATEGORY -> productRepository.findProductsByCategoryNameContainingIgnoreCase(searchValue, pageable);
                case STATUS -> productRepository.findProductsByStatusContainingIgnoreCase(searchValue, pageable);
                case COLOR -> productRepository.findProductsByProductColorContainingIgnoreCase(searchValue, pageable);
            };
        }

        return products.map(ProductManageResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductManageResponse getProductDetail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        return ProductManageResponse.from(product);
    }

    public void updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        product.update(request);
    }

    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("삭제할 상품이 없습니다.");
        }
        productRepository.deleteById(productId);
    }

    @Transactional(readOnly = true)
    public List<BrandManageResponse> getBrandList() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream().map(BrandManageResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<BrandManageResponse> getBrandList(
            BrandManageSearchType searchType, String searchValue, Pageable pageable) {
        
        Page<Brand> brands;

        if (searchValue == null || searchValue.isEmpty()) {
            brands = brandRepository.findAll(pageable);
        } else {
            brands = switch (searchType) {
                case NAME -> brandRepository.findBrandsByBrandNameContainingIgnoreCase(searchValue, pageable);
                case DESCRIPTION -> brandRepository.findBrandsByBrandDescriptionContainingIgnoreCase(searchValue, pageable);
            };
        }

        return brands.map(BrandManageResponse::from);
    }

    @Transactional(readOnly = true)
    public BrandManageResponse getBrandDetail(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("브랜드를 찾을 수 없습니다."));

        return BrandManageResponse.from(brand);
    }

    public void updateBrand(Long brandId, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("브랜드를 찾을 수 없습니다."));

        // 브랜드명 중복 체크 (자신 제외)
        if (request.getBrandName() != null && !request.getBrandName().trim().isEmpty()) {
            String newBrandName = request.getBrandName().trim();
            if (!brand.getBrandName().equals(newBrandName) && 
                brandRepository.existsByBrandNameIgnoreCase(newBrandName)) {
                throw new RuntimeException("이미 존재하는 브랜드명입니다.");
            }
        }

        brand.update(request);
    }

    public void deleteBrand(Long brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("삭제할 브랜드가 없습니다."));

        // 해당 브랜드에 상품이 있는지 확인
        if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
            throw new RuntimeException("해당 브랜드에 연결된 상품이 있어 삭제할 수 없습니다.");
        }

        brandRepository.deleteById(brandId);
    }

    @Transactional(readOnly = true)
    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    public void createBrand(BrandCreateRequest request) {

        if (brandRepository.existsByBrandNameIgnoreCase(request.getBrandName())) {
            throw new IllegalArgumentException("이미 존재하는 브랜드 이름입니다.");
        }

        brandRepository.save(
                Brand.builder()
                        .brandName(request.getBrandName())
                        .brandDescription(request.getBrandDescription())
                        .logoUrl(request.getLogoUrl())
                        .build()
        );
    }

    public void createProduct(ProductCreateRequest request) {

        ProductCategory category = productCategoryRepository.findById(request.getProductCategoryId())
                .orElseThrow(() -> new RuntimeException("카테고리가 존재하지 않습니다."));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("브랜드가 존재하지 않습니다."));

        Product product = Product.builder()
                .productTitle(request.getProductTitle())
                .brand(brand)
                .productCategory(category)
                .mainImageUrl(request.getMainImageUrl())
                .description(request.getDescription())
                .originalPrice(request.getOriginalPrice())
                .salePrice(request.getSalePrice())
                .couponPrice(request.getCouponPrice())
                .discountRate(request.getDiscountRate())
                .isFreeShipping(request.getIsFreeShipping())
                .isTodayDeal(request.getIsTodayDeal())
                .isCohouseOnly(request.getIsCohouseOnly())
                .productColor(request.getProductColor())
                .material(request.getMaterial())
                .capacity(request.getCapacity())
                .stockQuantity(request.getStockQuantity())
                .productStatusCode(commonCodeRepository.findById(request.getProductStatusCodeId())
                        .orElseThrow(() -> new RuntimeException("상품 상태 코드를 찾을 수 없습니다: " + request.getProductStatusCodeId())))
                .build();

        productRepository.save(product);
    }

    public List<ProductCategoryResponse> getProductCategoryList() {
        List<ProductCategoryResponse> list = new ArrayList<>();
        productCategoryRepository.findAll().forEach(productCategory -> {
            list.add(ProductCategoryResponse.from(productCategory));
        });

        return list;
    }

    public List<ProductCategoryResponse> getProductCategoryLevel1() {
        List<ProductCategory> productCategoryList = productCategoryRepository.findAllByLevel(1);

        return productCategoryList.stream()
                .map(ProductCategoryResponse::from)
                .collect(Collectors.toList());
    }

    public List<ProductCategoryResponse>  getChildCategories(Long parentId) {
        List<ProductCategory> children = productCategoryRepository.findByParentCategory_CategoryId(parentId);
        return children.stream()
                .map(ProductCategoryResponse::from)
                .collect(Collectors.toList());
    }
}
