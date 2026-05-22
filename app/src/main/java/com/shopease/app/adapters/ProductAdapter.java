package com.shopease.app.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.shopease.app.R;
import com.shopease.app.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    private final Context context;
    private final List<Product> productList;
    private final OnProductClickListener onProductClickListener;
    private final OnAddToCartListener onAddToCartListener;

    public ProductAdapter(Context context,
                          List<Product> productList,
                          OnProductClickListener onProductClickListener,
                          OnAddToCartListener onAddToCartListener) {
        this.context = context;
        this.productList = new ArrayList<>(productList);
        this.onProductClickListener = onProductClickListener;
        this.onAddToCartListener = onAddToCartListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvCategory.setText(product.getCategory());
        holder.tvProductName.setText(product.getName());
        holder.tvPrice.setText(context.getString(R.string.product_price_format, product.getPrice()));

        int imageResId = context.getResources().getIdentifier(
                product.getImageName(),
                "drawable",
                context.getPackageName()
        );
        ColorDrawable placeholder = new ColorDrawable(ContextCompat.getColor(context, R.color.light_gray));

        Glide.with(context)
                .load(imageResId == 0 ? null : imageResId)
                .placeholder(placeholder)
                .error(placeholder)
                .into(holder.ivProductImage);

        holder.itemView.setOnClickListener(v -> {
            if (onProductClickListener != null) {
                onProductClickListener.onProductClick(product);
            }
        });
        holder.btnAddToCart.setOnClickListener(v -> {
            if (onAddToCartListener != null) {
                onAddToCartListener.onAddToCart(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        productList.clear();
        productList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvCategory;
        TextView tvProductName;
        TextView tvPrice;
        MaterialButton btnAddToCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
