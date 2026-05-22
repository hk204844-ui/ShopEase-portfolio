package com.shopease.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shopease.app.models.CartItem;
import com.shopease.app.models.Order;
import com.shopease.app.models.OrderItem;
import com.shopease.app.models.Product;
import com.shopease.app.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "shopease.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_CART = "cart";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_ORDER_ITEMS = "order_items";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_USERS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "email TEXT NOT NULL UNIQUE, " +
                        "password TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "description TEXT NOT NULL, " +
                        "price REAL NOT NULL, " +
                        "category TEXT NOT NULL, " +
                        "image_name TEXT NOT NULL" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_CART + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "product_id INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL DEFAULT 1" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_ORDERS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "total_price REAL NOT NULL, " +
                        "order_date TEXT NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'Confirmed'" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_ORDER_ITEMS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "order_id INTEGER NOT NULL, " +
                        "product_id INTEGER NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "price REAL NOT NULL" +
                        ")"
        );

        seedProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // USER METHODS

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{"id"},
                "email = ?",
                new String[]{email},
                null,
                null,
                null
        );

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) {
            cursor.close();
        }
        if (exists) {
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password", password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{"id", "name", "email", "password"},
                "email = ? AND password = ?",
                new String[]{email, password},
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password"))
            );
        }

        if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{"id", "name", "email", "password"},
                "id = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password"))
            );
        }

        if (cursor != null) {
            cursor.close();
        }
        return user;
    }

    // PRODUCT METHODS

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                null,
                null,
                null,
                null,
                "id ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                products.add(new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_name"))
                ));
            }
            cursor.close();
        }
        return products;
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String loweredQuery = "%" + query.toLowerCase(Locale.US) + "%";
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                "LOWER(name) LIKE ? OR LOWER(category) LIKE ?",
                new String[]{loweredQuery, loweredQuery},
                null,
                null,
                "id ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                products.add(new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_name"))
                ));
            }
            cursor.close();
        }
        return products;
    }

    public Product getProductById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PRODUCTS,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        Product product = null;
        if (cursor != null && cursor.moveToFirst()) {
            product = new Product(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                    cursor.getString(cursor.getColumnIndexOrThrow("category")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image_name"))
            );
        }

        if (cursor != null) {
            cursor.close();
        }
        return product;
    }

    public void seedProducts() {
        SQLiteDatabase db = this.getWritableDatabase();
        seedProducts(db);
    }

    private void seedProducts(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
        boolean hasProducts = false;
        if (cursor != null && cursor.moveToFirst()) {
            hasProducts = cursor.getInt(0) > 0;
            cursor.close();
        }
        if (hasProducts) {
            return;
        }

        db.beginTransaction();
        try {
            insertProduct(db, "iPhone 15",
                    "The iPhone 15 delivers smooth performance with Apple's latest A-series chip and an advanced dual-camera system. It offers a bright display, strong battery life, and seamless ecosystem integration.",
                    1399.00, "Phones", "iphone15");
            insertProduct(db, "Samsung Galaxy S24",
                    "The Galaxy S24 combines a premium AMOLED screen with fast performance for everyday multitasking and media use. Its camera system captures sharp photos in both daylight and low-light conditions.",
                    1299.00, "Phones", "samsung_s24");
            insertProduct(db, "Google Pixel 8",
                    "The Pixel 8 focuses on clean Android software and smart AI-powered features for photos and voice tools. It provides reliable performance, strong cameras, and timely software updates.",
                    1199.00, "Phones", "google_pixel_8");
            insertProduct(db, "OnePlus 12",
                    "The OnePlus 12 offers flagship-level speed with a high-refresh display and fast charging support. It balances premium hardware with a smooth, customizable Android experience.",
                    1099.00, "Phones", "iphone15");

            insertProduct(db, "MacBook Air M2",
                    "The MacBook Air M2 is lightweight and powerful, making it ideal for productivity, study, and creative tasks on the go. Its battery efficiency and silent design provide a consistently smooth user experience.",
                    1799.00, "Laptops", "macbook_pro");
            insertProduct(db, "Dell XPS 15",
                    "The Dell XPS 15 features a premium build, vivid display options, and strong processing power for demanding workloads. It is well-suited for developers, designers, and creators needing performance and portability.",
                    2299.00, "Laptops", "dell_xps");
            insertProduct(db, "HP Spectre x360",
                    "The HP Spectre x360 is a versatile 2-in-1 laptop with a sleek design and touch-enabled convertible form factor. It delivers dependable performance for work, media, and presentations in multiple modes.",
                    1999.00, "Laptops", "surface_laptop");
            insertProduct(db, "Lenovo ThinkPad X1",
                    "The ThinkPad X1 offers enterprise-grade reliability, a comfortable keyboard, and durable construction for professionals. It provides balanced performance, strong security features, and all-day productivity.",
                    2099.00, "Laptops", "macbook_pro");

            insertProduct(db, "Sony WH-1000XM5",
                    "Sony WH-1000XM5 headphones provide industry-leading noise cancellation and rich, detailed sound. They are designed for long listening sessions with excellent comfort and battery life.",
                    549.00, "Audio", "sony_wh1000xm5");
            insertProduct(db, "AirPods Pro",
                    "AirPods Pro deliver clear sound with active noise cancellation and adaptive transparency features. They pair seamlessly with Apple devices and include a compact charging case for daily use.",
                    399.00, "Audio", "airpods_pro");
            insertProduct(db, "Bose QC45",
                    "Bose QC45 headphones are known for comfort-focused design and effective noise cancellation in busy environments. They produce balanced audio and dependable wireless performance for travel and work.",
                    479.00, "Audio", "bose_quietcomfort");
            insertProduct(db, "Samsung Galaxy Buds",
                    "Galaxy Buds provide portable, high-quality wireless listening with clear call audio and strong connectivity. Their lightweight fit and compact case make them practical for everyday commuting.",
                    249.00, "Audio", "airpods_pro");

            insertProduct(db, "Apple Watch Series 9",
                    "Apple Watch Series 9 adds fast performance, health tracking, and smooth interaction in a polished wearable design. It supports fitness goals, notifications, and convenient daily smart features.",
                    699.00, "Accessories", "apple_watch");
            insertProduct(db, "Samsung Galaxy Watch",
                    "Samsung Galaxy Watch combines stylish hardware with robust health and activity tracking capabilities. It integrates well with Android devices for calls, notifications, and app-based controls.",
                    599.00, "Accessories", "apple_watch");
            insertProduct(db, "Anker PowerBank",
                    "Anker PowerBank offers reliable portable charging with sufficient capacity for phones, earbuds, and small devices. It features durable construction and practical output options for travel.",
                    99.00, "Accessories", "iphone15");
            insertProduct(db, "Logitech MX Master 3",
                    "The MX Master 3 is a productivity mouse with ergonomic comfort and precise tracking across multiple surfaces. It supports customizable controls and seamless switching between paired devices.",
                    169.00, "Accessories", "logitech_g502");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private void insertProduct(SQLiteDatabase db, String name, String description, double price, String category, String imageName) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("price", price);
        values.put("category", category);
        values.put("image_name", imageName);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    // CART METHODS

    public void addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(
                TABLE_CART,
                new String[]{"id", "quantity"},
                "user_id = ? AND product_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int cartId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int existingQty = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            ContentValues values = new ContentValues();
            values.put("quantity", existingQty + quantity);
            db.update(TABLE_CART, values, "id = ?", new String[]{String.valueOf(cartId)});
            cursor.close();
            return;
        }

        if (cursor != null) {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        values.put("quantity", quantity);
        db.insert(TABLE_CART, null, values);
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> cartItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.id, c.user_id, c.product_id, c.quantity, p.name AS product_name, p.price AS product_price " +
                "FROM " + TABLE_CART + " c " +
                "INNER JOIN " + TABLE_PRODUCTS + " p ON c.product_id = p.id " +
                "WHERE c.user_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                cartItems.add(new CartItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("product_price")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity"))
                ));
            }
            cursor.close();
        }
        return cartItems;
    }

    public void updateCartQuantity(int cartId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", quantity);
        db.update(TABLE_CART, values, "id = ?", new String[]{String.valueOf(cartId)});
    }

    public void removeFromCart(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, "id = ?", new String[]{String.valueOf(cartId)});
    }

    public void clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public int getCartCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(quantity), 0) FROM " + TABLE_CART + " WHERE user_id = ?",
                new String[]{String.valueOf(userId)}
        );
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ORDER METHODS

    public long placeOrder(int userId, double totalPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("total_price", totalPrice);
        values.put("order_date", getCurrentDate());
        values.put("status", "Confirmed");
        return db.insert(TABLE_ORDERS, null, values);
    }

    public void addOrderItem(long orderId, int productId, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("order_id", orderId);
        values.put("product_id", productId);
        values.put("quantity", quantity);
        values.put("price", price);
        db.insert(TABLE_ORDER_ITEMS, null, values);
    }

    public List<Order> getOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id, user_id, total_price, order_date, status " +
                "FROM " + TABLE_ORDERS + " " +
                "WHERE user_id = ? AND order_date >= date('now', '-6 months') " +
                "ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                orders.add(new Order(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("total_price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("order_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                ));
            }
            cursor.close();
        }
        return orders;
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new java.util.Date());
    }

    public void clearOrderHistory(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERS, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public long placeDemoOrder(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("total_price", 178.00);
        values.put("order_date", getCurrentDate());
        values.put("status", "Delivered");
        long orderId = db.insert(TABLE_ORDERS, null, values);

        addOrderItem(orderId, 1, 2, 89.00); // Assuming product ID 1 exists
        return orderId;
    }

    public List<OrderItem> getOrderItems(long orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price, p.name AS product_name " +
                "FROM " + TABLE_ORDER_ITEMS + " oi " +
                "INNER JOIN " + TABLE_PRODUCTS + " p ON oi.product_id = p.id " +
                "WHERE oi.order_id = ? " +
                "ORDER BY oi.id ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                items.add(new OrderItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("order_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("product_name")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                ));
            }
            cursor.close();
        }
        return items;
    }
}
