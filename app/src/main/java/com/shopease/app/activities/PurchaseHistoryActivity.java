package com.shopease.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.shopease.app.R;
import com.shopease.app.adapters.OrderAdapter;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.Order;
import com.shopease.app.utils.SessionManager;

import java.util.List;

public class PurchaseHistoryActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper databaseHelper;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmptyState;
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
        setContentView(R.layout.activity_purchase_history);
        databaseHelper = new DatabaseHelper(this);

        setupToolbar();
        bindViews();
        loadOrders();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.my_orders_title);
        }
    }

    private void bindViews() {
        rvOrders = findViewById(R.id.rvOrders);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        MaterialButton btnAddDemoOrder = findViewById(R.id.btnAddDemoOrder);
        MaterialButton btnUserManual = findViewById(R.id.btnUserManual);
        MaterialButton btnClearHistory = findViewById(R.id.btnClearHistory);

        btnAddDemoOrder.setOnClickListener(v -> {
            databaseHelper.placeDemoOrder(userId);
            loadOrders();
            Toast.makeText(this, R.string.demo_order_added, Toast.LENGTH_SHORT).show();
        });

        btnUserManual.setOnClickListener(v -> {
            startActivity(new Intent(this, UserManualActivity.class));
        });

        btnClearHistory.setOnClickListener(v -> {
            databaseHelper.clearOrderHistory(userId);
            loadOrders();
            Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadOrders() {
        List<Order> orders = databaseHelper.getOrderHistory(userId);
        boolean hasOrders = orders != null && !orders.isEmpty();
        rvOrders.setVisibility(hasOrders ? View.VISIBLE : View.GONE);
        layoutEmptyState.setVisibility(hasOrders ? View.GONE : View.VISIBLE);
        if (hasOrders) {
            rvOrders.setAdapter(new OrderAdapter(this, orders));
        } else {
            rvOrders.setAdapter(null);
        }
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
