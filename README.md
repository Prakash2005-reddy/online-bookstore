# Chapter & Co. Online Bookstore

A responsive bookstore storefront backed by a Java 17 / Spring Boot REST API and MySQL. The database is the source of truth for books, inventory, customer accounts, carts, orders, and newsletter subscriptions.

## Requirements

- Java 17+
- Maven 3.8+
- Docker Desktop with Docker Compose, or a local MySQL 8 server
- Python 3 (only for the simple frontend web server), or the VS Code Live Server extension

## Run locally

1. Start MySQL from the project root:

   ```powershell
   docker compose up -d mysql
   ```

   The default database is `bookstore`, username `root`, and password `password`. To use another password, set `DB_PASSWORD` before starting Docker and the backend.

2. Start the API in a second terminal:

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

   The API runs at `http://localhost:8080`. On first startup, Hibernate creates/updates the tables and the application adds sample categories, sample books, and an admin account.

3. Serve the frontend in a third terminal:

   ```powershell
   cd frontend
   python -m http.server 5500
   ```

   Open `http://localhost:5500`. The frontend is configured to call the API at `http://localhost:8080`; allowed development origins include ports 5500 and 3000.

### Database and admin settings

The backend reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables. Defaults connect to the local Docker MySQL instance. The first-run admin account is:

- Email: `admin@chapterco.com`
- Password: `admin12345`

Set `ADMIN_PASSWORD` before the very first backend startup to choose another admin password. Change the development defaults before deploying this application publicly. The initial admin is created once; changing the environment variable later does not reset an existing password.

## Features

- Catalog browsing, title/author/category search, category filters, ratings, stock, and book detail views
- Registration and sign-in using BCrypt password hashes and server-side sessions
- CSRF-protected state-changing requests and role-protected admin endpoints
- Per-user persistent carts with quantity and stock validation
- Checkout with delivery details, transactionally reserved inventory, order confirmation, and order history
- Admin catalog CRUD, inventory management, order listing, and order status changes
- Persisted newsletter subscriptions
- Responsive storefront, cart drawer, forms, and success/error notifications

## API overview

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | `/api/auth/csrf` | Public | Initialize session CSRF token |
| POST | `/api/auth/register` | Public | Register a customer |
| POST | `/api/auth/login` | Public | Sign in |
| GET / POST | `/api/auth/me`, `/api/auth/logout` | Signed in | Current account / sign out |
| GET | `/api/books`, `/api/books/{id}` | Public | Search/list and view book details (`q`, `categoryId`) |
| GET | `/api/categories` | Public | List categories |
| GET / POST | `/api/cart`, `/api/cart/items` | Signed in | Read cart / add item |
| PUT / DELETE | `/api/cart/items/{bookId}` | Signed in | Change quantity / remove item |
| GET / POST | `/api/orders` | Signed in | View history / place an order |
| POST / PUT / DELETE | `/api/admin/books[/{id}]` | Admin | Create, update, and delete catalog books |
| GET | `/api/admin/orders` | Admin | List customer orders |
| PATCH | `/api/admin/orders/{id}/status` | Admin | Update fulfillment status |
| POST | `/api/newsletter` | Public | Subscribe an email address |

The admin dashboard is shown after signing in with the seeded admin account. User-provided content is validated at the API boundary, and order creation plus inventory decrement happen in one database transaction.

## Database tables

The application creates `users`, `categories`, `books`, `cart_items`, `orders`, `order_items`, and `newsletter_subscribers` with primary keys, foreign keys, unique constraints, and indexes. Order items keep a title and price snapshot, so order history remains intact if a catalog book is later removed.

## Build and tests

From `backend`, run:

```powershell
mvn test
```

Build a runnable jar with `mvn package`; run it with `java -jar target/online-bookstore-1.0.0.jar`.
