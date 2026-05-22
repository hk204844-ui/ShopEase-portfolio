package com.shopease.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.shopease.app.R;
import com.shopease.app.database.DatabaseHelper;
import com.shopease.app.models.Order;
import com.shopease.app.models.OrderItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final List<Order> orders;
    private final HashMap<Integer, Boolean> expandedStates = new HashMap<>();
    private final HashMap<Integer, List<OrderItem>> orderItemsCache = new HashMap<>();

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        this.orders = new ArrayList<>(orders);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        int orderId = order.getId();
        boolean isExpanded = expandedStates.get(orderId) != null && expandedStates.get(orderId);

        String formattedOrderNumber = String.format(Locale.US, "%03d", orderId);
        holder.tvOrderNumber.setText(context.getString(R.string.order_number, formattedOrderNumber));
        holder.tvStatus.setText(order.getStatus());
        holder.tvOrderDate.setText(formatDisplayDate(order.getOrderDate()));
        holder.tvTotalPrice.setText(context.getString(R.string.product_price_format, order.getTotalPrice()));

        List<OrderItem> cachedItems = orderItemsCache.get(orderId);
        if (cachedItems != null) {
            holder.tvItemCount.setText(context.getString(R.string.items_count, getTotalQuantity(cachedItems)));
        } else {
            holder.tvItemCount.setText(R.string.tap_to_expand);
        }

        setExpandedState(holder, orderId, isExpanded);

        View.OnClickListener toggleListener = v -> {
            boolean currentState = expandedStates.get(orderId) != null && expandedStates.get(orderId);
            boolean newState = !currentState;
            expandedStates.put(orderId, newState);
            setExpandedState(holder, orderId, newState);
        };

        holder.layoutOrderRoot.setOnClickListener(toggleListener);
        holder.ivExpandArrow.setOnClickListener(toggleListener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private void setExpandedState(OrderViewHolder holder, int orderId, boolean expanded) {
        holder.layoutOrderItems.setVisibility(expanded ? View.VISIBLE : View.GONE);
        holder.ivExpandArrow.animate().rotation(expanded ? 180f : 0f).setDuration(180).start();

        if (expanded) {
            List<OrderItem> orderItems = orderItemsCache.get(orderId);
            if (orderItems == null) {
                orderItems = databaseHelper.getOrderItems(orderId);
                orderItemsCache.put(orderId, orderItems);
            }
            populateOrderItems(holder.layoutOrderItems, orderItems);
            holder.tvItemCount.setText(context.getString(R.string.items_count, getTotalQuantity(orderItems)));
        }
    }

    private void populateOrderItems(LinearLayout container, List<OrderItem> items) {
        container.removeAllViews();
        if (items == null || items.isEmpty()) {
            TextView emptyText = new TextView(context);
            emptyText.setText(R.string.no_order_items);
            emptyText.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            container.addView(emptyText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        for (OrderItem item : items) {
            View row = inflater.inflate(R.layout.item_order_product, container, false);
            TextView tvName = row.findViewById(R.id.tvProductName);
            TextView tvQty = row.findViewById(R.id.tvQuantity);
            TextView tvSubtotal = row.findViewById(R.id.tvSubtotal);

            tvName.setText(item.getProductName());
            tvQty.setText(context.getString(R.string.order_item_quantity, item.getQuantity()));
            tvSubtotal.setText(context.getString(R.string.cart_item_subtotal, item.getSubtotal()));

            container.addView(row);
        }
    }

    private int getTotalQuantity(List<OrderItem> items) {
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    private String formatDisplayDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.US);
            Date parsed = parser.parse(rawDate);
            return parsed == null ? rawDate : formatter.format(parsed);
        } catch (ParseException e) {
            return rawDate;
        }
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutOrderRoot;
        TextView tvOrderNumber;
        TextView tvStatus;
        TextView tvOrderDate;
        TextView tvItemCount;
        TextView tvTotalPrice;
        ImageView ivExpandArrow;
        LinearLayout layoutOrderItems;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutOrderRoot = itemView.findViewById(R.id.layoutOrderRoot);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            ivExpandArrow = itemView.findViewById(R.id.ivExpandArrow);
            layoutOrderItems = itemView.findViewById(R.id.layoutOrderItems);
        }
    }
}
