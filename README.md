# Mo Store – SuperMarket Web System

A web-based supermarket management system built with **Spring Boot 3**, **Java 21**, **Thymeleaf**, and **MySQL**.

## Features

- **Dashboard** – today's revenue, sales, total products, low stock and top-product insights
- **Products** – full CRUD with brand, unit of measure, tax rate, manufacture/expiry dates, warehouse location, batch/lot, low-stock and "EXPIRING" badges
- **Categories** – CRUD with product counts
- **Suppliers** – CRUD with tax ID and payment terms
- **Point of Sale** – cart with customer name, discount and tax at checkout, stock auto-reduction, receipts showing Subtotal/Discount/Tax/Grand Total, and sales history
- **Barcode & QR scanning** – scanner-wedge POS input (scan/type a code and press Enter) plus offline, per-product QR code generation
- **Purchase Orders** – multi-item orders with expected delivery date and notes, PENDING → RECEIVED/CANCELLED lifecycle, auto stock/price updates on receipt
- **Employees & Roles** – user management with role-based navigation (ADMIN, CASHIER, STOCK_MANAGER)
- **Reports** – date-range filtered revenue, sales and top products
- **Currency** – Tanzanian Shilling (TSh) with thousands separators

## Tech Stack

- Java 21 · Spring Boot 3.3.5
- Thymeleaf · Bootstrap 5 (custom professional stylesheet)
- MySQL · Spring Data JPA
- Spring Security (role-based access)

## Requirements

- JDK 21
- MySQL (created database: `super_market`)
- Maven 3.9.9 (or use the included Maven wrapper `mvnw.cmd`)

## Setup & Run

1. Start MySQL and create a database named `super_market`.
2. Build the project:

   ```bash
   mvn -DskipTests package
   ```

3. Run the application:

   ```bash
   java -jar target/supermarket-web-1.0.0.jar
   ```

4. Open **http://localhost:9090** in your browser.

Default login: `admin` / `admin123`

> The schema auto-updates (`ddl-auto=update`). A database seeder populates the initial admin user, categories, suppliers and products on first (empty) run.

## Project Structure

```
src/main/java/com/supermarket
├── config       # Security config, database seeder, global model advice
├── controller   # Dashboard, Product, Category, Supplier, Sale, PurchaseOrder, Employee, Report, Auth
├── entity       # Product, Category, Supplier, Sale, SaleItem, PurchaseOrder, PurchaseOrderItem, User
├── repository   # Spring Data JPA repositories
├── security     # Custom user details service
└── service      # Business logic

src/main/resources
├── templates    # Thymeleaf views (shared layout + pages)
├── static       # CSS and JS (bootstrap, custom style, offline QR encoder)
└── application.properties
```

## Documentation

Full documentation is available in the [`docs/`](docs/) folder as Word and PDF files.
