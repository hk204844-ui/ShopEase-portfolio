package com.shopease.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.shopease.app.R;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.CartItem;
import com.shopease.app.utils.SessionManager;

import java.util.Locale;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private int userId;

    private TextInputEditText etFullName;
    private TextInputEditText etAddress;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextView tvUniqueItems;
    private TextView tvTotalItems;
    private TextView tvTotalPrice;

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
        setContentView(R.layout.activity_checkout);
        databaseHelper = new DatabaseHelper(this);

        List<CartItem> cartItems = databaseHelper.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, R.string.your_cart_is_empty, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        bindViews();
        prefillSessionData();
        updateOrderSummary(cartItems);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.checkout_title);
        }
    }

    private void bindViews() {
        etFullName = findViewById(R.id.etFullName);
        etAddress = findViewById(R.id.etAddress);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        tvUniqueItems = findViewById(R.id.tvUniqueItems);
        tvTotalItems = findViewById(R.id.tvTotalItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        MaterialButton btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void prefillSessionData() {
        etFullName.setText(sessionManager.getUserName());
        etEmail.setText(sessionManager.getUserEmail());
    }

    private void updateOrderSummary(List<CartItem> cartItems) {
        int uniqueItems = cartItems == null ? 0 : cartItems.size();
        int totalQty = 0;
        double totalPrice = 0;
        for (CartItem item : cartItems) {
            totalQty += item.getQuantity();
            totalPrice += item.getSubtotal();
        }
        tvUniqueItems.setText(getString(R.string.unique_items, uniqueItems));
        tvTotalItems.setText(getString(R.string.total_quantity, totalQty));
        tvTotalPrice.setText(String.format(Locale.US, getString(R.string.cart_item_subtotal), totalPrice));
    }

    private void placeOrder() {
        String fullName = getText(etFullName);
        String address = getText(etAddress);
        String phone = getText(etPhone);
        String email = getText(etEmail);

        if (fullName.isEmpty() || address.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, R.string.all_fields_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!phone.matches("\\d{8,}")) {
            Toast.makeText(this, R.string.invalid_phone, Toast.LENGTH_SHORT).show();
            return;
        }

        List<CartItem> cartItems = databaseHelper.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, R.string.your_cart_is_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotal();
        }

        long orderId = databaseHelper.placeOrder(userId, total);
        for (CartItem item : cartItems) {
            databaseHelper.addOrderItem(orderId, item.getProductId(), item.getQuantity(), item.getProductPrice());
        }
        databaseHelper.clearCart(userId);

        new AlertDialog.Builder(this)
                .setTitle(R.string.order_placed)
                .setMessage(R.string.order_success_message)
                .setCancelable(false)
                .setPositiveButton(R.string.continue_shopping, (dialog, which) -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
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
