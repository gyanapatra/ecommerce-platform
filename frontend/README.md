# Forma storefront

An Angular 22 standalone storefront for the ePlatform API. The interface uses the account, member profile, product catalog, persistent cart, mock-first payment, and order history endpoints. Category filters and search are derived from catalog data. Newsletter signup is a visual interaction only and does not submit customer information.

## Start locally

1. Start PostgreSQL, Auth Service, Member Service, Product Catalog Service, Order Service, Payment Service and API Gateway as described in the repository README.
2. From this directory, run `npm install` and `npm start`.
3. Open `http://localhost:4200`.

The Angular dev server proxies `/api` to `http://localhost:8090`. For another gateway address, edit `proxy.conf.json`. Catalog endpoints require a valid access token, so sign in or create an account to load the products. The Gateway's allowed browser origin defaults to `http://localhost:4200`; set `APP_CORS_ALLOWED_ORIGIN` to the deployed frontend origin.

## API alignment

- `POST /api/v1/auth/register` — email, password, firstName, lastName.
- `POST /api/v1/auth/login` — email and password; access and refresh tokens are stored in browser local storage.
- `POST /api/v1/auth/refresh` and `POST /api/v1/auth/logout` use the refresh token request body.
- `GET /api/v1/products` — unwraps `ApiResponse<List<ProductResponse>>` and drives search, category tabs and product cards.
- `GET`, `POST`, `PUT /items/{productId}`, and `DELETE /items/{productId}` under `/api/v1/cart` manage the authenticated customer's persistent bag.
- `POST /api/v1/orders` places an order with a fresh UUID `Idempotency-Key`; `POST /api/v1/payments` completes local mock payment with the order ID as its stable idempotency key. Orders can be paid again from order history while they remain `PLACED` or `PAYMENT_PENDING`.
- `GET` and `PUT /api/v1/members/me` retrieve and save the signed-in member profile; Member Service owns the member number and membership status.

Inventory is reserved during checkout. The storefront then completes a local mock payment; an unpaid order remains in order history with a retry action. The backend marks it `PAYMENT_PENDING` before the provider is called, which prevents cancellation from racing with a charge. Successful payment marks the order `PAID` and emits an order event for Notification Service. This mock provider does not collect card data or contact a payment network. The frontend keeps the newsletter interaction local and does not imply that a signup was submitted.


