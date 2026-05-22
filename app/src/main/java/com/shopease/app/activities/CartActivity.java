package com.shopease.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.shopease.app.R;
import com.shopease.app.adapters.CartAdapter;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.CartItem;
import com.shopease.app.utils.SessionManager;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private CartAdapter cartAdapter;
    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutBottomBar;
    private TextView tvTotal;
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
        userId = sessionManager.getUserId();
        setContentView(R.layout.activity_cart);
        databaseHelper = new DatabaseHelper(this);

        setupToolbar();
        bindViews();
        setupRecyclerView();
        loadCartItems();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_cart_title);
        }
    }

    private void bindViews() {
        rvCartItems = findViewById(R.id.rvCartItems);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        layoutBottomBar = findViewById(R.id.layoutBottomBar);
        tvTotal = findViewById(R.id.tvTotal);
        MaterialButton btnCheckout = findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> startActivity(new Intent(this, CheckoutActivity.class)));
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(
                this,
                new ArrayList<>(),
                this::onQuantityChanged,
                this::onDeleteItem
        );
        rvCartItems.setAdapter(cartAdapter);
    }

    private void onQuantityChanged(int cartId, int newQuantity) {
        databaseHelper.updateCartQuantity(cartId, newQuantity);
        loadCartItems();
    }

    private void onDeleteItem(int cartId) {
        databaseHelper.removeFromCart(cartId);
        loadCartItems();
    }

    private void loadCartItems() {
        List<CartItem> cartItems = databaseHelper.getCartItems(userId);
        cartAdapter.updateList(cartItems);
        updateCartUi(cartItems);
    }

    private void updateCartUi(List<CartItem> cartItems) {
        boolean isEmpty = cartItems == null || cartItems.isEmpty();
        rvCartItems.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        layoutBottomBar.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (!isEmpty) {
            tvTotal.setText(calculateTotal(cartItems));
        }
    }

    private String calculateTotal(List<CartItem> items) {
        double total = 0;
        if (items != null) {
            for (CartItem item : items) {
                total += item.getSubtotal();
            }
        }
        return String.format(Locale.US, getString(R.string.total_label_pattern), total);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
