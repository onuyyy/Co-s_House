-- Coupon scope migration
USE cos;
ALTER TABLE coupon
    ADD COLUMN scope VARCHAR(20) NOT NULL DEFAULT 'PRODUCT';

ALTER TABLE coupon
    ADD COLUMN brand_id BIGINT NULL;

ALTER TABLE coupon
    MODIFY COLUMN product_id BIGINT NULL;

ALTER TABLE coupon
    ADD CONSTRAINT fk_coupon_brand
        FOREIGN KEY (brand_id) REFERENCES brand(brand_id);
