# ShopEase — Android eCommerce App

Academic group project (**ICT372 · King's Own Institute**) — a local eCommerce Android application with SQLite persistence, Material Design UI, and core shopping flows.

## Features

- Product catalog, detail, and search with type-ahead SQLite queries
- Shopping cart and checkout flow
- User registration, login, and session management
- Purchase history and order tracking
- Material Design layouts and navigation

## Tech stack

- **Java** · Android SDK
- **SQLite** via `DatabaseHelper`
- **Gradle** build system

## Screens implemented

| Screen | Responsibility |
|--------|----------------|
| Product Detail | Product info, add to cart |
| Search | Type-ahead product search |
| Cart / Checkout | Cart management and checkout |
| Home | Catalog browsing |

## Build & run

1. Open the project in **Android Studio** (Ladybug or newer recommended).
2. Sync Gradle — do **not** commit `local.properties` (SDK path is machine-specific).
3. Run on an emulator or device (API 24+).

```bash
./gradlew assembleDebug
```

## Project structure

```
app/src/main/java/com/shopease/app/
  activities/    # Login, Home, Search, Cart, Checkout, etc.
  adapters/      # RecyclerView adapters
  database/      # SQLite helper
  models/        # Product, Cart, Order, User
app/src/main/res/
  layout/        # XML layouts
  drawable/      # Icons and product placeholders
```

## Note

Sample product images use vector placeholders for coursework demonstration. Not connected to a live payment gateway.

## Author

**Habib Khan** — King's Own Institute · [GitHub](https://github.com/hk204844-ui)
