package com.shopease.app.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.button.MaterialButton;
import com.shopease.app.R;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.Product;
import com.shopease.app.utils.SessionManager;

public class ProductDetailActivity extends AppCompatActivity {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10;

    private MaterialToolbar toolbar;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private Product product;
    private int userId;
    private int selectedQuantity = 1;

    private TextView tvQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        userId = sessionManager.getUserId();
        setContentView(R.layout.activity_product_detail);

        int productId = getIntent().getIntExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        product = databaseHelper.getProductById(productId);
        if (product == null) {
            Toast.makeText(this, R.string.product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(product.getCategory());
        }

        bindViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCartBadge();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_product_detail, menu);
        refreshCartBadge();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.action_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        ImageView ivProductImage = findViewById(R.id.ivProductImage);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvPrice = findViewById(R.id.tvPrice);
        TextView tvDescription = findViewById(R.id.tvDescription);
        MaterialButton btnMinus = findViewById(R.id.btnMinus);
        MaterialButton btnPlus = findViewById(R.id.btnPlus);
        MaterialButton btnAddToCart = findViewById(R.id.btnAddToCart);
        tvQuantity = findViewById(R.id.tvQuantity);

        tvCategory.setText(product.getCategory());
        tvProductName.setText(product.getName());
        tvPrice.setText(getString(R.string.product_price_format, product.getPrice()));
        tvDescription.setText(product.getDescription());
        tvQuantity.setText(String.valueOf(selectedQuantity));

        int imageResId = getResources().getIdentifier(product.getImageName(), "drawable", getPackageName());
        ColorDrawable placeholder = new ColorDrawable(ContextCompat.getColor(this, R.color.light_gray));
        Glide.with(this)
                .load(imageResId == 0 ? null : imageResId)
                .placeholder(placeholder)
                .error(placeholder)
                .into(ivProductImage);

        btnMinus.setOnClickListener(v -> {
            selectedQuantity = Math.max(MIN_QUANTITY, selectedQuantity - 1);
            tvQuantity.setText(String.valueOf(selectedQuantity));
        });

        btnPlus.setOnClickListener(v -> {
            selectedQuantity = Math.min(MAX_QUANTITY, selectedQuantity + 1);
            tvQuantity.setText(String.valueOf(selectedQuantity));
        });

        btnAddToCart.setOnClickListener(v -> {
            databaseHelper.addToCart(userId, product.getId(), selectedQuantity);
            Toast.makeText(this, R.string.added_to_cart_successfully, Toast.LENGTH_SHORT).show();
            animateAddButton(btnAddToCart);
            refreshCartBadge();
        });
    }

    private void animateAddButton(MaterialButton button) {
        button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
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
}
