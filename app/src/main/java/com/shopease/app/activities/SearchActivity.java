package com.shopease.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.shopease.app.R;
import com.shopease.app.adapters.ProductAdapter;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.Product;
import com.shopease.app.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private TextView tvEmptyState;
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
        setContentView(R.layout.activity_search);

        databaseHelper = new DatabaseHelper(this);
        setupToolbar();
        setupListAndSearch();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.search_products_title);
        }
    }

    private void setupListAndSearch() {
        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        TextInputEditText etSearch = findViewById(R.id.etSearch);
        etSearch.requestFocus();
        etSearch.post(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(etSearch, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        allProducts = databaseHelper.getAllProducts();
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(
                this,
                allProducts,
                this::openProductDetail,
                this::addProductToCart
        );
        rvProducts.setAdapter(productAdapter);
        toggleEmptyState(allProducts);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s == null ? "" : s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void filterProducts(String query) {
        String trimmedQuery = query == null ? "" : query.trim();
        List<Product> filteredList;
        if (trimmedQuery.isEmpty()) {
            filteredList = allProducts;
        } else {
            filteredList = databaseHelper.searchProducts(trimmedQuery);
        }
        productAdapter.updateList(filteredList);
        toggleEmptyState(filteredList);
    }

    private void toggleEmptyState(List<Product> list) {
        boolean isEmpty = list == null || list.isEmpty();
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void addProductToCart(Product product) {
        databaseHelper.addToCart(userId, product.getId(), 1);
        Toast.makeText(this, R.string.added_to_cart, Toast.LENGTH_SHORT).show();
    }
}
