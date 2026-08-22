package com.example.hinchmart.config;

import com.example.hinchmart.entity.*;
import com.example.hinchmart.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final SellerDocumentRepository sellerDocumentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductBulkPriceRepository productBulkPriceRepository;
    private final InventoryRepository inventoryRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingRepository shipmentTrackingRepository;
    private final NotificationRepository notificationRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public DataInitializer(CategoryRepository categoryRepository,
                           SubCategoryRepository subCategoryRepository,
                           BrandRepository brandRepository,
                           ProductRepository productRepository,
                           SellerDocumentRepository sellerDocumentRepository,
                           ActivityLogRepository activityLogRepository,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductBulkPriceRepository productBulkPriceRepository,
                           InventoryRepository inventoryRepository,
                           DeliveryPartnerRepository deliveryPartnerRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           OrderStatusHistoryRepository orderStatusHistoryRepository,
                           ShipmentRepository shipmentRepository,
                           ShipmentTrackingRepository shipmentTrackingRepository,
                           NotificationRepository notificationRepository,
                           DeviceTokenRepository deviceTokenRepository,
                           NotificationPreferenceRepository notificationPreferenceRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.sellerDocumentRepository = sellerDocumentRepository;
        this.activityLogRepository = activityLogRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productBulkPriceRepository = productBulkPriceRepository;
        this.inventoryRepository = inventoryRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderStatusHistoryRepository = orderStatusHistoryRepository;
        this.shipmentRepository = shipmentRepository;
        this.shipmentTrackingRepository = shipmentTrackingRepository;
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Override
    public void run(String... args) {
        seedCategoriesAndBrands();
        seedSubCategories();
        seedProducts();
        seedProductBulkPricesAndInventory();
        seedSellerDocuments();
        seedActivityLogs();
        seedCartsAndCartItems();
        seedDeliveryPartners();
        seedOrdersAndShipments();
        seedNotificationsAndPreferences();
    }

    private void seedCategoriesAndBrands() {
        // Seed Categories if missing
        seedCategoryIfMissing("Cement & Concrete", "Cement, concrete and construction materials", "cement.jpg");
        seedCategoryIfMissing("Steel Rods & Rebars", "TMT bars, steel rods and reinforcement materials", "steel.jpg");
        seedCategoryIfMissing("Pipes & Fittings", "Pipes, fittings and plumbing materials", "pipes.jpg");
        seedCategoryIfMissing("Electrical & Cables", "Electrical cables, wires and accessories", "electrical.jpg");
        seedCategoryIfMissing("Power Tools & Machinery", "Power tools, machinery and equipment", "tools.jpg");
        seedCategoryIfMissing("Tiles & Flooring", "Tiles, flooring and related products", "tiles.jpg");
        seedCategoryIfMissing("Paints & Waterproofing", "Paints, coatings and waterproofing products", "paints.jpg");
        seedCategoryIfMissing("Safety Equipment", "Safety equipment and protective products", "safety.jpg");

        // Seed Brands if missing
        seedBrandIfMissing("UltraTech", "India's No. 1 Cement brand", "ultratech.png");
        seedBrandIfMissing("Tata Tiscon", "Superior quality TMT rebars", "tatatiscon.png");
        seedBrandIfMissing("Astral", "Pioneers in CPVC piping systems", "astral.png");
        seedBrandIfMissing("Finolex", "Premium wires, cables and pipes", "finolex.png");
        seedBrandIfMissing("Bosch", "Professional power tools and accessories", "bosch.png");
        seedBrandIfMissing("Havells", "Leading electrical equipment manufacturer", "havells.png");
        seedBrandIfMissing("Asian Paints", "Decorative and industrial coatings", "asianpaints.png");
        seedBrandIfMissing("3M Safety", "Personal safety and protective gear", "3m.png");
        seedBrandIfMissing("Kajaria", "India's largest ceramic tile manufacturer", "kajaria.png");
    }

    private void seedCategoryIfMissing(String name, String desc, String img) {
        if (categoryRepository.findByName(name).isEmpty()) {
            Category cat = new Category();
            cat.setName(name);
            cat.setDescription(desc);
            cat.setImage(img);
            cat.setActive("Y");
            categoryRepository.save(cat);
        }
    }

    private void seedBrandIfMissing(String name, String desc, String logo) {
        if (brandRepository.findByName(name).isEmpty()) {
            Brand brand = new Brand();
            brand.setName(name);
            brand.setDescription(desc);
            brand.setLogo(logo);
            brand.setActive("Y");
            brandRepository.save(brand);
        }
    }

    private void seedSubCategories() {
        if (subCategoryRepository.count() > 0) {
            return;
        }

        Category cement = categoryRepository.findByName("Cement & Concrete").orElse(null);
        Category steel = categoryRepository.findByName("Steel Rods & Rebars").orElse(null);
        Category pipes = categoryRepository.findByName("Pipes & Fittings").orElse(null);
        Category electrical = categoryRepository.findByName("Electrical & Cables").orElse(null);
        Category powerTools = categoryRepository.findByName("Power Tools & Machinery").orElse(null);
        Category tiles = categoryRepository.findByName("Tiles & Flooring").orElse(null);
        Category paints = categoryRepository.findByName("Paints & Waterproofing").orElse(null);
        Category safety = categoryRepository.findByName("Safety Equipment").orElse(null);

        List<SubCategory> subCategories = Arrays.asList(
                createSubCat("OPC 53 Grade Cement", "Ordinary Portland Cement 53 Grade for heavy construction", cement),
                createSubCat("PPC Cement", "Portland Pozzolana Cement for plastering and general masonry", cement),
                createSubCat("White Cement & Wall Putty", "White cement and skim coats for smooth wall finishes", cement),
                createSubCat("Ready Mix Concrete", "Pre-mixed high strength concrete", cement),
                createSubCat("Fe 550D TMT Rebars", "High ductility seismic resistant TMT steel bars", steel),
                createSubCat("MS Binding Wire", "Mild steel binding wire for bar bending", steel),
                createSubCat("Structural Steel Beams & Angles", "I-Beams, MS Channels, and Equal Angles", steel),
                createSubCat("CPVC Plumbing Pipes", "Hot and cold water CPVC pipes & fittings", pipes),
                createSubCat("UPVC Drainage Pipes", "Heavy-duty underground and drainage pipes", pipes),
                createSubCat("PVC Conduit Pipes", "Electrical wiring rigid conduit pipes", pipes),
                createSubCat("Copper House Wires", "FR & FRLS flame retardant house wiring cables", electrical),
                createSubCat("Industrial MCB & DBs", "Circuit breakers, distribution boards, and isolators", electrical),
                createSubCat("Commercial LED Lighting", "LED batten lights, flood lights, and high-bays", electrical),
                createSubCat("Rotary Hammers & Drills", "Heavy duty rotary hammer drills and impact drivers", powerTools),
                createSubCat("Angle Grinders & Cutters", "4-inch & 9-inch angle grinders and marble cutters", powerTools),
                createSubCat("Cordless Power Tools", "18V Lithium-ion cordless drill sets and wrenches", powerTools),
                createSubCat("Vitrified Floor Tiles", "Double charged and glazed vitrified floor tiles (600x600, 600x1200)", tiles),
                createSubCat("Ceramic Wall Tiles", "Glossy and matte finish digital ceramic wall tiles", tiles),
                createSubCat("Exterior Emulsion Paints", "Weatherproof exterior architectural paints", paints),
                createSubCat("Waterproofing Compounds", "Liquid water repellent and damp-proof membranes", paints),
                createSubCat("Industrial Safety Helmets", "EN397 certified industrial hard hats with ratchet harness", safety),
                createSubCat("Safety Shoes & Footwear", "Steel toe puncture resistant leather safety boots", safety),
                createSubCat("Safety Harness & Fall Protection", "Full body safety harness with lanyard and shock absorber", safety),
                createSubCat("High-Visibility Safety Vests", "Reflective neon construction vests", safety)
        );

        subCategoryRepository.saveAll(subCategories);
    }

    private SubCategory createSubCat(String name, String desc, Category cat) {
        SubCategory sub = new SubCategory();
        sub.setName(name);
        sub.setDescription(desc);
        sub.setActive("Y");
        sub.setCategory(cat);
        return sub;
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            return;
        }

        Category cement = categoryRepository.findByName("Cement & Concrete").orElse(null);
        Category steel = categoryRepository.findByName("Steel Rods & Rebars").orElse(null);
        Category pipes = categoryRepository.findByName("Pipes & Fittings").orElse(null);
        Category electrical = categoryRepository.findByName("Electrical & Cables").orElse(null);
        Category powerTools = categoryRepository.findByName("Power Tools & Machinery").orElse(null);
        Category tiles = categoryRepository.findByName("Tiles & Flooring").orElse(null);
        Category paints = categoryRepository.findByName("Paints & Waterproofing").orElse(null);
        Category safety = categoryRepository.findByName("Safety Equipment").orElse(null);

        Brand ultraTech = brandRepository.findByName("UltraTech").orElse(null);
        Brand tataTiscon = brandRepository.findByName("Tata Tiscon").orElse(null);
        Brand astral = brandRepository.findByName("Astral").orElse(null);
        Brand finolex = brandRepository.findByName("Finolex").orElse(null);
        Brand bosch = brandRepository.findByName("Bosch").orElse(null);
        Brand havells = brandRepository.findByName("Havells").orElse(null);
        Brand asianPaints = brandRepository.findByName("Asian Paints").orElse(null);
        Brand safety3M = brandRepository.findByName("3M Safety").orElse(null);
        Brand kajaria = brandRepository.findByName("Kajaria").orElse(null);

        SubCategory opcCement = subCategoryRepository.findByName("OPC 53 Grade Cement").orElse(null);
        SubCategory ppcCement = subCategoryRepository.findByName("PPC Cement").orElse(null);
        SubCategory tmtRebars = subCategoryRepository.findByName("Fe 550D TMT Rebars").orElse(null);
        SubCategory cpvcPipes = subCategoryRepository.findByName("CPVC Plumbing Pipes").orElse(null);
        SubCategory copperWires = subCategoryRepository.findByName("Copper House Wires").orElse(null);
        SubCategory rotaryDrills = subCategoryRepository.findByName("Rotary Hammers & Drills").orElse(null);
        SubCategory grinders = subCategoryRepository.findByName("Angle Grinders & Cutters").orElse(null);
        SubCategory vitrifiedTiles = subCategoryRepository.findByName("Vitrified Floor Tiles").orElse(null);
        SubCategory extPaints = subCategoryRepository.findByName("Exterior Emulsion Paints").orElse(null);
        SubCategory waterProofing = subCategoryRepository.findByName("Waterproofing Compounds").orElse(null);
        SubCategory helmets = subCategoryRepository.findByName("Industrial Safety Helmets").orElse(null);
        SubCategory safetyShoes = subCategoryRepository.findByName("Safety Shoes & Footwear").orElse(null);

        List<Product> products = Arrays.asList(
                // Product 1
                buildProduct("UltraTech 53 Grade OPC Cement (50kg Bag)", "UltraTech Direct", cement, opcCement, ultraTech,
                        "HN-CEM-OPC53-50K", "252329", 28.0, 50, "BAGS", 420.0, 375.0, 5000, 2, "APPROVED"),

                // Product 2
                buildProduct("UltraTech Super PPC Weather Plus Cement (50kg)", "UltraTech Direct", cement, ppcCement, ultraTech,
                        "HN-CEM-PPC-50K", "252329", 28.0, 50, "BAGS", 400.0, 355.0, 4500, 2, "APPROVED"),

                // Product 3
                buildProduct("Tata Tiscon 550D TMT Rebar (12mm x 12m)", "Tata Steel Distributor", steel, tmtRebars, tataTiscon,
                        "HN-STL-TATA-12MM", "721420", 18.0, 5, "TONNES", 68000.0, 62500.0, 120, 3, "APPROVED"),

                // Product 4
                buildProduct("Tata Tiscon 550D TMT Rebar (16mm x 12m)", "Tata Steel Distributor", steel, tmtRebars, tataTiscon,
                        "HN-STL-TATA-16MM", "721420", 18.0, 5, "TONNES", 68000.0, 62000.0, 85, 3, "APPROVED"),

                // Product 5
                buildProduct("Astral CPVC Pro SDR 11 Pipe (1 inch, 3m)", "Astral Pipe Hub", pipes, cpvcPipes, astral,
                        "HN-PIP-AST-1IN-3M", "391740", 18.0, 20, "PIECES", 580.0, 475.0, 800, 1, "APPROVED"),

                // Product 6
                buildProduct("Finolex 2.5 sq mm FR Copper Wire (90m Box, Red)", "Finolex Cables Ltd", electrical, copperWires, finolex,
                        "HN-ELE-FIN-25RD-90M", "854449", 18.0, 10, "BOXES", 2850.0, 2350.0, 350, 1, "APPROVED"),

                // Product 7
                buildProduct("Havells 4 sq mm Flame Retardant Wire (90m, Blue)", "Havells India", electrical, copperWires, havells,
                        "HN-ELE-HAV-40BL-90M", "854449", 18.0, 10, "BOXES", 4400.0, 3650.0, 200, 2, "APPROVED"),

                // Product 8
                buildProduct("Bosch GBH 2-26 DRE Professional Rotary Hammer Drill (800W)", "Bosch Power Store", powerTools, rotaryDrills, bosch,
                        "HN-TLS-BSH-GBH226", "846721", 18.0, 2, "PIECES", 9800.0, 7850.0, 45, 2, "APPROVED"),

                // Product 9
                buildProduct("Bosch GWS 600 Professional 4-Inch Angle Grinder (670W)", "Bosch Power Store", powerTools, grinders, bosch,
                        "HN-TLS-BSH-GWS600", "846729", 18.0, 5, "PIECES", 2600.0, 1999.0, 110, 1, "APPROVED"),

                // Product 10
                buildProduct("Kajaria Glazed Vitrified Floor Tiles (600x1200mm, Statuario Marble Look)", "Kajaria Ceramics Direct", tiles, vitrifiedTiles, kajaria,
                        "HN-TIL-KAJ-6012-STAT", "690721", 18.0, 25, "BOXES", 1450.0, 1180.0, 600, 3, "APPROVED"),

                // Product 11
                buildProduct("Asian Paints Apex Ultima Weatherproof Exterior Emulsion (20L)", "Asian Paints Depot", paints, extPaints, asianPaints,
                        "HN-PNT-AP-ULT-20L", "320890", 18.0, 4, "BUCKETS", 7200.0, 5950.0, 150, 2, "APPROVED"),

                // Product 12
                buildProduct("Asian Paints SmartCare Damp Block 2K Waterproofing (15kg)", "Asian Paints Depot", paints, waterProofing, asianPaints,
                        "HN-PNT-AP-DAMP2K-15K", "382440", 18.0, 5, "PACKS", 3100.0, 2480.0, 180, 2, "APPROVED"),

                // Product 13
                buildProduct("3M H-700 Series Industrial Safety Helmet with 4-Point Ratchet (White)", "3M Safety Direct", safety, helmets, safety3M,
                        "HN-SFT-3M-H700-WH", "650610", 18.0, 20, "PIECES", 550.0, 420.0, 750, 1, "APPROVED"),

                // Product 14
                buildProduct("3M Steel Toe Puncture Resistant Safety Shoes (Size 9, Black)", "3M Safety Direct", safety, safetyShoes, safety3M,
                        "HN-SFT-3M-SH-SZ9", "640340", 18.0, 10, "PAIRS", 2400.0, 1850.0, 220, 2, "APPROVED")
        );

        productRepository.saveAll(products);
    }

    private Product buildProduct(String name, String seller, Category cat, SubCategory subCat, Brand brand,
                                 String sku, String hsn, Double gst, Integer moq, String unit,
                                 Double mrp, Double sellingPrice, Integer stock, Integer deliveryDays, String status) {
        Product p = new Product();
        p.setProductName(name);
        p.setSeller(seller);
        p.setCategory(cat);
        p.setSubcategory(subCat);
        p.setBrand(brand);
        p.setSku(sku);
        p.setHsn(hsn);
        p.setGstPercentage(gst);
        p.setMoq(moq);
        p.setUnit(unit);
        p.setMrp(mrp);
        p.setSellingPrice(sellingPrice);
        p.setStock(stock);
        p.setDeliveryDays(deliveryDays);
        p.setApprovalStatus(status);
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    private void seedProductBulkPricesAndInventory() {
        if (productBulkPriceRepository.count() > 0) {
            return;
        }

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return;

        // Find Steel Rebar Product for B2B Bulk Tier example
        Product steelProduct = products.stream()
                .filter(p -> p.getProductName().contains("Tata Tiscon 550D TMT Rebar (12mm"))
                .findFirst().orElse(products.get(0));

        // Find Cement Product
        Product cementProduct = products.stream()
                .filter(p -> p.getProductName().contains("UltraTech 53 Grade OPC Cement"))
                .findFirst().orElse(products.get(0));

        // 3 Bulk Pricing tiers for Steel
        ProductBulkPrice steelTier1 = new ProductBulkPrice(null, 1, 4, 61500.0, steelProduct);
        ProductBulkPrice steelTier2 = new ProductBulkPrice(null, 5, 9, 60800.0, steelProduct);
        ProductBulkPrice steelTier3 = new ProductBulkPrice(null, 10, null, 59900.0, steelProduct);

        // 3 Bulk Pricing tiers for Cement
        ProductBulkPrice cementTier1 = new ProductBulkPrice(null, 50, 99, 375.0, cementProduct);
        ProductBulkPrice cementTier2 = new ProductBulkPrice(null, 100, 499, 360.0, cementProduct);
        ProductBulkPrice cementTier3 = new ProductBulkPrice(null, 500, null, 345.0, cementProduct);

        productBulkPriceRepository.saveAll(Arrays.asList(steelTier1, steelTier2, steelTier3, cementTier1, cementTier2, cementTier3));

        // Seed 3 Inventory stock records
        if (inventoryRepository.count() == 0) {
            Inventory inv1 = new Inventory(null, 120, 10, 15, steelProduct);
            Inventory inv2 = new Inventory(null, 5000, 200, 500, cementProduct);
            Product pipesProduct = products.size() > 4 ? products.get(4) : steelProduct;
            Inventory inv3 = new Inventory(null, 800, 50, 100, pipesProduct);
            inventoryRepository.saveAll(Arrays.asList(inv1, inv2, inv3));
        }
    }

    private void seedSellerDocuments() {
        if (sellerDocumentRepository.count() > 0) {
            return;
        }

        // Row 1: Approved GST Document
        SellerDocument doc1 = new SellerDocument();
        doc1.setSellerId(101L);
        doc1.setSeller("UltraTech Direct");
        doc1.setDocumentType("GST_CERTIFICATE");
        doc1.setDocumentNumber("GSTIN27AAACU1234F1Z5");
        doc1.setDocumentUrl("https://storage.hinchmart.com/documents/ultratech-gst.pdf");
        doc1.setFileName("ultratech_gst_registration.pdf");
        doc1.setFileType("application/pdf");
        doc1.setFileSize(245820L);
        doc1.setStatus("APPROVED");
        doc1.setVerifiedBy("Admin_Rajesh");
        doc1.setVerifiedAt(LocalDateTime.now().minusDays(10));
        doc1.setCreatedAt(LocalDateTime.now().minusDays(12));

        // Row 2: Approved PAN Document
        SellerDocument doc2 = new SellerDocument();
        doc2.setSellerId(102L);
        doc2.setSeller("Tata Steel Distributor");
        doc2.setDocumentType("PAN_CARD");
        doc2.setDocumentNumber("AAACT8821B");
        doc2.setDocumentUrl("https://storage.hinchmart.com/documents/tatasteel-pan.pdf");
        doc2.setFileName("tatasteel_corp_pan.pdf");
        doc2.setFileType("application/pdf");
        doc2.setFileSize(182040L);
        doc2.setStatus("APPROVED");
        doc2.setVerifiedBy("Admin_Pooja");
        doc2.setVerifiedAt(LocalDateTime.now().minusDays(8));
        doc2.setCreatedAt(LocalDateTime.now().minusDays(9));

        // Row 3: Pending Trade License Document
        SellerDocument doc3 = new SellerDocument();
        doc3.setSellerId(103L);
        doc3.setSeller("Astral Pipe Hub");
        doc3.setDocumentType("TRADE_LICENSE");
        doc3.setDocumentNumber("TL-MUM-2026-9812");
        doc3.setDocumentUrl("https://storage.hinchmart.com/documents/astral-tradelicense.pdf");
        doc3.setFileName("astral_trade_license_2026.pdf");
        doc3.setFileType("application/pdf");
        doc3.setFileSize(312500L);
        doc3.setStatus("PENDING");
        doc3.setCreatedAt(LocalDateTime.now().minusDays(1));

        sellerDocumentRepository.saveAll(Arrays.asList(doc1, doc2, doc3));
    }

    private void seedActivityLogs() {
        if (activityLogRepository.count() > 0) {
            return;
        }

        // Row 1: Seller Login Log
        ActivityLog log1 = new ActivityLog();
        log1.setUserId(101L);
        log1.setUserName("seller_ultratech");
        log1.setUserRole("SELLER");
        log1.setAction("LOGIN");
        log1.setEntityType("AUTH");
        log1.setEntityId(101L);
        log1.setDescription("Seller successfully authenticated from portal");
        log1.setDetails("{\"ip\": \"192.168.1.45\", \"device\": \"Chrome / Windows 11\"}");
        log1.setIpAddress("192.168.1.45");
        log1.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/122.0");
        log1.setStatus("SUCCESS");
        log1.setCreatedAt(LocalDateTime.now().minusHours(4));

        // Row 2: Admin Product Approval Log
        ActivityLog log2 = new ActivityLog();
        log2.setUserId(1L);
        log2.setUserName("admin_rajesh");
        log2.setUserRole("ADMIN");
        log2.setAction("APPROVE_PRODUCT");
        log2.setEntityType("PRODUCT");
        log2.setEntityId(3L);
        log2.setDescription("Approved product listing: Tata Tiscon 550D TMT Rebar");
        log2.setDetails("{\"sku\": \"HN-STL-TATA-12MM\", \"approvalStatus\": \"APPROVED\"}");
        log2.setIpAddress("192.168.1.10");
        log2.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)");
        log2.setStatus("SUCCESS");
        log2.setCreatedAt(LocalDateTime.now().minusHours(2));

        // Row 3: Buyer Add to Cart Log
        ActivityLog log3 = new ActivityLog();
        log3.setUserId(201L);
        log3.setUserName("buyer_builder_infra");
        log3.setUserRole("BUYER");
        log3.setAction("ADD_TO_CART");
        log3.setEntityType("CART");
        log3.setEntityId(1L);
        log3.setDescription("Buyer added 5 Tons of Tata Tiscon 550D TMT Rebar with bulk pricing tier 2");
        log3.setDetails("{\"productId\": 3, \"quantity\": 5, \"tierUnitPrice\": 60800.0, \"subtotal\": 304000.0}");
        log3.setIpAddress("192.168.1.88");
        log3.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X)");
        log3.setStatus("SUCCESS");
        log3.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        activityLogRepository.saveAll(Arrays.asList(log1, log2, log3));
    }

    private void seedCartsAndCartItems() {
        if (cartRepository.count() > 0) {
            return;
        }

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return;

        // Create Cart for Buyer 1
        Cart cart = new Cart();
        cart.setBuyerId(1L);
        cart.setCreatedAt(LocalDateTime.now().minusHours(1));
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        Product steel = products.stream()
                .filter(p -> p.getProductName().contains("Tata Tiscon 550D TMT Rebar (12mm"))
                .findFirst().orElse(products.get(0));

        Product cement = products.stream()
                .filter(p -> p.getProductName().contains("UltraTech 53 Grade OPC Cement"))
                .findFirst().orElse(products.get(0));

        Product pipes = products.size() > 4 ? products.get(4) : steel;

        // Row 1: CartItem 1 - 5 Tons of Steel with Tier 2 pricing (₹60,800/Ton)
        CartItem item1 = new CartItem();
        item1.setCart(cart);
        item1.setProduct(steel);
        item1.setSeller(steel.getSeller());
        item1.setSellerId(102L);
        item1.setQuantity(5);
        item1.setUnitPrice(60800.0);
        item1.setSubtotal(304000.0); // 5 * 60,800
        item1.setGstPercentage(18.0);
        item1.setCreatedAt(LocalDateTime.now().minusMinutes(40));

        // Row 2: CartItem 2 - 100 Bags of Cement with Tier 2 pricing (₹360/Bag)
        CartItem item2 = new CartItem();
        item2.setCart(cart);
        item2.setProduct(cement);
        item2.setSeller(cement.getSeller());
        item2.setSellerId(101L);
        item2.setQuantity(100);
        item2.setUnitPrice(360.0);
        item2.setSubtotal(36000.0); // 100 * 360
        item2.setGstPercentage(28.0);
        item2.setCreatedAt(LocalDateTime.now().minusMinutes(30));

        // Row 3: CartItem 3 - 25 Pieces of Astral CPVC Pipes (₹475/Piece)
        CartItem item3 = new CartItem();
        item3.setCart(cart);
        item3.setProduct(pipes);
        item3.setSeller(pipes.getSeller());
        item3.setSellerId(103L);
        item3.setQuantity(25);
        item3.setUnitPrice(475.0);
        item3.setSubtotal(11875.0); // 25 * 475
        item3.setGstPercentage(18.0);
        item3.setCreatedAt(LocalDateTime.now().minusMinutes(20));

        cartItemRepository.saveAll(Arrays.asList(item1, item2, item3));
    }

    private void seedDeliveryPartners() {
        if (deliveryPartnerRepository.count() > 0) {
            return;
        }

        DeliveryPartner p1 = new DeliveryPartner(null, "BlueDart Express", "BLUEDART", "+91 1860 233 1234", "https://track.bluedart.com/track?no={tracking_number}", "EXPRESS_VAN", true, LocalDateTime.now().minusDays(30));
        DeliveryPartner p2 = new DeliveryPartner(null, "Delhivery Surface Heavy", "DELHIVERY", "+91 8069 855 555", "https://www.delhivery.com/track/package/{tracking_number}", "CONTAINER_20FT", true, LocalDateTime.now().minusDays(30));
        DeliveryPartner p3 = new DeliveryPartner(null, "VRL Logistics Bulk Freight", "VRL", "+91 8362 237 100", "https://vrlgroup.in/track?cno={tracking_number}", "HEAVY_TRUCK_32FT", true, LocalDateTime.now().minusDays(30));
        DeliveryPartner p4 = new DeliveryPartner(null, "Rivigo Heavy Transport", "RIVIGO", "+91 1800 120 5454", "https://rivigo.com/track?lr={tracking_number}", "HEAVY_TRUCK_32FT", true, LocalDateTime.now().minusDays(30));

        deliveryPartnerRepository.saveAll(Arrays.asList(p1, p2, p3, p4));
    }

    private void seedOrdersAndShipments() {
        if (orderRepository.count() > 0) {
            return;
        }

        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return;

        Product steel = products.stream().filter(p -> p.getProductName().contains("Tata Tiscon")).findFirst().orElse(products.get(0));
        Product cement = products.stream().filter(p -> p.getProductName().contains("UltraTech")).findFirst().orElse(products.get(0));
        Product pipes = products.size() > 4 ? products.get(4) : steel;

        DeliveryPartner vrl = deliveryPartnerRepository.findByPartnerCode("VRL").orElse(null);
        DeliveryPartner delhivery = deliveryPartnerRepository.findByPartnerCode("DELHIVERY").orElse(null);
        DeliveryPartner bluedart = deliveryPartnerRepository.findByPartnerCode("BLUEDART").orElse(null);

        // -------------------------------------------------------------
        // Order 1: Delivered TMT Steel Rebar Order
        // -------------------------------------------------------------
        Order order1 = new Order();
        order1.setOrderNumber("ORD-20260815-1001");
        order1.setBuyerId(201L);
        order1.setAddressId(10L);
        order1.setShippingAddress("Site #42, Tech Park Phase 2, Whitefield, Bangalore, Karnataka - 560066");
        order1.setSubtotal(304000.0);
        order1.setGstAmount(54720.0);
        order1.setDeliveryCharge(2500.0);
        order1.setTotalAmount(361220.0);
        order1.setPaymentMethod("NET_BANKING");
        order1.setPaymentStatus("PAID");
        order1.setOrderStatus("DELIVERED");
        order1.setCreatedAt(LocalDateTime.now().minusDays(4));
        order1.setUpdatedAt(LocalDateTime.now().minusDays(1));

        OrderItem item1 = new OrderItem();
        item1.setOrder(order1);
        item1.setProduct(steel);
        item1.setSellerId(102L);
        item1.setSeller("Tata Steel Distributor");
        item1.setQuantity(5);
        item1.setUnitPrice(60800.0);
        item1.setGstPercentage(18.0);
        item1.setGstAmount(54720.0);
        item1.setSubtotal(304000.0);
        item1.setTotalPrice(358720.0);
        item1.setCreatedAt(LocalDateTime.now().minusDays(4));
        order1.getItems().add(item1);

        OrderStatusHistory h1 = new OrderStatusHistory(null, order1, "DELIVERED", "SYSTEM", "Order delivered to construction site by logistics partner", LocalDateTime.now().minusDays(1));
        order1.getStatusHistory().add(h1);

        Order savedOrder1 = orderRepository.save(order1);

        // Shipment 1 for Order 1
        Shipment shp1 = new Shipment();
        shp1.setShipmentNumber("SHP-20260815-1001");
        shp1.setOrder(savedOrder1);
        shp1.setSellerId(102L);
        shp1.setDeliveryPartner(vrl);
        shp1.setTrackingNumber("TRK-VRL-992144");
        shp1.setShippingMode("SURFACE_BULK");
        shp1.setPackageWeightKg(5000.0);
        shp1.setPackageDimensions("600x150x80 cm");
        shp1.setTotalPackages(5);
        shp1.setStatus("DELIVERED");
        shp1.setPickupAddress("Tata Steel Yard, Peenya Industrial Area, Bangalore");
        shp1.setDeliveryAddress(savedOrder1.getShippingAddress());
        shp1.setPickupScheduledAt(LocalDateTime.now().minusDays(4));
        shp1.setEstimatedDeliveryAt(LocalDateTime.now().minusDays(1));
        shp1.setActualDeliveryAt(LocalDateTime.now().minusDays(1));
        shp1.setDriverName("Ramesh Gowda");
        shp1.setDriverContact("+91 98450 11223");
        shp1.setVehicleNumber("KA-04-E-8821");
        shp1.setCreatedAt(LocalDateTime.now().minusDays(4));
        shp1.setUpdatedAt(LocalDateTime.now().minusDays(1));

        Shipment savedShp1 = shipmentRepository.save(shp1);

        ShipmentTracking trk1a = new ShipmentTracking(null, savedShp1, "PICKUP_SCHEDULED", "Peenya Industrial Yard, Bangalore", "Heavy transport vehicle assigned for 5 Tons TMT Steel dispatch", 13.0285, 77.5195, LocalDateTime.now().minusDays(4), "SELLER");
        ShipmentTracking trk1b = new ShipmentTracking(null, savedShp1, "IN_TRANSIT", "Outer Ring Road Terminal, Mahadevapura", "Consignment in transit towards Whitefield construction zone", 12.9912, 77.7011, LocalDateTime.now().minusDays(2), "DRIVER");
        ShipmentTracking trk1c = new ShipmentTracking(null, savedShp1, "DELIVERED", "Site #42, Tech Park Phase 2, Whitefield", "Successfully unloaded and handed over to Site Engineer with POD signature", 12.9698, 77.7499, LocalDateTime.now().minusDays(1), "DRIVER");
        shipmentTrackingRepository.saveAll(Arrays.asList(trk1a, trk1b, trk1c));

        // -------------------------------------------------------------
        // Order 2: In-Transit Cement Order
        // -------------------------------------------------------------
        Order order2 = new Order();
        order2.setOrderNumber("ORD-20260818-1002");
        order2.setBuyerId(201L);
        order2.setAddressId(11L);
        order2.setShippingAddress("Plot #12B, Industrial Area, Peenya 3rd Phase, Bangalore - 560058");
        order2.setSubtotal(36000.0);
        order2.setGstAmount(10080.0);
        order2.setDeliveryCharge(1200.0);
        order2.setTotalAmount(47280.0);
        order2.setPaymentMethod("UPI");
        order2.setPaymentStatus("PAID");
        order2.setOrderStatus("SHIPPED");
        order2.setCreatedAt(LocalDateTime.now().minusDays(2));
        order2.setUpdatedAt(LocalDateTime.now().minusHours(12));

        OrderItem item2 = new OrderItem();
        item2.setOrder(order2);
        item2.setProduct(cement);
        item2.setSellerId(101L);
        item2.setSeller("UltraTech Direct");
        item2.setQuantity(100);
        item2.setUnitPrice(360.0);
        item2.setGstPercentage(28.0);
        item2.setGstAmount(10080.0);
        item2.setSubtotal(36000.0);
        item2.setTotalPrice(46080.0);
        item2.setCreatedAt(LocalDateTime.now().minusDays(2));
        order2.getItems().add(item2);

        OrderStatusHistory h2 = new OrderStatusHistory(null, order2, "SHIPPED", "SELLER", "Dispatched via VRL Logistics, Docket #VRL-882910", LocalDateTime.now().minusHours(12));
        order2.getStatusHistory().add(h2);

        Order savedOrder2 = orderRepository.save(order2);

        // Shipment 2 for Order 2
        Shipment shp2 = new Shipment();
        shp2.setShipmentNumber("SHP-20260818-1002");
        shp2.setOrder(savedOrder2);
        shp2.setSellerId(101L);
        shp2.setDeliveryPartner(delhivery);
        shp2.setTrackingNumber("TRK-DEL-441288");
        shp2.setShippingMode("SURFACE_BULK");
        shp2.setPackageWeightKg(5000.0);
        shp2.setPackageDimensions("200x200x120 cm (Palletized)");
        shp2.setTotalPackages(100);
        shp2.setStatus("IN_TRANSIT");
        shp2.setPickupAddress("UltraTech Bulk Depot, Nelamangala Hub, Bangalore");
        shp2.setDeliveryAddress(savedOrder2.getShippingAddress());
        shp2.setPickupScheduledAt(LocalDateTime.now().minusDays(2));
        shp2.setEstimatedDeliveryAt(LocalDateTime.now().plusDays(1));
        shp2.setDriverName("Sunil Kumar");
        shp2.setDriverContact("+91 97410 55667");
        shp2.setVehicleNumber("KA-01-AB-4509");
        shp2.setCreatedAt(LocalDateTime.now().minusDays(2));
        shp2.setUpdatedAt(LocalDateTime.now().minusHours(12));

        Shipment savedShp2 = shipmentRepository.save(shp2);

        ShipmentTracking trk2a = new ShipmentTracking(null, savedShp2, "PICKUP_SCHEDULED", "UltraTech Depot, Nelamangala Hub", "100 cement bags loaded on 2 pallets and verified", 13.0983, 77.3891, LocalDateTime.now().minusDays(2), "SELLER");
        ShipmentTracking trk2b = new ShipmentTracking(null, savedShp2, "IN_TRANSIT", "Yeshwanthpur Junction Transit Hub", "Shipment cleared transit checkpoint, heading to Peenya", 13.0234, 77.5489, LocalDateTime.now().minusHours(12), "DELIVERY_PARTNER");
        shipmentTrackingRepository.saveAll(Arrays.asList(trk2a, trk2b));

        // -------------------------------------------------------------
        // Order 3: Confirmed CPVC Pipe Order
        // -------------------------------------------------------------
        Order order3 = new Order();
        order3.setOrderNumber("ORD-20260819-1003");
        order3.setBuyerId(202L);
        order3.setAddressId(15L);
        order3.setShippingAddress("Tower C, Horizon Heights, Electronic City Phase 1, Bangalore - 560100");
        order3.setSubtotal(11875.0);
        order3.setGstAmount(2137.5);
        order3.setDeliveryCharge(800.0);
        order3.setTotalAmount(14812.5);
        order3.setPaymentMethod("CREDIT_CARD");
        order3.setPaymentStatus("PENDING");
        order3.setOrderStatus("CONFIRMED");
        order3.setCreatedAt(LocalDateTime.now().minusHours(6));
        order3.setUpdatedAt(LocalDateTime.now().minusHours(4));

        OrderItem item3 = new OrderItem();
        item3.setOrder(order3);
        item3.setProduct(pipes);
        item3.setSellerId(103L);
        item3.setSeller("Astral Pipe Hub");
        item3.setQuantity(25);
        item3.setUnitPrice(475.0);
        item3.setGstPercentage(18.0);
        item3.setGstAmount(2137.5);
        item3.setSubtotal(11875.0);
        item3.setTotalPrice(14012.5);
        item3.setCreatedAt(LocalDateTime.now().minusHours(6));
        order3.getItems().add(item3);

        OrderStatusHistory h3 = new OrderStatusHistory(null, order3, "CONFIRMED", "SELLER", "Order confirmed by seller, processing warehouse pickup", LocalDateTime.now().minusHours(4));
        order3.getStatusHistory().add(h3);

        Order savedOrder3 = orderRepository.save(order3);

        // Shipment 3 for Order 3
        Shipment shp3 = new Shipment();
        shp3.setShipmentNumber("SHP-20260819-1003");
        shp3.setOrder(savedOrder3);
        shp3.setSellerId(103L);
        shp3.setDeliveryPartner(bluedart);
        shp3.setTrackingNumber("TRK-BD-773199");
        shp3.setShippingMode("EXPRESS_LOGISTICS");
        shp3.setPackageWeightKg(125.0);
        shp3.setPackageDimensions("300x40x40 cm (Bundle)");
        shp3.setTotalPackages(25);
        shp3.setStatus("PICKUP_SCHEDULED");
        shp3.setPickupAddress("Astral Pipe Hub, Bommasandra Link Road, Bangalore");
        shp3.setDeliveryAddress(savedOrder3.getShippingAddress());
        shp3.setPickupScheduledAt(LocalDateTime.now().plusHours(2));
        shp3.setEstimatedDeliveryAt(LocalDateTime.now().plusDays(2));
        shp3.setDriverName("Manjunath B");
        shp3.setDriverContact("+91 99001 33445");
        shp3.setVehicleNumber("KA-05-MH-9122");
        shp3.setCreatedAt(LocalDateTime.now().minusHours(4));
        shp3.setUpdatedAt(LocalDateTime.now().minusHours(4));

        Shipment savedShp3 = shipmentRepository.save(shp3);

        ShipmentTracking trk3a = new ShipmentTracking(null, savedShp3, "PICKUP_SCHEDULED", "Astral Bommasandra Warehouse", "Bundle packed with protective strapping, awaiting courier pickup", 12.8182, 77.6890, LocalDateTime.now().minusHours(4), "SELLER");
        shipmentTrackingRepository.save(trk3a);
    }

    private void seedNotificationsAndPreferences() {
        if (notificationRepository.count() > 0) {
            return;
        }

        // Notifications for Buyer 1, Buyer 201, Buyer 202, Seller 101, Seller 102
        Notification n0 = new Notification(null, 1L, "BUYER", "ORDER_PLACED", "Order Placed Successfully: ORD-20260815-1001", "Your order of ₹361,220.00 for Tata Tiscon TMT Steel has been placed.", "ORDER", 1L, true, LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(4));
        Notification n0b = new Notification(null, 1L, "BUYER", "ORDER_SHIPPED", "Shipment Dispatched: SHP-20260818-1002", "Your cement order is in transit via Delhivery Surface. Tracking: TRK-DEL-441288", "SHIPMENT", 2L, false, null, LocalDateTime.now().minusHours(12));
        Notification n1 = new Notification(null, 201L, "BUYER", "ORDER_PLACED", "Order Placed Successfully: ORD-20260815-1001", "Your order of ₹361,220.00 for Tata Tiscon TMT Steel has been placed.", "ORDER", 1L, true, LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(4));
        Notification n2 = new Notification(null, 201L, "BUYER", "ORDER_DELIVERED", "Order Delivered: ORD-20260815-1001", "Your order has been successfully delivered to Site #42, Whitefield.", "ORDER", 1L, true, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));
        Notification n3 = new Notification(null, 102L, "SELLER", "ORDER_PLACED", "New Order Received: ORD-20260815-1001", "You received an order for 5 Tons Tata Tiscon 550D TMT Rebar (₹304,000.00).", "ORDER", 1L, true, LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(4));
        Notification n4 = new Notification(null, 201L, "BUYER", "ORDER_SHIPPED", "Shipment In Transit: SHP-20260818-1002", "Your cement order #ORD-20260818-1002 is in transit via Delhivery Surface. Tracking: TRK-DEL-441288", "SHIPMENT", 2L, false, null, LocalDateTime.now().minusHours(12));
        Notification n5 = new Notification(null, 202L, "BUYER", "ORDER_CONFIRMED", "Order Confirmed: ORD-20260819-1003", "Your Astral Pipe order has been confirmed and shipment scheduled.", "ORDER", 3L, false, null, LocalDateTime.now().minusHours(4));
        Notification n6 = new Notification(null, 101L, "SELLER", "LOW_STOCK", "Low Stock Alert: UltraTech 53 OPC Cement", "Current inventory for SKU HN-CMT-UT-53OPC has dropped below 150 bags.", "PRODUCT", 1L, false, null, LocalDateTime.now().minusHours(2));

        notificationRepository.saveAll(Arrays.asList(n0, n0b, n1, n2, n3, n4, n5, n6));

        // 3 Device Tokens
        if (deviceTokenRepository.count() == 0) {
            DeviceToken dt1 = new DeviceToken(null, 201L, "BUYER", "fcm_token_buyer_builder_pixel8_alpha778899", "ANDROID", "pixel_8_pro_001", true, LocalDateTime.now().minusHours(1), LocalDateTime.now().minusDays(15));
            DeviceToken dt2 = new DeviceToken(null, 201L, "BUYER", "web_push_token_buyer_builder_chrome_998811", "WEB", "chrome_mac_002", true, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusDays(10));
            DeviceToken dt3 = new DeviceToken(null, 102L, "SELLER", "apns_token_seller_tatasteel_iphone15_223344", "IOS", "iphone_15_pro_003", true, LocalDateTime.now().minusMinutes(30), LocalDateTime.now().minusDays(8));
            deviceTokenRepository.saveAll(Arrays.asList(dt1, dt2, dt3));
        }

        // 3 Notification Preferences
        if (notificationPreferenceRepository.count() == 0) {
            NotificationPreference pref1 = new NotificationPreference(null, 201L, "BUYER", "ORDER_UPDATES", true, true, true, true, LocalDateTime.now().minusDays(15), LocalDateTime.now());
            NotificationPreference pref2 = new NotificationPreference(null, 201L, "BUYER", "RFQ_UPDATES", true, true, true, false, LocalDateTime.now().minusDays(15), LocalDateTime.now());
            NotificationPreference pref3 = new NotificationPreference(null, 102L, "SELLER", "ORDER_UPDATES", true, true, true, true, LocalDateTime.now().minusDays(8), LocalDateTime.now());
            notificationPreferenceRepository.saveAll(Arrays.asList(pref1, pref2, pref3));
        }
    }
}
