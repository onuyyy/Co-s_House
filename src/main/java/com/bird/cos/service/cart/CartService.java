package com.bird.cos.service.cart;

import com.bird.cos.domain.cart.Cart;
import com.bird.cos.domain.cart.CartItem;
import com.bird.cos.domain.coupon.Coupon;
import com.bird.cos.domain.coupon.UserCoupon;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.product.ProductOption;
import com.bird.cos.domain.user.User;
import com.bird.cos.dto.cart.AddToCartRequest;
import com.bird.cos.dto.cart.CartItemResponseDto;
import com.bird.cos.dto.cart.CartListResponse;
import com.bird.cos.dto.cart.CartSummaryDto;
import com.bird.cos.repository.cart.CartHeaderRepository;
import com.bird.cos.repository.cart.CartItemRepository;
import com.bird.cos.repository.inventory.InventoryRepository;
import com.bird.cos.repository.mypage.coupon.UserCouponRepository;
import com.bird.cos.repository.product.ProductOptionRepository;
import com.bird.cos.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartHeaderRepository cartHeaderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final UserCouponRepository userCouponRepository;
    private final InventoryRepository inventoryRepository;

    //항목당 최대 수량
    private static final int MAX_PER_ITEM = 50;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 장바구니에 상품 추가
     * - 사용자별 Cart 헤더를 만들고 (없으면 생성), CartItem을 upsert
     */
    public void addCart(AddToCartRequest request, User user) {

        Long productId = request.productId();
        Integer quantity = request.quantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        int requestQty = quantity == null ? 1 : quantity;

        String selectedOptions = prepareSelectedOptions(request.selectedOptions(), product);

        Cart cart = cartHeaderRepository.findByUser(user)
                .orElseGet(() -> cartHeaderRepository.save(Cart.of(user)));

        CartItem item = cartItemRepository.findByCartAndProductAndSelectedOptions(cart, product, selectedOptions)
                .orElseGet(() -> CartItem.of(cart, product, 0, selectedOptions));

        Integer availableStock = getAvailableStock(product);
        int desired = defaultZero(item.getQuantity()) + requestQty;
        int normalized = normalizeQuantity(desired, availableStock);

        item.setQuantity(normalized);
        item.setCart(cart);
        if ((selectedOptions == null && item.getSelectedOptions() != null)
                || (selectedOptions != null && !selectedOptions.equals(item.getSelectedOptions()))) {
            item.setSelectedOptions(selectedOptions);
        }
        cartItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public CartListResponse getCart(User user) {
        List<CartItem> entities = cartItemRepository.findAllByCart_User(user);
        Set<Long> productIds = entities.stream()
                .map(ci -> ci.getProduct() != null ? ci.getProduct().getProductId() : null)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Long, List<ProductOption>> optionsByProduct = productIds.isEmpty()
                ? Map.of()
                : productOptionRepository.findByProduct_ProductIdIn(productIds).stream()
                .collect(Collectors.groupingBy(opt -> opt.getProduct().getProductId()));

        Map<Long, Integer> inventoryByProduct;
        if (productIds.isEmpty()) {
            inventoryByProduct = Map.of();
        } else {
            inventoryByProduct = inventoryRepository.findByProductIds(productIds).stream()
                    .collect(Collectors.toMap(inv -> inv.getProductId().getProductId(),
                            inv -> inv.getCurrentQuantity() != null ? inv.getCurrentQuantity() : 0,
                            (existing, replacement) -> existing));
        }

        List<CartItemResponseDto> items = entities.stream()
                .map(item -> toDto(item, optionsByProduct, inventoryByProduct))
                .toList();
        CartSummaryDto summary = summarize(user, entities, items);
        return new CartListResponse(items, summary);
    }

    //사용자 장바구니 목록 조회, 가격 계산: PriceCalculator 사용, 라인합계 포함
    @Transactional(readOnly = true)
    public List<CartItemResponseDto> getList(User user) {
        return getCart(user).items();
    }

    //장바구니 단건 상세 조회 (소유자 검증)
    @Transactional(readOnly = true)
    public CartItemResponseDto getDetail(Long cartItemId, User user) {
        CartItem item = cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)
                .orElseThrow(() -> new RuntimeException("장바구니 정보를 조회할 수 없습니다."));
        Long productId = item.getProduct() != null ? item.getProduct().getProductId() : null;
        Map<Long, List<ProductOption>> optionMap = productId == null
                ? Map.of()
                : Map.of(productId, productOptionRepository.findByProduct_ProductId(productId));
        Integer availableStock = getAvailableStock(item.getProduct());
        Map<Long, Integer> inventoryMap = (productId == null || availableStock == null)
                ? Map.of()
                : Map.of(productId, availableStock);
        return toDto(item, optionMap, inventoryMap);
    }

    //장바구니 상품 삭제 (단건/다건)
    public void delete(List<Long> cartItemIds, User user) {
        var deleteTargets = cartItemRepository.findAllByCartItemIdInAndCart_User(cartItemIds, user);
        if (deleteTargets.isEmpty()) {
            throw new RuntimeException("삭제할 항목이 존재하지 않습니다.");
        }
        cartItemRepository.deleteAll(deleteTargets);
    }

    //수량 변경
    public void updateQuantity(Long cartItemId, User user, Integer quantity) {
        if (quantity == null) throw new RuntimeException("수량이 필요합니다.");
        CartItem item = cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));
        Product product = item.getProduct();
        Integer availableStock = getAvailableStock(product);
        int normalized = normalizeQuantity(quantity, availableStock);
        item.setQuantity(normalized);
        cartItemRepository.save(item);
    }

    public void updateOptions(Long cartItemId, User user, String selectedOptions) {
        CartItem item = cartItemRepository.findByCartItemIdAndCart_User(cartItemId, user)
                .orElseThrow(() -> new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        if (selectedOptions == null || selectedOptions.isBlank()) {
            item.setSelectedOptions(null);
            cartItemRepository.save(item);
            return;
        }

        Long optionId;
        try {
            optionId = Long.parseLong(selectedOptions.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효한 옵션 ID가 아닙니다.");
        }

        var option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("옵션을 찾을 수 없습니다."));

        if (option.getProduct() == null || item.getProduct() == null
                || !option.getProduct().getProductId().equals(item.getProduct().getProductId())) {
            throw new RuntimeException("해당 상품의 옵션이 아닙니다.");
        }

        item.setSelectedOptions(String.valueOf(optionId));
        cartItemRepository.save(item);
    }

    //게스트 카트 병합
    public void mergeGuestCart(List<GuestItem> items, User user) {
        Cart cart = cartHeaderRepository.findByUser(user)
                .orElseGet(() -> cartHeaderRepository.save(Cart.of(user)));
        for (GuestItem g : items) {
            Product product = productRepository.findById(g.productId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
            String selectedOptions = prepareSelectedOptions(g.selectedOptions, product);
            CartItem item = cartItemRepository.findByCartAndProductAndSelectedOptions(cart, product, selectedOptions)
                    .orElseGet(() -> CartItem.of(cart, product, 0, selectedOptions));
            Integer availableStock = getAvailableStock(product);
            int desired = defaultZero(item.getQuantity()) + defaultZero(g.quantity);
            int normalized = normalizeQuantity(desired, availableStock);
            item.setQuantity(normalized);
            item.setCart(cart);
            if ((selectedOptions == null && item.getSelectedOptions() != null)
                    || (selectedOptions != null && !selectedOptions.equals(item.getSelectedOptions()))) {
                item.setSelectedOptions(selectedOptions);
            }
            cartItemRepository.save(item);
        }
    }

    public record GuestItem(Long productId, Integer quantity, String selectedOptions) {}

    private String prepareSelectedOptions(String rawSelectedOptions, Product product) {
        if (rawSelectedOptions == null) {
            return null;
        }
        String trimmed = rawSelectedOptions.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        Long optionId;
        try {
            optionId = Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효한 옵션 ID가 아닙니다.");
        }

        ProductOption option = productOptionRepository.findById(optionId)
                .orElseThrow(() -> new RuntimeException("옵션을 찾을 수 없습니다."));

        if (option.getProduct() == null || product == null
                || option.getProduct().getProductId() == null
                || product.getProductId() == null
                || !option.getProduct().getProductId().equals(product.getProductId())) {
            throw new RuntimeException("해당 상품의 옵션이 아닙니다.");
        }

        return optionId.toString();
    }

    private CartSummaryDto summarize(User user, List<CartItem> entities, List<CartItemResponseDto> items) {
        int totalQty = items.stream()
                .map(i -> i.getQuantity() == null ? 0 : i.getQuantity())
                .reduce(0, Integer::sum);
        BigDecimal totalAmount = items.stream()
                .map(i -> i.getLineTotal() == null ? ZERO : i.getLineTotal())
                .reduce(ZERO, BigDecimal::add);
        long outOfStockCount = items.stream()
                .filter(i -> Boolean.TRUE.equals(i.getOutOfStock()))
                .count();

        Map<Long, BigDecimal> amountByBrand = new HashMap<>();
        for (int i = 0; i < entities.size(); i++) {
            CartItem entity = entities.get(i);
            if (entity.getProduct() == null || entity.getProduct().getBrand() == null) {
                continue;
            }
            Long brandId = entity.getProduct().getBrand().getBrandId();
            if (brandId == null) {
                continue;
            }
            BigDecimal lineTotal = items.get(i).getLineTotal();
            if (lineTotal == null || lineTotal.compareTo(ZERO) <= 0) {
                continue;
            }
            amountByBrand.merge(brandId, lineTotal, BigDecimal::add);
        }

        List<UserCoupon> coupons = amountByBrand.isEmpty()
                ? List.of()
                : userCouponRepository.findByUserAndOrderIsNull(user);
        BigDecimal expectedDiscount = calculateExpectedDiscount(amountByBrand, coupons);
        BigDecimal expectedAmount = totalAmount.subtract(expectedDiscount);
        if (expectedAmount.compareTo(ZERO) < 0) {
            expectedAmount = ZERO;
        }

        return CartSummaryDto.builder()
                .totalQuantity(totalQty)
                .totalAmount(totalAmount)
                .expectedDiscount(expectedDiscount)
                .expectedAmount(expectedAmount)
                .changedCount(0)
                .outOfStockCount((int) outOfStockCount)
                .build();
    }

    private BigDecimal calculateExpectedDiscount(Map<Long, BigDecimal> amountByBrand, List<UserCoupon> coupons) {
        if (amountByBrand.isEmpty() || coupons == null || coupons.isEmpty()) {
            return ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        Map<Long, BigDecimal> bestByBrand = new HashMap<>();

        for (UserCoupon userCoupon : coupons) {
            if (userCoupon == null || userCoupon.getUsedAt() != null || !isCouponIssued(userCoupon)) {
                continue;
            }

            Coupon coupon = userCoupon.getCoupon();
            if (coupon == null || !isCouponActive(coupon, now)) {
                continue;
            }

            Product product = coupon.getProduct();
            if (product == null || product.getBrand() == null) {
                continue;
            }

            Long brandId = product.getBrand().getBrandId();
            if (brandId == null) {
                continue;
            }

            BigDecimal brandTotal = amountByBrand.get(brandId);
            if (brandTotal == null || brandTotal.compareTo(ZERO) <= 0) {
                continue;
            }

            BigDecimal discount = computeDiscount(coupon, brandTotal);
            if (discount.compareTo(ZERO) <= 0) {
                continue;
            }

            bestByBrand.merge(brandId, discount, (current, candidate) ->
                    candidate.compareTo(current) > 0 ? candidate : current);
        }

        return bestByBrand.values().stream()
                .reduce(ZERO, BigDecimal::add);
    }

    private boolean isCouponIssued(UserCoupon userCoupon) {
        if (userCoupon.getCouponStatus() == null) {
            return true;
        }
        String codeName = userCoupon.getCouponStatus().getCodeName();
        if (codeName != null && "ISSUED".equalsIgnoreCase(codeName)) {
            return true;
        }
        String codeId = userCoupon.getCouponStatus().getCodeId();
        return codeId != null && "COUPON_001".equalsIgnoreCase(codeId);
    }

    private boolean isCouponActive(Coupon coupon, LocalDateTime now) {
        if (Boolean.FALSE.equals(coupon.getIsActive())) {
            return false;
        }
        if (coupon.getStartDate() != null && coupon.getStartDate().isAfter(now)) {
            return false;
        }
        if (coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(now)) {
            return false;
        }
        return true;
    }

    private BigDecimal computeDiscount(Coupon coupon, BigDecimal baseAmount) {
        BigDecimal minPurchase = moneyOrNull(coupon.getMinPurchaseAmount());
        if (minPurchase != null && baseAmount.compareTo(minPurchase) < 0) {
            return ZERO;
        }

        BigDecimal maxDiscount = positiveMoney(coupon.getMaxDiscountAmount());
        BigDecimal amountDiscount = positiveMoney(coupon.getDiscountAmount());
        BigDecimal rateDiscount = null;
        if (coupon.getDiscountRate() != null && coupon.getDiscountRate().compareTo(ZERO) > 0) {
            rateDiscount = baseAmount.multiply(coupon.getDiscountRate())
                    .divide(HUNDRED, 0, RoundingMode.HALF_UP);
        }

        BigDecimal best = ZERO;
        if (amountDiscount != null) {
            BigDecimal applied = amountDiscount.min(baseAmount);
            if (maxDiscount != null && applied.compareTo(maxDiscount) > 0) {
                applied = maxDiscount;
            }
            best = applied;
        }

        if (rateDiscount != null) {
            BigDecimal applied = rateDiscount;
            if (maxDiscount != null && applied.compareTo(maxDiscount) > 0) {
                applied = maxDiscount;
            }
            if (applied.compareTo(baseAmount) > 0) {
                applied = baseAmount;
            }
            if (applied.compareTo(best) > 0) {
                best = applied;
            }
        }

        if (best.compareTo(baseAmount) > 0) {
            best = baseAmount;
        }

        return best.setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal positiveMoney(BigDecimal value) {
        BigDecimal normalized = moneyOrNull(value);
        if (normalized == null || normalized.compareTo(ZERO) <= 0) {
            return null;
        }
        return normalized;
    }

    private BigDecimal moneyOrNull(BigDecimal value) {
        return value == null ? null : value.setScale(0, RoundingMode.HALF_UP);
    }

    // --------- 내부 헬퍼 ---------
    //DTO 변환 + 가격 계산 동시에 함
    private CartItemResponseDto toDto(CartItem item,
                                      Map<Long, List<ProductOption>> optionsByProduct,
                                      Map<Long, Integer> inventoryByProduct) {
        Product p = item.getProduct();

        BigDecimal unitOriginal = p.getOriginalPrice();
        BigDecimal unitFinal = PriceCalculator.effectiveUnitPrice(p);
        BigDecimal lineTotal = PriceCalculator.lineTotal(unitFinal, defaultZero(item.getQuantity()));

        Long productId = p.getProductId();
        Integer availableStock = null;
        if (inventoryByProduct != null && inventoryByProduct.containsKey(productId)) {
            availableStock = inventoryByProduct.get(productId);
        } else if (p.getStockQuantity() != null) {
            availableStock = p.getStockQuantity();
        }

        boolean outOfStock = availableStock != null && item.getQuantity() != null
                && availableStock < item.getQuantity();

        String status = p.getProductStatusCode() != null ? p.getProductStatusCode().getCodeName() : null;

        List<ProductOption> options = (productId == null)
                ? List.of()
                : optionsByProduct.getOrDefault(productId, List.of());

        String selectedOptionsRaw = item.getSelectedOptions();
        Long selectedOptionId = null;
        if (selectedOptionsRaw != null && !selectedOptionsRaw.isBlank()) {
            try {
                selectedOptionId = Long.parseLong(selectedOptionsRaw.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        String selectedOptionLabel = null;
        if (selectedOptionId != null) {
            Long finalSelectedOptionId = selectedOptionId;
            selectedOptionLabel = options.stream()
                    .filter(opt -> opt.getOptionId().equals(finalSelectedOptionId))
                    .findFirst()
                    .map(this::buildOptionLabel)
                    .orElse(null);
        }

        List<CartItemResponseDto.Option> optionDtos = options.stream()
                .map(opt -> CartItemResponseDto.Option.builder()
                        .optionId(opt.getOptionId())
                        .optionName(opt.getOptionName())
                        .optionValue(opt.getOptionValue())
                        .additionalPrice(opt.getAdditionalPrice())
                        .build())
                .toList();

        return CartItemResponseDto.builder()
                .cartItemId(item.getCartItemId())
                .productId(p.getProductId())
                .title(p.getProductTitle())
                .imageUrl(p.getMainImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(unitOriginal)
                .finalPrice(unitFinal)
                .lineTotal(lineTotal)
                .outOfStock(outOfStock)
                .status(status)
                .selectedOptions(selectedOptionsRaw)
                .selectedOptionId(selectedOptionId)
                .selectedOptionLabel(selectedOptionLabel)
                .options(optionDtos)
                .build();
    }

    private String buildOptionLabel(ProductOption option) {
        if (option == null) {
            return null;
        }
        String name = option.getOptionName() != null ? option.getOptionName().trim() : "";
        String value = option.getOptionValue() != null ? option.getOptionValue().trim() : "";
        if (name.isEmpty()) {
            return value;
        }
        if (value.isEmpty()) {
            return name;
        }
        return name + " - " + value;
    }

    private static int defaultZero(Integer v) {
        return v == null ? 0 : v;
    }

    //수량 검증: 최대 수량/재고 제한 위반 시 예외 처리
    private int normalizeQuantity(int desired, Integer stockQuantity) {
        if (desired <= 0) {
            throw new RuntimeException("수량은 1개 이상이어야 합니다.");
        }
        if (desired > MAX_PER_ITEM) {
            throw new RuntimeException("최대 " + MAX_PER_ITEM + "개까지 담을 수 있습니다.");
        }
        if (stockQuantity != null && desired > stockQuantity) {
            throw new RuntimeException("재고 부족: 최대 " + stockQuantity + "개까지 담을 수 있습니다.");
        }
        return desired;
    }

    private Integer getAvailableStock(Product product) {
        if (product == null) {
            return null;
        }
        return inventoryRepository.findByProductId(product)
                .map(inv -> inv.getCurrentQuantity() != null ? inv.getCurrentQuantity() : 0)
                .orElse(product.getStockQuantity());
    }
}
