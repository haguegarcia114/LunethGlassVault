package com.example.lunethglassvault;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class FacturaAdapter extends RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder> {

    private final Context context;
    private final List<FacturaItem> itemList;

    public FacturaAdapter(Context context, List<FacturaItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public FacturaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_factura_card, parent, false);
        return new FacturaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FacturaViewHolder holder, int position) {
        FacturaItem item = itemList.get(position);

        if (item != null) {
            holder.tvTitle.setText(item.getName());
            holder.tvSupplierAndFactura.setText(String.format("%s | Factura: %s",
                    item.getSupplier() != null ? item.getSupplier() : "Unknown",
                    item.getFacturaNumber() != null ? item.getFacturaNumber() : "N/A"));
            holder.tvCode.setText("Code: " + item.getCode());

            // displays price in peso with quantity
            holder.tvPrice.setText(String.format(Locale.getDefault(), "₱%.2f | Stock: %d", item.getPrice(), item.getStockQuantity()));

            // load saved image
            if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
                try {
                    holder.imgItemPhoto.setImageURI(Uri.parse(item.getImageUri()));
                } catch (Exception e) {
                    holder.imgItemPhoto.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } else {
                holder.imgItemPhoto.setImageResource(android.R.drawable.ic_menu_camera);
            }

            // navigate to FacturaDetailActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, FacturaDetailActivity.class);
                intent.putExtra("productCode", item.getCode());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public void updateList(List<FacturaItem> newList) {
        this.itemList.clear();
        if (newList != null) {
            this.itemList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    public static class FacturaViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSupplierAndFactura, tvCode, tvPrice;
        ImageView imgItemPhoto;

        public FacturaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSupplierAndFactura = itemView.findViewById(R.id.tvSupplierAndFactura);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgItemPhoto = itemView.findViewById(R.id.imgItemPhoto);
        }
    }
}