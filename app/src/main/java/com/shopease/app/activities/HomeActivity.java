package com.shopease.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.shopease.app.R;
import com.shopease.app.adapters.ProductAdapter;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.Product;
import com.shopease.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private MaterialToolbar toolbar;
    private ProductAdapter productAdapter;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        userId = sessionManager.getUserId();
        databaseHelper = new DatabaseHelper(this);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.home_title);
        }

        rvProducts = findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));

        List<Product> products = databaseHelper.getAllProducts();
        productAdapter = new ProductAdapter(
                this,
                products == null ? new ArrayList<>() : products,
                this::openProductDetail,
                this::addProductToCart
        );
        rvProducts.setAdapter(productAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartBadge();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        refreshCartBadge();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }
        if (itemId == R.id.action_search) {
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        if (itemId == R.id.action_my_orders) {
            startActivity(new Intent(this, PurchaseHistoryActivity.class));
            return true;
        }
        if (itemId == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addProductToCart(Product product) {
        databaseHelper.addToCart(userId, product.getId(), 1);
        Toast.makeText(this, R.string.added_to_cart, Toast.LENGTH_SHORT).show();
        refreshCartBadge();
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void refreshCartBadge() {
        if (toolbar == null) {
            return;
        }
        Menu menu = toolbar.getMenu();
        if (menu == null || menu.findItem(R.id.action_cart) == null) {
            return;
        }

        int count = databaseHelper != null ? databaseHelper.getCartCount(userId) : 0;
        BadgeDrawable badgeDrawable = BadgeDrawable.create(this);
        badgeDrawable.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
        badgeDrawable.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));
        badgeDrawable.setVisible(count > 0);
        badgeDrawable.setNumber(count);
        BadgeUtils.attachBadgeDrawable(badgeDrawable, toolbar, R.id.action_cart);
    }

    private void redirectToLoginAndFinish() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
