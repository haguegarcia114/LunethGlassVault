package com.example.lunethglassvault;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class SalesHistoryAdapter extends RecyclerView.Adapter<SalesHistoryAdapter.SaleViewHolder> {

    private final Context context;
    private final List<SaleLog> salesList;

    public SalesHistoryAdapter(Context context, List<SaleLog> salesList) {
        this.context = context;
        this.salesList = salesList;
    }

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sale_history_card, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        SaleLog sale = salesList.get(position);

        holder.tvSaleItemCode.setText("Item Code: " + sale.getItemCode());
        holder.tvSaleFactura.setText("Factura #: " + sale.getFacturaNumber());
        holder.tvSaleTotalPrice.setText(String.format("P%.2f", sale.getTotalPrice()));
        holder.tvSaleQuantity.setText("Qty Sold: " + sale.getQuantitySold());

        // date and time formatting
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(sale.getTimestamp());
        String dateString = DateFormat.format("MMM dd, yyyy hh:mm a", cal).toString();
        holder.tvSaleTimestamp.setText(dateString);
    }

    @Override
    public int getItemCount() {
        return salesList != null ? salesList.size() : 0;
    }

    public static class SaleViewHolder extends RecyclerView.ViewHolder {
        TextView tvSaleItemCode, tvSaleFactura, tvSaleTotalPrice, tvSaleQuantity, tvSaleTimestamp;

        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSaleItemCode = itemView.findViewById(R.id.tvSaleItemCode);
            tvSaleFactura = itemView.findViewById(R.id.tvSaleFactura);
            tvSaleTotalPrice = itemView.findViewById(R.id.tvSaleTotalPrice);
            tvSaleQuantity = itemView.findViewById(R.id.tvSaleQuantity);
            tvSaleTimestamp = itemView.findViewById(R.id.tvSaleTimestamp);
        }
    }
}