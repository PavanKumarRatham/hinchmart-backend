# HinchMart — B2B Marketplace Backend

Enterprise-grade B2B Marketplace Backend built with **Java 21**, **Spring Boot 3**, **Spring Security 6 (Stateless JWT)**, **Hibernate / Spring Data JPA**, and **MySQL**.

---

## 🚀 Key Modules & Architecture

### 1. Authentication & Security (Stateless JWT & OTP)
- Dynamic Role-Based Access Control: `SUPER_ADMIN`, `ADMIN`, `BUYER`, `SELLER`, `SUPPORT`.
- Email/Password and Mobile + OTP authentication flows.
- Automated token lifecycle: Access Token (15m - 24h) + Refresh Token rotation.
- BCrypt hashed password security with comprehensive audit logging.

### 2. Seller Store & Product Catalog
- B2B seller verification & approval workflow (`PENDING` $\rightarrow$ `APPROVED`).
- Seller Store Profiles (`seller_stores`) with GSTIN validation.
- Hierarchical multi-category catalog: Categories, Subcategories, Brands, and Products.
- Tiered B2B bulk pricing engine (quantity-based price breaks) and Minimum Order Quantity (MOQ) enforcement.

### 3. Cart & B2B Checkout Engine
- Real-time stock reservation and seller status validation.
- Automated dynamic unit price resolution across tiered volume breaks.
- Automated GST breakdown calculation.
- Order placement lifecycle: `PLACED` $\rightarrow$ `CONFIRMED` $\rightarrow$ `PROCESSING` $\rightarrow$ `READY_TO_SHIP` $\rightarrow$ `SHIPPED` $\rightarrow$ `OUT_FOR_DELIVERY` $\rightarrow$ `DELIVERED`.

### 4. RFQ (Request for Quotation) & Multi-Seller Bidding
- Buyers create custom RFQs with line items and target delivery timelines.
- Verified sellers submit competitive quotations (`rfq_quotes`).
- Buyers review bids, accept winning quotation, and automatically reject competing quotes.

### 5. Payments & B2B Tax Invoicing (Member 1)
- **Zero Client-Trust Payment Engine**: Authoritative order amounts verified directly against database records before gateway order creation.
- Payment verification, audit transactions, and refund management.
- **Intra-State vs Inter-State GST Logic**:
  - Intra-State (e.g. Telangana $\leftrightarrow$ Telangana): Split into **CGST 9% + SGST 9%**.
  - Inter-State (e.g. Maharashtra $\leftrightarrow$ Telangana): Applied as **IGST 18%**.

### 6. Logistics, Milestone Tracking & Notifications (Member 2)
- Carrier booking across pre-configured delivery partners (`DELHIVERY_B2B`, `VRL_LOGISTICS`, `RIVIGO_SURFACE`, `BLUEDART_CARGO`).
- Live tracking checkpoints: $\text{PICKUP\_SCHEDULED} \rightarrow \text{PICKED\_UP} \rightarrow \text{IN\_TRANSIT} \rightarrow \text{OUT\_FOR\_DELIVERY} \rightarrow \text{DELIVERED}$.
- Event-driven in-app notifications and FCM push token registration.

---

## 🛠️ Tech Stack
- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.3.x
- **Security**: Spring Security 6, JWT (io.jsonwebtoken)
- **Database**: MySQL 8.x
- **ORM**: Hibernate 6 / Spring Data JPA
- **Documentation**: SpringDoc OpenAPI / Swagger 3

---

## 📖 API Documentation
Start the backend and visit Swagger UI:
👉 `http://localhost:8080/swagger-ui/index.html`
