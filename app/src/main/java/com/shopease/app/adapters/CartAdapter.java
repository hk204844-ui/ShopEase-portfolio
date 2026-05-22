package com.shopease.app.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.shopease.app.R;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.CartItem;
import com.shopease.app.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnQuantityChangeListener {
        void onQuantityChange(int cartId, int newQuantity);
    }

    public interface OnDeleteListener {
        void onDelete(int cartId);
    }

    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final List<CartItem> cartItems;
    private final OnQuantityChangeListener quantityChangeListener;
    private final OnDeleteListener deleteListener;

    public CartAdapter(Context context,
                       List<CartItem> cartItems,
                       OnQuantityChangeListener quantityChangeListener,
                       OnDeleteListener deleteListener) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.cartItems = new ArrayList<>(cartItems);
        this.quantityChangeListener = quantityChangeListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.tvProductName.setText(cartItem.getProductName());
        holder.tvPrice.setText(context.getString(R.string.product_price_format, cartItem.getProductPrice()));
        holder.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
        holder.tvSubtotal.setText(context.getString(R.string.cart_item_subtotal, cartItem.getSubtotal()));

        loadProductImage(cartItem, holder.ivProductImage);

        holder.btnMinus.setOnClickListener(v -> {
            if (cartItem.getQuantity() <= 1) {
                if (deleteListener != null) {
                    deleteListener.onDelete(cartItem.getCartId());
                }
            } else if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChange(cartItem.getCartId(), cartItem.getQuantity() - 1);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            if (quantityChangeListener != null) {
                quantityChangeListener.onQuantityChange(cartItem.getCartId(), cartItem.getQuantity() + 1);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(cartItem.getCartId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateList(List<CartItem> newList) {
        cartItems.clear();
        cartItems.addAll(newList);
        notifyDataSetChanged();
    }

    private void loadProductImage(CartItem cartItem, ImageView imageView) {
        Product product = databaseHelper.getProductById(cartItem.getProductId());
        int imageResId = 0;
        if (product != null) {
            imageResId = context.getResources().getIdentifier(
                    product.getImageName(),
                    "drawable",
                    context.getPackageName()
            );
        }
        ColorDrawable placeholder = new ColorDrawable(ContextCompat.getColor(context, R.color.light_gray));
        Glide.with(context)
                .load(imageResId == 0 ? null : imageResId)
                .placeholder(placeholder)
                .error(placeholder)
                .into(imageView);
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvPrice;
        TextView tvQuantity;
        TextView tvSubtotal;
        MaterialButton btnMinus;
        MaterialButton btnPlus;
        ImageButton btnDelete;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
