-- =======================================================================
-- HINCHMART: ORDERS, LOGISTICS, TRACKING & NOTIFICATIONS SCHEMA + SEED DATA
-- =======================================================================

-- 1. ORDERS TABLE
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    address_id BIGINT,
    shipping_address VARCHAR(255),
    subtotal DOUBLE NOT NULL,
    gst_amount DOUBLE NOT NULL,
    delivery_charge DOUBLE NOT NULL,
    total_amount DOUBLE NOT NULL,
    payment_method VARCHAR(255),
    payment_status VARCHAR(255),
    order_status VARCHAR(255) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);

-- 2. ORDER ITEMS TABLE
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    seller_id BIGINT,
    seller VARCHAR(255),
    quantity INT NOT NULL,
    unit_price DOUBLE NOT NULL,
    gst_percentage DOUBLE,
    gst_amount DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    total_price DOUBLE NOT NULL,
    created_at DATETIME,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
);

-- 3. ORDER STATUS HISTORY TABLE
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    changed_by VARCHAR(255),
    remarks VARCHAR(255),
    created_at DATETIME,
    CONSTRAINT fk_order_status_history_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- 4. DELIVERY PARTNERS TABLE
CREATE TABLE IF NOT EXISTS delivery_partners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    partner_name VARCHAR(255) NOT NULL,
    partner_code VARCHAR(255) NOT NULL UNIQUE,
    contact_number VARCHAR(255),
    tracking_url_template VARCHAR(255),
    vehicle_type VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME
);

-- 5. SHIPMENTS TABLE
CREATE TABLE IF NOT EXISTS shipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_number VARCHAR(255) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    delivery_partner_id BIGINT,
    tracking_number VARCHAR(255),
    shipping_mode VARCHAR(255),
    package_weight_kg DOUBLE,
    package_dimensions VARCHAR(255),
    total_packages INT DEFAULT 1,
    status VARCHAR(255) NOT NULL,
    pickup_address VARCHAR(500),
    delivery_address VARCHAR(500),
    pickup_scheduled_at DATETIME,
    estimated_delivery_at DATETIME,
    actual_delivery_at DATETIME,
    driver_name VARCHAR(255),
    driver_contact VARCHAR(255),
    vehicle_number VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_shipments_partner FOREIGN KEY (delivery_partner_id) REFERENCES delivery_partners (id)
);

-- 6. SHIPMENT TRACKING TABLE
CREATE TABLE IF NOT EXISTS shipment_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    description VARCHAR(500),
    latitude DOUBLE,
    longitude DOUBLE,
    event_time DATETIME,
    recorded_by VARCHAR(255),
    CONSTRAINT fk_shipment_tracking_shipment FOREIGN KEY (shipment_id) REFERENCES shipments (id) ON DELETE CASCADE
);

-- 7. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    recipient_role VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    reference_type VARCHAR(255),
    reference_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME,
    created_at DATETIME
);

-- 8. DEVICE TOKENS TABLE
CREATE TABLE IF NOT EXISTS device_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_role VARCHAR(255) NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_type VARCHAR(255),
    device_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at DATETIME,
    created_at DATETIME
);

-- 9. NOTIFICATION PREFERENCES TABLE
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_role VARCHAR(255) NOT NULL,
    notification_type VARCHAR(255) NOT NULL,
    in_app_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME
);

-- =======================================================================
-- INSERT SEED DATA
-- =======================================================================

-- 1. ORDERS
INSERT INTO orders (id, order_number, buyer_id, address_id, shipping_address, subtotal, gst_amount, delivery_charge, total_amount, payment_method, payment_status, order_status, created_at, updated_at)
VALUES 
(1, 'ORD-20260815-1001', 201, 10, 'Site #42, Tech Park Phase 2, Whitefield, Bangalore, Karnataka - 560066', 304000.00, 54720.00, 2500.00, 361220.00, 'NET_BANKING', 'PAID', 'DELIVERED', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY),
(2, 'ORD-20260818-1002', 201, 11, 'Plot #12B, Industrial Area, Peenya 3rd Phase, Bangalore - 560058', 36000.00, 10080.00, 1200.00, 47280.00, 'UPI', 'PAID', 'SHIPPED', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 12 HOUR),
(3, 'ORD-20260819-1003', 202, 15, 'Tower C, Horizon Heights, Electronic City Phase 1, Bangalore - 560100', 11875.00, 2137.50, 800.00, 14812.50, 'CREDIT_CARD', 'PENDING', 'CONFIRMED', NOW() - INTERVAL 6 HOUR, NOW() - INTERVAL 4 HOUR);

-- 2. ORDER ITEMS
INSERT INTO order_items (id, order_id, product_id, seller_id, seller, quantity, unit_price, gst_percentage, gst_amount, subtotal, total_price, created_at)
VALUES 
(1, 1, 3, 102, 'Tata Steel Distributor', 5, 60800.00, 18.0, 54720.00, 304000.00, 358720.00, NOW() - INTERVAL 4 DAY),
(2, 2, 1, 101, 'UltraTech Direct', 100, 360.00, 28.0, 10080.00, 36000.00, 46080.00, NOW() - INTERVAL 2 DAY),
(3, 3, 5, 103, 'Astral Pipe Hub', 25, 475.00, 18.0, 2137.50, 11875.00, 14012.50, NOW() - INTERVAL 6 HOUR);

-- 3. ORDER STATUS HISTORY
INSERT INTO order_status_history (id, order_id, status, changed_by, remarks, created_at)
VALUES 
(1, 1, 'DELIVERED', 'SYSTEM', 'Order delivered to construction site by logistics partner', NOW() - INTERVAL 1 DAY),
(2, 2, 'SHIPPED', 'SELLER', 'Dispatched via VRL Logistics, Docket #VRL-882910', NOW() - INTERVAL 12 HOUR),
(3, 3, 'CONFIRMED', 'SELLER', 'Order confirmed by seller, processing warehouse pickup', NOW() - INTERVAL 4 HOUR);

-- 4. DELIVERY PARTNERS
INSERT INTO delivery_partners (id, partner_name, partner_code, contact_number, tracking_url_template, vehicle_type, active, created_at)
VALUES 
(1, 'BlueDart Express', 'BLUEDART', '+91 1860 233 1234', 'https://track.bluedart.com/track?no={tracking_number}', 'EXPRESS_VAN', TRUE, NOW() - INTERVAL 30 DAY),
(2, 'Delhivery Surface Heavy', 'DELHIVERY', '+91 8069 855 555', 'https://www.delhivery.com/track/package/{tracking_number}', 'CONTAINER_20FT', TRUE, NOW() - INTERVAL 30 DAY),
(3, 'VRL Logistics Bulk Freight', 'VRL', '+91 8362 237 100', 'https://vrlgroup.in/track?cno={tracking_number}', 'HEAVY_TRUCK_32FT', TRUE, NOW() - INTERVAL 30 DAY),
(4, 'Rivigo Heavy Transport', 'RIVIGO', '+91 1800 120 5454', 'https://rivigo.com/track?lr={tracking_number}', 'HEAVY_TRUCK_32FT', TRUE, NOW() - INTERVAL 30 DAY);

-- 5. SHIPMENTS
INSERT INTO shipments (id, shipment_number, order_id, seller_id, delivery_partner_id, tracking_number, shipping_mode, package_weight_kg, package_dimensions, total_packages, status, pickup_address, delivery_address, pickup_scheduled_at, estimated_delivery_at, actual_delivery_at, driver_name, driver_contact, vehicle_number, created_at, updated_at)
VALUES 
(1, 'SHP-20260815-1001', 1, 102, 3, 'TRK-VRL-992144', 'SURFACE_BULK', 5000.0, '600x150x80 cm', 5, 'DELIVERED', 'Tata Steel Yard, Peenya Industrial Area, Bangalore', 'Site #42, Tech Park Phase 2, Whitefield, Bangalore, Karnataka - 560066', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 'Ramesh Gowda', '+91 98450 11223', 'KA-04-E-8821', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 1 DAY),
(2, 'SHP-20260818-1002', 2, 101, 2, 'TRK-DEL-441288', 'SURFACE_BULK', 5000.0, '200x200x120 cm (Palletized)', 100, 'IN_TRANSIT', 'UltraTech Bulk Depot, Nelamangala Hub, Bangalore', 'Plot #12B, Industrial Area, Peenya 3rd Phase, Bangalore - 560058', NOW() - INTERVAL 2 DAY, NOW() + INTERVAL 1 DAY, NULL, 'Sunil Kumar', '+91 97410 55667', 'KA-01-AB-4509', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 12 HOUR),
(3, 'SHP-20260819-1003', 3, 103, 1, 'TRK-BD-773199', 'EXPRESS_LOGISTICS', 125.0, '300x40x40 cm (Bundle)', 25, 'PICKUP_SCHEDULED', 'Astral Pipe Hub, Bommasandra Link Road, Bangalore', 'Tower C, Horizon Heights, Electronic City Phase 1, Bangalore - 560100', NOW() + INTERVAL 2 HOUR, NOW() + INTERVAL 2 DAY, NULL, 'Manjunath B', '+91 99001 33445', 'KA-05-MH-9122', NOW() - INTERVAL 4 HOUR, NOW() - INTERVAL 4 HOUR);

-- 6. SHIPMENT TRACKING
INSERT INTO shipment_tracking (id, shipment_id, status, location, description, latitude, longitude, event_time, recorded_by)
VALUES 
(1, 1, 'PICKUP_SCHEDULED', 'Peenya Industrial Yard, Bangalore', 'Heavy transport vehicle assigned for 5 Tons TMT Steel dispatch', 13.0285, 77.5195, NOW() - INTERVAL 4 DAY, 'SELLER'),
(2, 1, 'IN_TRANSIT', 'Outer Ring Road Terminal, Mahadevapura', 'Consignment in transit towards Whitefield construction zone', 12.9912, 77.7011, NOW() - INTERVAL 2 DAY, 'DRIVER'),
(3, 1, 'DELIVERED', 'Site #42, Tech Park Phase 2, Whitefield', 'Successfully unloaded and handed over to Site Engineer with POD signature', 12.9698, 77.7499, NOW() - INTERVAL 1 DAY, 'DRIVER'),
(4, 2, 'PICKUP_SCHEDULED', 'UltraTech Depot, Nelamangala Hub', '100 cement bags loaded on 2 pallets and verified', 13.0983, 77.3891, NOW() - INTERVAL 2 DAY, 'SELLER'),
(5, 2, 'IN_TRANSIT', 'Yeshwanthpur Junction Transit Hub', 'Shipment cleared transit checkpoint, heading to Peenya', 13.0234, 77.5489, NOW() - INTERVAL 12 HOUR, 'DELIVERY_PARTNER'),
(6, 3, 'PICKUP_SCHEDULED', 'Astral Bommasandra Warehouse', 'Bundle packed with protective strapping, awaiting courier pickup', 12.8182, 77.6890, NOW() - INTERVAL 4 HOUR, 'SELLER');

-- 7. NOTIFICATIONS
INSERT INTO notifications (id, recipient_id, recipient_role, type, title, message, reference_type, reference_id, is_read, read_at, created_at)
VALUES 
(1, 201, 'BUYER', 'ORDER_PLACED', 'Order Placed Successfully: ORD-20260815-1001', 'Your order of ₹361,220.00 for Tata Tiscon TMT Steel has been placed.', 'ORDER', 1, TRUE, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(2, 201, 'BUYER', 'ORDER_DELIVERED', 'Order Delivered: ORD-20260815-1001', 'Your order has been successfully delivered to Site #42, Whitefield.', 'ORDER', 1, TRUE, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(3, 102, 'SELLER', 'ORDER_PLACED', 'New Order Received: ORD-20260815-1001', 'You received an order for 5 Tons Tata Tiscon 550D TMT Rebar (₹304,000.00).', 'ORDER', 1, TRUE, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
(4, 201, 'BUYER', 'ORDER_SHIPPED', 'Shipment In Transit: SHP-20260818-1002', 'Your cement order #ORD-20260818-1002 is in transit via Delhivery Surface. Tracking: TRK-DEL-441288', 'SHIPMENT', 2, FALSE, NULL, NOW() - INTERVAL 12 HOUR),
(5, 202, 'BUYER', 'ORDER_CONFIRMED', 'Order Confirmed: ORD-20260819-1003', 'Your Astral Pipe order has been confirmed and shipment scheduled.', 'ORDER', 3, FALSE, NULL, NOW() - INTERVAL 4 HOUR),
(6, 101, 'SELLER', 'LOW_STOCK', 'Low Stock Alert: UltraTech 53 OPC Cement', 'Current inventory for SKU HN-CMT-UT-53OPC has dropped below 150 bags.', 'PRODUCT', 1, FALSE, NULL, NOW() - INTERVAL 2 HOUR);

-- 8. DEVICE TOKENS
INSERT INTO device_tokens (id, user_id, user_role, token, device_type, device_id, is_active, last_used_at, created_at)
VALUES 
(1, 201, 'BUYER', 'fcm_token_buyer_builder_pixel8_alpha778899', 'ANDROID', 'pixel_8_pro_001', TRUE, NOW() - INTERVAL 1 HOUR, NOW() - INTERVAL 15 DAY),
(2, 201, 'BUYER', 'web_push_token_buyer_builder_chrome_998811', 'WEB', 'chrome_mac_002', TRUE, NOW() - INTERVAL 2 HOUR, NOW() - INTERVAL 10 DAY),
(3, 102, 'SELLER', 'apns_token_seller_tatasteel_iphone15_223344', 'IOS', 'iphone_15_pro_003', TRUE, NOW() - INTERVAL 30 MINUTE, NOW() - INTERVAL 8 DAY);

-- 9. NOTIFICATION PREFERENCES
INSERT INTO notification_preferences (id, user_id, user_role, notification_type, in_app_enabled, email_enabled, push_enabled, sms_enabled, created_at, updated_at)
VALUES 
(1, 201, 'BUYER', 'ORDER_UPDATES', TRUE, TRUE, TRUE, TRUE, NOW() - INTERVAL 15 DAY, NOW()),
(2, 201, 'BUYER', 'RFQ_UPDATES', TRUE, TRUE, TRUE, FALSE, NOW() - INTERVAL 15 DAY, NOW()),
(3, 102, 'SELLER', 'ORDER_UPDATES', TRUE, TRUE, TRUE, TRUE, NOW() - INTERVAL 8 DAY, NOW());
