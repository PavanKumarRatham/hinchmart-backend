package com.hinchmart.init;

import com.hinchmart.entity.*;
import com.hinchmart.entity.enums.*;
import com.hinchmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BuyerProfileRepository buyerProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final RfqRepository rfqRepository;
    private final SellerStoreRepository sellerStoreRepository;
    private final RfqQuoteRepository rfqQuoteRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hinchmart.seed.enabled:true}")
    private boolean seedEnabled;

    public DataInitializer(UserRepository userRepository,
                           BuyerProfileRepository buyerProfileRepository,
                           SellerProfileRepository sellerProfileRepository,
                           CategoryRepository categoryRepository,
                           SubcategoryRepository subcategoryRepository,
                           BrandRepository brandRepository,
                           ProductRepository productRepository,
                           RfqRepository rfqRepository,
                           SellerStoreRepository sellerStoreRepository,
                           RfqQuoteRepository rfqQuoteRepository,
                           DeliveryPartnerRepository deliveryPartnerRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.buyerProfileRepository = buyerProfileRepository;
        this.sellerProfileRepository = sellerProfileRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.rfqRepository = rfqRepository;
        this.sellerStoreRepository = sellerStoreRepository;
        this.rfqQuoteRepository = rfqQuoteRepository;
        this.deliveryPartnerRepository = deliveryPartnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        if (userRepository.count() > 0) {
            logger.info("Database already contains data. Skipping initial seeding.");
            return;
        }

        logger.info("Initializing HinchMart B2B Marketplace Seed Data...");

        // 1. Initialize Users with all Roles
        User superAdmin = new User("superadmin@hinchmart.com", "9999999990", passwordEncoder.encode("SuperAdmin@123"), "Super Admin", Role.SUPER_ADMIN, AccountStatus.ACTIVE);
        userRepository.save(superAdmin);

        User admin = new User("admin@hinchmart.com", "9999999991", passwordEncoder.encode("Admin@123"), "Platform Administrator", Role.ADMIN, AccountStatus.ACTIVE);
        userRepository.save(admin);

        User support = new User("support@hinchmart.com", "9999999992", passwordEncoder.encode("Support@123"), "Customer Support Lead", Role.SUPPORT, AccountStatus.ACTIVE);
        userRepository.save(support);

        // Buyer User & Profile
        User buyer = new User("buyer@demo.com", "9876543210", passwordEncoder.encode("Buyer@123"), "Rajesh Sharma", Role.BUYER, AccountStatus.ACTIVE);
        User savedBuyer = userRepository.save(buyer);

        BuyerProfile buyerProfile = new BuyerProfile(savedBuyer, "Apex Infra Projects Pvt Ltd", "27AAAAA0000A1Z5", "Infrastructure Contractor");
        buyerProfile.setBillingAddress("Plot 45, MIDC Industrial Area, Phase 2, Pune");
        buyerProfile.setShippingAddress("Site #7, Metro Line 3 Corridor, Hinjewadi, Pune");
        buyerProfile.setCity("Pune");
        buyerProfile.setState("Maharashtra");
        buyerProfile.setPincode("411057");
        buyerProfile.setCreditLimit(new BigDecimal("5000000.00")); // 50 Lakhs
        buyerProfile.setAnnualTurnover(new BigDecimal("25000000.00"));
        buyerProfileRepository.save(buyerProfile);
        savedBuyer.setBuyerProfile(buyerProfile);

        // Seller User & Profile (Approved)
        User seller = new User("seller@tata.com", "9822012345", passwordEncoder.encode("Seller@123"), "Anand Verma", Role.SELLER, AccountStatus.ACTIVE);
        User savedSeller = userRepository.save(seller);

        SellerProfile sellerProfile = new SellerProfile(savedSeller, "Tata Steel Distribution Hub", "27AAACT2727Q1ZW", "Authorized Direct Distributor", SellerStatus.APPROVED);
        sellerProfile.setPanNumber("AAACT2727Q");
        sellerProfile.setWarehouseAddress("Godown 12-B, Logistics Park, Kalamboli Steel Yard, Navi Mumbai");
        sellerProfile.setCity("Navi Mumbai");
        sellerProfile.setState("Maharashtra");
        sellerProfile.setPincode("410218");
        sellerProfile.setRating(4.9);
        sellerProfile.setBankAccountNumber("0025102000012345");
        sellerProfile.setBankIfscCode("HDFC0000025");
        sellerProfile.setBankName("HDFC Bank");
        sellerProfile.setBankAccountName("Tata Steel Distribution Hub Pvt Ltd");
        sellerProfile.setVerifiedAt(LocalDateTime.now().minusDays(30));

        SellerDocument doc1 = new SellerDocument(sellerProfile, DocumentType.GST_CERTIFICATE, "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600", "27AAACT2727Q1ZW");
        doc1.setVerificationStatus(ApprovalStatus.APPROVED);
        doc1.setVerifiedAt(LocalDateTime.now().minusDays(30));
        sellerProfile.addDocument(doc1);

        SellerDocument doc2 = new SellerDocument(sellerProfile, DocumentType.PAN_CARD, "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=600", "AAACT2727Q");
        doc2.setVerificationStatus(ApprovalStatus.APPROVED);
        doc2.setVerifiedAt(LocalDateTime.now().minusDays(30));
        sellerProfile.addDocument(doc2);

        SellerDocument doc3 = new SellerDocument(sellerProfile, DocumentType.CANCELLED_CHEQUE, "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600", "CHQ-981245");
        doc3.setVerificationStatus(ApprovalStatus.APPROVED);
        doc3.setVerifiedAt(LocalDateTime.now().minusDays(30));
        sellerProfile.addDocument(doc3);

        SellerDocument doc4 = new SellerDocument(sellerProfile, DocumentType.BUSINESS_PROOF, "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=600", "REG-MAH-2021-9988");
        doc4.setVerificationStatus(ApprovalStatus.APPROVED);
        doc4.setVerifiedAt(LocalDateTime.now().minusDays(30));
        sellerProfile.addDocument(doc4);

        sellerProfileRepository.save(sellerProfile);
        savedSeller.setSellerProfile(sellerProfile);

        // Additional Seller (Pending Verification)
        User pendingSeller = new User("seller2@demo.com", "9833098765", passwordEncoder.encode("Seller@123"), "Vikram Patel", Role.SELLER, AccountStatus.ACTIVE);
        User savedPendingSeller = userRepository.save(pendingSeller);

        SellerProfile pendingSellerProfile = new SellerProfile(savedPendingSeller, "Gujarat Industrial Spares & Tools", "24AABCG1234F1Z1", "Wholesaler", SellerStatus.PENDING);
        pendingSellerProfile.setPanNumber("AABCG1234F");
        pendingSellerProfile.setWarehouseAddress("GIDC Estate, Phase 3, Vatva, Ahmedabad");
        pendingSellerProfile.setCity("Ahmedabad");
        pendingSellerProfile.setState("Gujarat");
        pendingSellerProfile.setPincode("382445");
        pendingSellerProfile.setBankAccountNumber("919010045678901");
        pendingSellerProfile.setBankIfscCode("UTIB0000919");
        pendingSellerProfile.setBankName("Axis Bank");
        pendingSellerProfile.setBankAccountName("Gujarat Industrial Spares");

        SellerDocument pendingDoc1 = new SellerDocument(pendingSellerProfile, DocumentType.GST_CERTIFICATE, "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=600", "24AABCG1234F1Z1");
        pendingSellerProfile.addDocument(pendingDoc1);

        SellerDocument pendingDoc2 = new SellerDocument(pendingSellerProfile, DocumentType.PAN_CARD, "https://images.unsplash.com/photo-1554224154-26032ffc0d07?w=600", "AABCG1234F");
        pendingSellerProfile.addDocument(pendingDoc2);

        SellerDocument pendingDoc3 = new SellerDocument(pendingSellerProfile, DocumentType.CANCELLED_CHEQUE, "https://images.unsplash.com/photo-1554224155-6726b3ff858f?w=600", "CHQ-334129");
        pendingSellerProfile.addDocument(pendingDoc3);

        SellerDocument pendingDoc4 = new SellerDocument(pendingSellerProfile, DocumentType.BUSINESS_PROOF, "https://images.unsplash.com/photo-1450133064473-71024230f91b?w=600", "UDYAM-GJ-01-0012345");
        pendingSellerProfile.addDocument(pendingDoc4);

        sellerProfileRepository.save(pendingSellerProfile);
        savedPendingSeller.setSellerProfile(pendingSellerProfile);

        // 2. Initialize Brands
        Brand tataBrand = brandRepository.save(new Brand("TATA Steel", "tata-steel", "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=200", "India's premier steel manufacturer"));
        Brand ultraTechBrand = brandRepository.save(new Brand("UltraTech Cement", "ultratech-cement", "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=200", "The Engineer's Choice"));
        Brand astralBrand = brandRepository.save(new Brand("Astral Pipes", "astral-pipes", "https://images.unsplash.com/photo-1541888946425-d0fbb18f15f6?w=200", "Leader in plumbing and drainage piping systems"));
        Brand havellsBrand = brandRepository.save(new Brand("Havells", "havells", "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=200", "Leading Fast Moving Electrical Goods (FMEG) company"));
        Brand boschBrand = brandRepository.save(new Brand("Bosch Power Tools", "bosch-power-tools", "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=200", "Professional power tools and machinery"));
        Brand asianPaintsBrand = brandRepository.save(new Brand("Asian Paints", "asian-paints", "https://images.unsplash.com/photo-1562259949-e8e7689d7828?w=200", "India's leading paint and waterproofing manufacturer"));
        Brand karamBrand = brandRepository.save(new Brand("Karam Safety", "karam-safety", "https://images.unsplash.com/photo-1578873375969-d652264e101b?w=200", "Personal Protective Equipment and industrial safety solutions"));

        // 3. Initialize HinchMart Categories & Subcategories
        // 1. Steel Rods & Rebars
        Category steelCat = categoryRepository.save(new Category("Steel Rods & Rebars", "steel-rods-rebars", "Structural steel, TMT rebars, MS angles, channels, and binding wires for heavy construction.", "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=600", 1));
        Subcategory tmtSub = subcategoryRepository.save(new Subcategory(steelCat, "TMT Rebars (Fe 500D / 550D)", "tmt-rebars", "High yield strength thermo-mechanically treated bars.", "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=400"));
        subcategoryRepository.save(new Subcategory(steelCat, "Structural MS Channels & Beams", "ms-channels-beams", "ISMB, ISMC heavy structural steel.", "https://images.unsplash.com/photo-1535813547-99c456a41d4a?w=400"));
        subcategoryRepository.save(new Subcategory(steelCat, "Binding Wires & Stirrups", "binding-wires", "GI and MS binding wires for rebar mesh reinforcement.", "https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=400"));

        // 2. Cement & Concrete
        Category cementCat = categoryRepository.save(new Category("Cement & Concrete", "cement-concrete", "OPC 53 Grade, PPC, Ready Mix Concrete (RMC), and curing compounds.", "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=600", 2));
        Subcategory ppcSub = subcategoryRepository.save(new Subcategory(cementCat, "PPC Cement (Portland Pozzolana)", "ppc-cement", "Durable hydraulic cement for residential and commercial structures.", "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=400"));
        subcategoryRepository.save(new Subcategory(cementCat, "OPC 53 Grade Cement", "opc-53-grade", "High early strength cement for bridges, flyovers, and high-rise RCC.", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400"));

        // 3. Pipes & Fittings
        Category pipesCat = categoryRepository.save(new Category("Pipes & Fittings", "pipes-fittings", "PVC, CPVC, HDPE, SWR pipes, and brass industrial valves.", "https://images.unsplash.com/photo-1541888946425-d0fbb18f15f6?w=600", 3));
        Subcategory hdpeSub = subcategoryRepository.save(new Subcategory(pipesCat, "HDPE Industrial Pressure Pipes", "hdpe-pipes", "High density polyethylene piping for water distribution and gas.", "https://images.unsplash.com/photo-1541888946425-d0fbb18f15f6?w=400"));
        subcategoryRepository.save(new Subcategory(pipesCat, "CPVC Hot & Cold Water Pipes", "cpvc-pipes", "Chlorinated polyvinyl chloride pipes for plumbing.", "https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=400"));

        // 4. Electrical & Cables
        Category electricalCat = categoryRepository.save(new Category("Electrical & Cables", "electrical-cables", "Armoured cables, HT/LT wires, switchgears, and distribution panels.", "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=600", 4));
        Subcategory cableSub = subcategoryRepository.save(new Subcategory(electricalCat, "Armoured Heavy Duty Cables", "armoured-cables", "Multi-core copper and aluminium underground cables.", "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400"));

        // 5. Power Tools & Machinery
        Category toolsCat = categoryRepository.save(new Category("Power Tools & Machinery", "power-tools-machinery", "Concrete mixers, rotary hammers, angle grinders, and compactors.", "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600", 5));
        Subcategory grindersSub = subcategoryRepository.save(new Subcategory(toolsCat, "Heavy Angle Grinders & Cutters", "angle-grinders", "Industrial abrasive cutters and angle grinders.", "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=400"));

        // 6. Tiles & Flooring
        categoryRepository.save(new Category("Tiles & Flooring", "tiles-flooring", "Vitrified tiles, epoxy industrial floor coatings, granite slabs, and adhesive grouts.", "https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600", 6));

        // 7. Paints & Waterproofing
        Category paintsCat = categoryRepository.save(new Category("Paints & Waterproofing", "paints-waterproofing", "Elastomeric waterproofing membranes, exterior emulsions, and primers.", "https://images.unsplash.com/photo-1562259949-e8e7689d7828?w=600", 7));
        Subcategory waterProofSub = subcategoryRepository.save(new Subcategory(paintsCat, "Liquid Waterproofing Membranes", "waterproofing-membranes", "Polymer modified cementitious coatings for terrace and basements.", "https://images.unsplash.com/photo-1562259949-e8e7689d7828?w=400"));

        // 8. Safety Equipment
        Category safetyCat = categoryRepository.save(new Category("Safety Equipment", "safety-equipment", "ISI marked helmets, safety shoes, full body fall arrest harnesses, and reflective wear.", "https://images.unsplash.com/photo-1578873375969-d652264e101b?w=600", 8));
        Subcategory ppeSub = subcategoryRepository.save(new Subcategory(safetyCat, "Head & Fall Protection PPE", "head-fall-protection", "Industrial safety helmets, harnesses and safety lanyards.", "https://images.unsplash.com/photo-1578873375969-d652264e101b?w=400"));

        // 4. Initialize Products (including the exact requested TATA Tiscon 550D TMT Bar)

        // Product 1: TATA Tiscon 550D TMT Bar
        Product tataTmt = new Product();
        tataTmt.setProductName("TATA Tiscon 550D TMT Bar");
        tataTmt.setSlug("tata-tiscon-550d-tmt-bar");
        tataTmt.setSeller(savedSeller);
        tataTmt.setCategory(steelCat);
        tataTmt.setSubcategory(tmtSub);
        tataTmt.setBrand(tataBrand);
        tataTmt.setSku("TATA-TISCON-550D-12MM");
        tataTmt.setHsnCode("72142090");
        tataTmt.setGstRate(new BigDecimal("18.00"));
        tataTmt.setMoq(1); // 1 Ton
        tataTmt.setUnit(ProductUnit.TON);
        tataTmt.setMrp(new BigDecimal("65000.00"));
        tataTmt.setSellingPrice(new BigDecimal("61500.00"));
        tataTmt.setStock(25); // 25 Tons
        tataTmt.setDeliveryDays(2);
        tataTmt.setApprovalStatus(ApprovalStatus.APPROVED);
        tataTmt.setDescription("TATA Tiscon 550D is India's first GreenPro certified rebar. Made with superior virgin iron ore and primary steelmaking technology. Exceptional ductility, higher bendability, and superior seismic resistance for all residential, commercial and bridge infrastructure.");
        tataTmt.setSpecifications("{\"Grade\":\"Fe 550D\",\"Diameter\":\"12mm (also available 8mm, 10mm, 16mm, 20mm, 25mm, 32mm)\",\"Standard\":\"IS 1786:2008\",\"Carbon Content\":\"0.25% Max\",\"Yield Strength\":\"550 N/mm² Min\",\"Elongation\":\"14.5% Min\",\"Certifications\":\"BIS Certified, GreenPro Certified\"}");
        tataTmt.setActive(true);

        tataTmt.addImage(new ProductImage(tataTmt, "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=800", true, 0));
        tataTmt.addImage(new ProductImage(tataTmt, "https://images.unsplash.com/photo-1535813547-99c456a41d4a?w=800", false, 1));

        // Multi-tier Bulk Pricing as specified in requirements:
        // 1–4 Tons      ₹61,500
        // 5–9 Tons      ₹60,800
        // 10–24 Tons    ₹59,900
        // 25+ Tons      ₹58,500
        tataTmt.addBulkPrice(new ProductBulkPrice(tataTmt, 1, 4, new BigDecimal("61500.00"), BigDecimal.ZERO));
        tataTmt.addBulkPrice(new ProductBulkPrice(tataTmt, 5, 9, new BigDecimal("60800.00"), new BigDecimal("1.14")));
        tataTmt.addBulkPrice(new ProductBulkPrice(tataTmt, 10, 24, new BigDecimal("59900.00"), new BigDecimal("2.60")));
        tataTmt.addBulkPrice(new ProductBulkPrice(tataTmt, 25, null, new BigDecimal("58500.00"), new BigDecimal("4.88")));

        tataTmt.setInventory(new Inventory(tataTmt, 25, 0, 5, "Kalamboli Yard - Bay 4"));
        productRepository.save(tataTmt);

        // Product 2: UltraTech Super PPC Cement
        Product ultraPpc = new Product();
        ultraPpc.setProductName("UltraTech Super Cement (PPC 50kg Bag)");
        ultraPpc.setSlug("ultratech-super-cement-ppc-50kg");
        ultraPpc.setSeller(savedSeller);
        ultraPpc.setCategory(cementCat);
        ultraPpc.setSubcategory(ppcSub);
        ultraPpc.setBrand(ultraTechBrand);
        ultraPpc.setSku("ULTRATECH-SUPER-PPC-50KG");
        ultraPpc.setHsnCode("25232910");
        ultraPpc.setGstRate(new BigDecimal("28.00"));
        ultraPpc.setMoq(50); // 50 Bags
        ultraPpc.setUnit(ProductUnit.BAG);
        ultraPpc.setMrp(new BigDecimal("420.00"));
        ultraPpc.setSellingPrice(new BigDecimal("380.00"));
        ultraPpc.setStock(1200); // 1200 Bags
        ultraPpc.setDeliveryDays(1);
        ultraPpc.setApprovalStatus(ApprovalStatus.APPROVED);
        ultraPpc.setDescription("UltraTech Super is a finely blended Portland Pozzolana Cement engineered with micro-particles for denser, damp-proof concrete and superior surface finish.");
        ultraPpc.setSpecifications("{\"Bag Weight\":\"50 Kg\",\"Packaging\":\"HDPE Tamper-proof bag\",\"Setting Time (Initial)\":\"140 mins\",\"Setting Time (Final)\":\"240 mins\",\"Standard\":\"IS 1489 (Part 1)\"}");
        ultraPpc.setActive(true);

        ultraPpc.addImage(new ProductImage(ultraPpc, "https://images.unsplash.com/photo-1589939705384-5185137a7f0f?w=800", true, 0));
        ultraPpc.addBulkPrice(new ProductBulkPrice(ultraPpc, 50, 199, new BigDecimal("380.00"), BigDecimal.ZERO));
        ultraPpc.addBulkPrice(new ProductBulkPrice(ultraPpc, 200, 499, new BigDecimal("365.00"), new BigDecimal("3.95")));
        ultraPpc.addBulkPrice(new ProductBulkPrice(ultraPpc, 500, null, new BigDecimal("350.00"), new BigDecimal("7.89")));

        ultraPpc.setInventory(new Inventory(ultraPpc, 1200, 0, 100, "Bhiwandi Central Depot"));
        productRepository.save(ultraPpc);

        // Product 3: Astral Taurus HDPE Pressure Pipe
        Product astralHdpe = new Product();
        astralHdpe.setProductName("Astral Taurus PE 100 HDPE Pipe 63mm PN 10");
        astralHdpe.setSlug("astral-taurus-hdpe-pipe-63mm-pn10");
        astralHdpe.setSeller(savedSeller);
        astralHdpe.setCategory(pipesCat);
        astralHdpe.setSubcategory(hdpeSub);
        astralHdpe.setBrand(astralBrand);
        astralHdpe.setSku("ASTRAL-HDPE-63MM-PN10");
        astralHdpe.setHsnCode("39172110");
        astralHdpe.setGstRate(new BigDecimal("18.00"));
        astralHdpe.setMoq(50);
        astralHdpe.setUnit(ProductUnit.METER);
        astralHdpe.setMrp(new BigDecimal("145.00"));
        astralHdpe.setSellingPrice(new BigDecimal("120.00"));
        astralHdpe.setStock(3000);
        astralHdpe.setDeliveryDays(3);
        astralHdpe.setApprovalStatus(ApprovalStatus.APPROVED);
        astralHdpe.setDescription("High Density Polyethylene pipe manufactured from virgin grade raw materials. Resists chemical aggression, zero corrosion, and 50+ year operational lifespan.");
        astralHdpe.setSpecifications("{\"Outside Diameter\":\"63 mm\",\"Pressure Rating\":\"PN 10 (10 kgf/cm²)\",\"Raw Material\":\"PE 100\",\"Coil Length\":\"100 Meters / 500 Meters\"}");
        astralHdpe.setActive(true);

        astralHdpe.addImage(new ProductImage(astralHdpe, "https://images.unsplash.com/photo-1541888946425-d0fbb18f15f6?w=800", true, 0));
        astralHdpe.addBulkPrice(new ProductBulkPrice(astralHdpe, 50, 199, new BigDecimal("120.00"), BigDecimal.ZERO));
        astralHdpe.addBulkPrice(new ProductBulkPrice(astralHdpe, 200, 999, new BigDecimal("112.00"), new BigDecimal("6.67")));
        astralHdpe.addBulkPrice(new ProductBulkPrice(astralHdpe, 1000, null, new BigDecimal("105.00"), new BigDecimal("12.50")));

        astralHdpe.setInventory(new Inventory(astralHdpe, 3000, 0, 200, "Panvel Logistics Center"));
        productRepository.save(astralHdpe);

        // 5. Initialize Sample RFQ
        Rfq demoRfq = new Rfq();
        demoRfq.setRfqNumber("RFQ-2026-0001-DEMO");
        demoRfq.setBuyer(savedBuyer);
        demoRfq.setTitle("Procurement of 50 Tons TMT Fe550D & 500 Bags Cement for Metro Site #7");
        demoRfq.setNotes("Urgent dispatch required to Hinjewadi site. Unloading facilities available at location. Mill test certificates and GST tax invoice mandatory.");
        demoRfq.setDeliveryPincode("411057");
        demoRfq.setDeliveryTimelineDays(5);
        demoRfq.setStatus(RfqStatus.OPEN);

        Rfq savedRfq = rfqRepository.save(demoRfq);

        // 6. Initialize Sample Seller Store
        SellerStore tataStore = new SellerStore(
                savedSeller,
                "Tata Steel Authorized Hub",
                "tata-steel-hub",
                "https://images.unsplash.com/photo-1504917599217-d4dc5ebe6122?w=400",
                "https://images.unsplash.com/photo-1587293852726-70cdb56c2866?w=1200",
                "Official direct supply point for Tata Tiscon TMT rebars, structural sections, and high-tensile wire rods with mill test certifications.",
                "sales@tatasteelhub.com",
                "9822012345",
                "27AAACT2727Q1ZW",
                "Godown 12-B, Logistics Park, Kalamboli Steel Yard, Navi Mumbai",
                StoreStatus.ACTIVE
        );
        sellerStoreRepository.save(tataStore);

        // 7. Initialize Sample RFQ Quotation
        RfqQuote tataQuote = new RfqQuote(
                savedRfq,
                savedSeller,
                new BigDecimal("58500.00"), // Rate per ton
                new BigDecimal("18.00"),
                new BigDecimal("3500.00"),
                3,
                LocalDateTime.now().plusDays(10),
                "100% Against Dispatch / RTGS",
                "Ex-stock available at Kalamboli yard. Test certificate with heat numbers included.",
                QuoteStatus.SUBMITTED
        );
        rfqQuoteRepository.save(tataQuote);

        // 8. Initialize Delivery Partners
        deliveryPartnerRepository.save(new DeliveryPartner("Delhivery B2B Freight", "DELHIVERY_B2B", "+91-124-6719500", "https://www.delhivery.com/track/package/{trackingNumber}"));
        deliveryPartnerRepository.save(new DeliveryPartner("VRL Logistics Heavy Freight", "VRL_LOGISTICS", "+91-836-2237511", "https://www.vrlgroup.in/track_consignment.aspx?lr_no={trackingNumber}"));
        deliveryPartnerRepository.save(new DeliveryPartner("Rivigo Express Surface", "RIVIGO_SURFACE", "+91-124-4354500", "https://www.rivigo.com/tracking?awb={trackingNumber}"));
        deliveryPartnerRepository.save(new DeliveryPartner("Blue Dart Apex Cargo", "BLUEDART_CARGO", "1860-233-1234", "https://www.bluedart.com/tracking/{trackingNumber}"));

        logger.info(">>> HinchMart Seed Data initialized successfully!");
        logger.info(">>> Demo Credentials: ");
        logger.info("    SUPER_ADMIN : superadmin@hinchmart.com / SuperAdmin@123");
        logger.info("    ADMIN       : admin@hinchmart.com / Admin@123");
        logger.info("    BUYER       : buyer@demo.com / Buyer@123");
        logger.info("    SELLER      : seller@tata.com / Seller@123");
        logger.info("    SUPPORT     : support@hinchmart.com / Support@123");
    }
}
