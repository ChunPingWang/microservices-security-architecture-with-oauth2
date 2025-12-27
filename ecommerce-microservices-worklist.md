# E-Commerce Microservices Platform - AI 工具開發工作清單

> **用途**: 提供給 AI 編碼助手（Claude Code、GitHub Copilot、Cursor 等）使用的結構化工作清單
> **專案類型**: 企業級電商微服務平台
> **架構模式**: DDD + 六角架構 (Hexagonal Architecture)

---

## 📋 專案元資料

```yaml
project_name: E-Commerce Microservices Platform
tech_stack:
  language: Java 21
  framework: Spring Boot 3.2.1
  security: Spring Security 6.x
  database: PostgreSQL 15
  cache: Redis 7
  migration: Flyway 10.x
  
architecture:
  pattern: Hexagonal Architecture (Ports & Adapters)
  design: Domain-Driven Design (DDD)
  testing: Test-Driven Development (TDD)

services:
  - name: customer-service
    port: 8081
  - name: product-service
    port: 8082
  - name: order-service
    port: 8083
  - name: payment-service
    port: 8084
  - name: logistics-service
    port: 8084
  - name: sales-service
    port: 8085
  - name: admin-portal
    port: 8090
  - name: api-gateway
    port: 8080
```

---

## 🏗️ 第一階段：基礎設施與共用模組

### TASK-001: 建立 shared-kernel 共用領域物件

```yaml
id: TASK-001
priority: P0 (最高)
module: shared-kernel
estimated_effort: 2d
dependencies: []

description: |
  建立跨服務共用的領域物件、值物件、事件定義

deliverables:
  - src/main/java/com/ecommerce/shared/domain/
      - AggregateRoot.java
      - DomainEvent.java
      - ValueObject.java
      - EntityId.java
  - src/main/java/com/ecommerce/shared/vo/
      - Money.java
      - Email.java
      - PhoneNumber.java
      - Address.java
  - src/main/java/com/ecommerce/shared/event/
      - OrderCreatedEvent.java
      - PaymentCompletedEvent.java
      - ShipmentStatusChangedEvent.java

acceptance_criteria:
  - [ ] 所有值物件實作 equals/hashCode
  - [ ] 領域事件包含 eventId, timestamp, aggregateId
  - [ ] 單元測試覆蓋率 > 90%
  
code_pattern: |
  // ValueObject 基類範例
  public abstract class ValueObject<T> {
      @Override
      public abstract boolean equals(Object o);
      @Override
      public abstract int hashCode();
  }
```

### TASK-002: 建立 security-infrastructure 安全基礎設施

```yaml
id: TASK-002
priority: P0
module: security-infrastructure
estimated_effort: 3d
dependencies: [TASK-001]

description: |
  實作 JWT 認證、授權過濾器、用戶上下文傳遞機制

deliverables:
  - src/main/java/com/ecommerce/security/
      - jwt/
          - JwtTokenProvider.java
          - JwtProperties.java
      - filter/
          - JwtAuthenticationFilter.java
      - interceptor/
          - ServiceAuthInterceptor.java
      - context/
          - CurrentUserContext.java
      - config/
          - SecurityConfig.java

acceptance_criteria:
  - [ ] JWT Token 產生與驗證功能
  - [ ] Access Token 15分鐘過期
  - [ ] Refresh Token 7天過期
  - [ ] 服務間認證使用專用 Service Token
  - [ ] CurrentUserContext 支援 @RequestScope
  
key_implementation: |
  // JwtAuthenticationFilter 核心邏輯
  @Override
  protected void doFilterInternal(HttpServletRequest request,
          HttpServletResponse response, FilterChain filterChain) {
      extractToken(request).ifPresent(this::authenticateToken);
      filterChain.doFilter(request, response);
  }
  
  // ServiceAuthInterceptor 服務間認證
  @Override
  public void apply(RequestTemplate template) {
      String serviceToken = jwtTokenProvider.generateAccessToken(
          "service-internal", "service@internal", "SERVICE");
      template.header("Authorization", "Bearer " + serviceToken);
  }
```

---

## 🔐 第二階段：安全架構實作

### TASK-003: API Gateway 南北向安全

```yaml
id: TASK-003
priority: P0
module: api-gateway
estimated_effort: 2d
dependencies: [TASK-002]

description: |
  實作外部客戶端透過 API Gateway 進入系統的安全機制

deliverables:
  - src/main/java/com/ecommerce/gateway/config/
      - SecurityConfig.java
      - RateLimitConfig.java
  - src/main/java/com/ecommerce/gateway/filter/
      - JwtAuthenticationFilter.java
      - RateLimitFilter.java

security_rules:
  public_endpoints:
    - /api/auth/**
    - /api/v1/products/**
    - /actuator/health/**
  authenticated_endpoints:
    - /api/v1/cart/**
    - /api/v1/orders/**
  admin_endpoints:
    - /api/admin/**

acceptance_criteria:
  - [ ] 公開端點無需認證
  - [ ] 認證端點驗證 JWT Token
  - [ ] 管理端點需要 ROLE_ADMIN
  - [ ] Rate Limiting 防止濫用
  
config_example: |
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) {
      return http
          .csrf(csrf -> csrf.disable())
          .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/auth/**").permitAll()
              .requestMatchers("/api/v1/products/**").permitAll()
              .requestMatchers("/api/v1/cart/**").authenticated()
              .requestMatchers("/api/admin/**").hasRole("ADMIN")
              .anyRequest().authenticated())
          .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
          .build();
  }
```

### TASK-004: 東西向服務間安全

```yaml
id: TASK-004
priority: P1
module: all-services
estimated_effort: 2d
dependencies: [TASK-002, TASK-003]

description: |
  實作微服務之間的內部通訊安全機制（服務間 JWT 認證）

deliverables:
  - FeignClientConfig for each service
  - ServiceAuthInterceptor integration
  - TracingFeignInterceptor for distributed tracing

feign_client_pattern: |
  @FeignClient(name = "payment-service", 
               configuration = FeignClientConfig.class)
  public interface PaymentServiceClient {
      @PostMapping("/internal/payments/process")
      PaymentResult processPayment(@RequestBody PaymentRequest request);
  }
  
  @Configuration
  public class FeignClientConfig {
      @Bean
      public ServiceAuthInterceptor serviceAuthInterceptor(
              JwtTokenProvider jwtTokenProvider) {
          return new ServiceAuthInterceptor(jwtTokenProvider);
      }
  }

acceptance_criteria:
  - [ ] 服務間呼叫自動注入 Service Token
  - [ ] 被呼叫服務驗證 ROLE_SERVICE 權限
  - [ ] B3 追蹤標頭正確傳遞
```

---

## 👤 第三階段：客戶服務 (customer-service)

### TASK-005: US1 客戶認證功能

```yaml
id: TASK-005
priority: P0
module: customer-service
user_story: US1
estimated_effort: 3d
dependencies: [TASK-002]

description: |
  實作客戶註冊、登入、JWT 認證、帳號鎖定功能

api_endpoints:
  - POST /api/auth/login         # 客戶登入
  - POST /api/auth/refresh       # Token 刷新
  - POST /api/customers/register # 客戶註冊
  - GET  /api/customers/me       # 取得個人資料

domain_model:
  aggregate: Customer
  entities:
    - Customer (id, email, password, status, failedLoginAttempts)
  value_objects:
    - Email
    - Password (BCrypt encrypted)
    - CustomerStatus (ACTIVE, LOCKED, SUSPENDED)

business_rules:
  - 密碼使用 BCrypt 加密儲存
  - 連續登入失敗 5 次鎖定 30 分鐘
  - Access Token 15 分鐘過期
  - Refresh Token 7 天過期

hexagonal_structure:
  domain:
    - Customer.java
    - CustomerRepository.java (port)
  application:
    - RegisterCustomerUseCase.java
    - AuthenticateCustomerUseCase.java
  infrastructure:
    - JpaCustomerRepository.java (adapter)
    - CustomerController.java (adapter)

acceptance_criteria:
  - [ ] 註冊時驗證 Email 唯一性
  - [ ] 登入成功返回 JWT Token
  - [ ] 帳號鎖定後拒絕登入
  - [ ] 單元測試 > 80% 覆蓋率
```

### TASK-006: US7 會員等級功能

```yaml
id: TASK-006
priority: P2
module: customer-service
user_story: US7
estimated_effort: 2d
dependencies: [TASK-005]

description: |
  實作消費累積、等級升級、會員專屬折扣功能

api_endpoints:
  - GET /api/customers/me/membership  # 取得會員資訊

domain_model:
  value_objects:
    - MembershipLevel (BRONZE, SILVER, GOLD, PLATINUM)
    - TotalSpent
  rules:
    - BRONZE: 0 ~ 9,999
    - SILVER: 10,000 ~ 49,999
    - GOLD: 50,000 ~ 199,999
    - PLATINUM: 200,000+

discount_rules:
  BRONZE: 0%
  SILVER: 3%
  GOLD: 5%
  PLATINUM: 10%

acceptance_criteria:
  - [ ] 消費金額正確累積
  - [ ] 等級自動升級
  - [ ] 折扣率正確計算
```

---

## 📦 第四階段：商品服務 (product-service)

### TASK-007: US2 商品瀏覽功能

```yaml
id: TASK-007
priority: P0
module: product-service
user_story: US2
estimated_effort: 3d
dependencies: [TASK-001]

description: |
  實作商品列表、分類、搜尋、詳情頁功能

api_endpoints:
  - GET /api/v1/products           # 商品列表 (分頁)
  - GET /api/v1/products/{id}      # 商品詳情
  - GET /api/v1/products/search    # 商品搜尋
  - GET /api/v1/categories         # 分類列表

domain_model:
  aggregate: Product
  entities:
    - Product (id, name, description, price, stock, categoryId)
    - Category (id, name, parentId)
  value_objects:
    - ProductId
    - Money
    - Stock

features:
  - 分頁查詢 (Pageable)
  - 關鍵字搜尋 (Elasticsearch)
  - 分類篩選
  - 庫存顯示

hexagonal_structure:
  domain:
    - Product.java
    - ProductRepository.java (port)
    - ProductSearchService.java (port)
  application:
    - GetProductListUseCase.java
    - SearchProductsUseCase.java
  infrastructure:
    - JpaProductRepository.java
    - ElasticsearchProductSearchAdapter.java
    - ProductController.java

acceptance_criteria:
  - [ ] 分頁參數正確處理
  - [ ] 搜尋結果相關性排序
  - [ ] 庫存數量即時顯示
  - [ ] API 回應時間 < 200ms
```

---

## 🛒 第五階段：購物車與訂單

### TASK-008: US3 購物車功能

```yaml
id: TASK-008
priority: P1
module: order-service
user_story: US3
estimated_effort: 2d
dependencies: [TASK-005, TASK-007]

description: |
  實作新增、修改數量、移除商品功能

api_endpoints:
  - GET    /api/v1/cart           # 取得購物車
  - POST   /api/v1/cart/items     # 新增商品
  - PUT    /api/v1/cart/items/{id} # 修改數量
  - DELETE /api/v1/cart/items/{id} # 移除商品

storage_strategy: Redis
  key_pattern: "cart:{customerId}"
  ttl: 7 days

domain_model:
  aggregate: Cart
  entities:
    - CartItem (productId, quantity, price)
  operations:
    - addItem(productId, quantity)
    - updateQuantity(productId, quantity)
    - removeItem(productId)
    - calculateTotal()

acceptance_criteria:
  - [ ] 購物車資料存入 Redis
  - [ ] 商品數量上限檢查
  - [ ] 總金額即時計算
  - [ ] 7 天無操作自動清除
```

### TASK-009: US4 訂單付款功能

```yaml
id: TASK-009
priority: P0
module: order-service, payment-service
user_story: US4
estimated_effort: 4d
dependencies: [TASK-008]

description: |
  實作建立訂單、多種付款方式、付款逾時處理

api_endpoints:
  - POST /api/v1/orders           # 建立訂單
  - GET  /api/v1/orders           # 訂單歷史
  - GET  /api/v1/orders/{id}      # 訂單詳情
  - POST /api/v1/orders/{id}/pay  # 發起付款

order_flow:
  1. 驗證購物車商品
  2. 檢查庫存
  3. 建立訂單 (狀態: PENDING)
  4. 鎖定庫存
  5. 導向付款
  6. 付款成功 → 訂單確認
  7. 付款逾時 → 釋放庫存、取消訂單

payment_methods:
  - CREDIT_CARD
  - BANK_TRANSFER
  - LINE_PAY
  - APPLE_PAY

timeout_handling:
  payment_timeout: 30 minutes
  action: 自動取消訂單、釋放庫存

saga_pattern:
  steps:
    - CreateOrderStep
    - ReserveInventoryStep
    - ProcessPaymentStep
    - ConfirmOrderStep
  compensation:
    - ReleaseInventoryStep
    - CancelOrderStep

acceptance_criteria:
  - [ ] 訂單狀態正確轉換
  - [ ] 庫存正確扣減/釋放
  - [ ] 付款逾時自動處理
  - [ ] SAGA 補償機制正常運作
```

---

## 🚚 第六階段：物流與促銷

### TASK-010: US5 訂單追蹤功能

```yaml
id: TASK-010
priority: P2
module: logistics-service
user_story: US5
estimated_effort: 2d
dependencies: [TASK-009]

description: |
  實作物流追蹤、狀態更新通知功能

api_endpoints:
  - GET /api/v1/shipments/{orderId}/tracking  # 物流追蹤

shipment_status:
  - PREPARING     # 準備中
  - SHIPPED       # 已出貨
  - IN_TRANSIT    # 運送中
  - OUT_FOR_DELIVERY  # 配送中
  - DELIVERED     # 已送達

notification:
  channels:
    - EMAIL
    - SMS
    - PUSH
  events:
    - StatusChanged
    - DeliveryScheduled

acceptance_criteria:
  - [ ] 追蹤碼查詢物流狀態
  - [ ] 狀態變更發送通知
  - [ ] 預計送達時間顯示
```

### TASK-011: US6 促銷優惠功能

```yaml
id: TASK-011
priority: P1
module: sales-service
user_story: US6
estimated_effort: 3d
dependencies: [TASK-009]

description: |
  實作促銷活動、優惠券驗證與套用功能

api_endpoints:
  - GET  /api/v1/promotions           # 促銷活動列表
  - POST /api/v1/coupons/validate     # 優惠券驗證
  - POST /api/v1/coupons/apply        # 套用優惠券

promotion_types:
  - PERCENTAGE_OFF    # 百分比折扣
  - FIXED_AMOUNT_OFF  # 固定金額折扣
  - BUY_X_GET_Y       # 買X送Y
  - FREE_SHIPPING     # 免運費

coupon_validation:
  checks:
    - 有效期限
    - 使用次數限制
    - 最低消費金額
    - 適用商品/分類
    - 會員等級限制

domain_model:
  aggregates:
    - Promotion
    - Coupon
  value_objects:
    - DiscountRule
    - ValidityPeriod
    - UsageLimit

acceptance_criteria:
  - [ ] 優惠券驗證邏輯完整
  - [ ] 折扣金額正確計算
  - [ ] 不可重複使用驗證
  - [ ] 促銷活動時間控制
```

---

## 🖥️ 第七階段：管理後台

### TASK-012: Admin Portal 後台功能

```yaml
id: TASK-012
priority: P1
module: admin-portal
estimated_effort: 5d
dependencies: [TASK-005 ~ TASK-011]

description: |
  實作管理後台完整功能

api_endpoints:
  auth:
    - POST /api/admin/auth/login     # 管理員登入
  
  products:
    - GET    /api/admin/products     # 商品列表
    - POST   /api/admin/products     # 新增商品
    - PUT    /api/admin/products/{id} # 更新商品
    - DELETE /api/admin/products/{id} # 刪除商品
    - PUT    /api/admin/products/{id}/stock # 庫存調整
    - PUT    /api/admin/products/{id}/status # 上下架
  
  orders:
    - GET  /api/admin/orders         # 訂單列表
    - PUT  /api/admin/orders/{id}/status # 狀態更新
    - POST /api/admin/orders/{id}/cancel # 取消訂單
  
  customers:
    - GET /api/admin/customers       # 客戶列表
    - PUT /api/admin/customers/{id}/status # 帳號狀態
    - PUT /api/admin/customers/{id}/membership # 會員等級
  
  promotions:
    - GET    /api/admin/promotions   # 促銷列表
    - POST   /api/admin/promotions   # 新增促銷
    - PUT    /api/admin/promotions/{id} # 更新促銷
    - DELETE /api/admin/promotions/{id} # 刪除促銷
  
  reports:
    - GET /api/admin/reports/sales   # 銷售報表
    - GET /api/admin/reports/daily   # 每日銷售
    - GET /api/admin/reports/top-products # 熱銷商品
    - GET /api/admin/reports/customers # 客戶統計

role_permissions:
  ADMIN:
    - all operations
  OPERATOR:
    - orders (read, update)
    - products (read, update stock)
    - reports (read)
  VIEWER:
    - read only

acceptance_criteria:
  - [ ] 權限控制完整
  - [ ] 報表資料正確
  - [ ] 批次操作支援
  - [ ] 操作日誌記錄
```

---

## 📊 第八階段：可觀測性

### TASK-013: 分散式追蹤與監控

```yaml
id: TASK-013
priority: P1
module: all-services
estimated_effort: 3d
dependencies: [TASK-003]

description: |
  整合 Micrometer、Zipkin、Prometheus、Grafana

components:
  tracing:
    - Zipkin integration
    - B3 header propagation
    - TracingFeignInterceptor
  
  metrics:
    - Micrometer metrics
    - Prometheus endpoint
    - Custom business metrics
  
  health:
    - /actuator/health
    - /actuator/health/liveness
    - /actuator/health/readiness
    - /actuator/info
    - /actuator/prometheus

tracing_headers:
  - X-B3-TraceId
  - X-B3-SpanId
  - X-B3-Sampled

grafana_dashboards:
  - JVM Metrics
  - HTTP Request Metrics
  - Database Connection Pool
  - Business KPIs

acceptance_criteria:
  - [ ] 請求可跨服務追蹤
  - [ ] Prometheus 指標正確收集
  - [ ] Grafana 儀表板配置完成
  - [ ] 告警規則設定
```

---

## 🐳 第九階段：容器化與部署

### TASK-014: Docker & Kubernetes 部署

```yaml
id: TASK-014
priority: P1
module: infrastructure
estimated_effort: 3d
dependencies: [TASK-001 ~ TASK-013]

description: |
  建立 Docker 映像檔與 Kubernetes 部署清單

deliverables:
  docker:
    - Dockerfile for each service
    - docker-compose.yml (local dev)
    - docker-compose.monitoring.yml
  
  kubernetes:
    - infrastructure/k8s/base/
        - namespace.yaml
        - configmap.yaml
        - secret.yaml
        - deployments/
        - services/
        - ingress.yaml
    - infrastructure/k8s/overlays/
        - dev/kustomization.yaml
        - prod/kustomization.yaml

k8s_resources:
  deployments:
    - customer-service
    - product-service
    - order-service
    - payment-service
    - logistics-service
    - sales-service
    - admin-portal
    - api-gateway
  
  services:
    - ClusterIP for internal
    - LoadBalancer for api-gateway
  
  config:
    - ConfigMap for app config
    - Secret for credentials

probes:
  liveness: /actuator/health/liveness
  readiness: /actuator/health/readiness
  startup: /actuator/health

resource_limits:
  requests:
    cpu: 250m
    memory: 512Mi
  limits:
    cpu: 500m
    memory: 1Gi

acceptance_criteria:
  - [ ] Docker 映像建置成功
  - [ ] K8s 部署正常啟動
  - [ ] 服務間通訊正常
  - [ ] Ingress 路由正確
```

---

## 🧪 測試策略

### TASK-015: 測試覆蓋率達標

```yaml
id: TASK-015
priority: P1
module: all-services
estimated_effort: ongoing

test_types:
  unit_tests:
    scope: Domain logic, Use cases
    coverage_target: "> 80%"
    framework: JUnit 5, Mockito
  
  integration_tests:
    scope: Repository, Controller
    coverage_target: "> 70%"
    framework: @SpringBootTest, TestContainers
  
  security_tests:
    scope: Authentication, Authorization
    framework: @WithMockUser, @WithSecurityContext

test_examples:
  security_test: |
    @SpringBootTest
    @AutoConfigureMockMvc
    class OrderControllerSecurityTest {
        @Test
        @WithMockUser(roles = "CUSTOMER")
        void authenticatedUser_canCreateOrder() {
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(APPLICATION_JSON)
                    .content(orderJson))
                .andExpect(status().isCreated());
        }
        
        @Test
        void unauthenticatedUser_cannotCreateOrder() {
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(APPLICATION_JSON)
                    .content(orderJson))
                .andExpect(status().isUnauthorized());
        }
    }

current_status:
  total_tests: 478
  by_service:
    customer-service: 67
    product-service: 89
    order-service: 42
    payment-service: 58
    logistics-service: 45
    sales-service: 48
    admin-portal: 50
    shared-kernel: 79

acceptance_criteria:
  - [ ] 單元測試覆蓋率 > 80%
  - [ ] 整合測試覆蓋率 > 70%
  - [ ] 所有安全端點有測試
  - [ ] CI Pipeline 測試通過
```

---

## 📚 附錄：AI 工具指令範本

### A. Claude Code 專案初始化指令

```
/init 請根據以下規格建立 Spring Boot 微服務專案：
- Java 21
- Spring Boot 3.2.1
- 六角架構 (Hexagonal Architecture)
- 包含 domain, application, infrastructure 三層
```

### B. 建立領域模型指令

```
/code 請建立 Customer 領域模型：
- Aggregate Root
- 包含 Email, Password 值物件
- 實作帳號鎖定邏輯 (5次失敗鎖定30分鐘)
- 遵循 DDD 原則
```

### C. 建立 REST API 指令

```
/code 請建立 ProductController：
- GET /api/v1/products (分頁)
- GET /api/v1/products/{id}
- GET /api/v1/products/search
- 使用六角架構，透過 UseCase 呼叫
```

### D. 建立安全配置指令

```
/code 請建立 Spring Security 配置：
- JWT 認證過濾器
- 路徑授權規則
- 無狀態 Session
- 參考 SecurityConfig 範例
```

### E. 建立測試指令

```
/test 請為 OrderService 建立測試：
- 單元測試 (Mock Repository)
- 整合測試 (@SpringBootTest)
- 安全測試 (@WithMockUser)
```

---

## 📌 工作優先順序總覽

| 優先級 | Task ID | 模組 | 工作項目 |
|--------|---------|------|----------|
| P0 | TASK-001 | shared-kernel | 共用領域物件 |
| P0 | TASK-002 | security-infrastructure | 安全基礎設施 |
| P0 | TASK-003 | api-gateway | 南北向安全 |
| P0 | TASK-005 | customer-service | 客戶認證 |
| P0 | TASK-007 | product-service | 商品瀏覽 |
| P0 | TASK-009 | order-service | 訂單付款 |
| P1 | TASK-004 | all-services | 東西向安全 |
| P1 | TASK-008 | order-service | 購物車 |
| P1 | TASK-011 | sales-service | 促銷優惠 |
| P1 | TASK-012 | admin-portal | 管理後台 |
| P1 | TASK-013 | all-services | 可觀測性 |
| P1 | TASK-014 | infrastructure | K8s 部署 |
| P2 | TASK-006 | customer-service | 會員等級 |
| P2 | TASK-010 | logistics-service | 物流追蹤 |

---

*Generated for AI-assisted development tools*
*Last Updated: 2025-12-27*
