package com.bird.cos.service.cart;

import com.bird.cos.domain.cart.Cart;
import com.bird.cos.domain.cart.CartItem;
import com.bird.cos.domain.product.Coupon;
import com.bird.cos.domain.product.Product;
import com.bird.cos.domain.user.User;
import com.bird.cos.domain.user.UserCoupon;
import com.bird.cos.dto.cart.AddToCartRequest;
import com.bird.cos.dto.cart.CartItemResponseDto;
import com.bird.cos.dto.cart.CartListResponse;
import com.bird.cos.dto.cart.CartSummaryDto;
import com.bird.cos.repository.cart.CartHeaderRepository;
import com.bird.cos.repository.cart.CartItemRepository;
import com.bird.cos.repository.product.ProductRepository;
import com.bird.cos.repository.user.UserCouponRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartHeaderRepository cartHeaderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserCouponRepository userCouponRepository;

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

        Cart cart = cartHeaderRepository.findByUser(user)
                .orElseGet(() -> cartHeaderRepository.save(Cart.of(user)));

        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseGet(() -> CartItem.of(cart, product, 0, null));

        int desired = defaultZero(item.getQuantity()) + requestQty;
        int normalized = normalizeQuantity(desired, product.getStockQuantity());

        item.setQuantity(normalized);
        item.setCart(cart);
        cartItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public CartListResponse getCart(User user) {
        List<CartItem> entities = cartItemRepository.findAllByCart_User(user);
        List<CartItemResponseDto> items = entities.stream()
                .map(this::toDto)
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
        return toDto(item);
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
        int normalized = normalizeQuantity(quantity, product.getStockQuantity());
        item.setQuantity(normalized);
        cartItemRepository.save(item);
    }

    //게스트 카트 병합
    public void mergeGuestCart(List<GuestItem> items, User user) {
        Cart cart = cartHeaderRepository.findByUser(user)
                .orElseGet(() -> cartHeaderRepository.save(Cart.of(user)));
        for (GuestItem g : items) {
            Product product = productRepository.findById(g.productId)
                    .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
            CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                    .orElseGet(() -> CartItem.of(cart, product, 0, g.selectedOptions));
            int desired = defaultZero(item.getQuantity()) + defaultZero(g.quantity);
            int normalized = normalizeQuantity(desired, product.getStockQuantity());
            item.setQuantity(normalized);
            cartItemRepository.save(item);
        }
    }

    public record GuestItem(Long productId, Integer quantity, String selectedOptions) {}

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
    private CartItemResponseDto toDto(CartItem item) {
        Product p = item.getProduct();

        BigDecimal unitOriginal = p.getOriginalPrice();
        BigDecimal unitFinal = PriceCalculator.effectiveUnitPrice(p);
        BigDecimal lineTotal = PriceCalculator.lineTotal(unitFinal, defaultZero(item.getQuantity()));

        boolean outOfStock = p.getStockQuantity() != null && item.getQuantity() != null
                && p.getStockQuantity() < item.getQuantity();

        String status = p.getProductStatusCode() != null ? p.getProductStatusCode().getCodeName() : null;

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
                .build();
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
        if (stockQuantity != null && stockQuantity > 0 && desired > stockQuantity) {
            throw new RuntimeException("재고 부족: 최대 " + stockQuantity + "개까지 담을 수 있습니다.");
        }
        return desired;
    }
}
