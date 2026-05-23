# ShopEase — App structure (summary)

```
Activities (UI)
    │
    ▼
Adapters ←→ Models (Product, Cart, Order, User)
    │
    ▼
DatabaseHelper (SQLite)
```

## Key screens

| Screen | Responsibility |
|--------|----------------|
| Search | Type-ahead product queries |
| Product Detail | Item view, add to cart |
| Cart / Checkout | Order flow |
| Home | Catalog browsing |
