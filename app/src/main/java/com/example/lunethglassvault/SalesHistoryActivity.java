package com.example.lunethglassvault;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SalesHistoryActivity extends AppCompatActivity {

    private RecyclerView rvSalesHistory;
    private SalesHistoryAdapter adapter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        rvSalesHistory = findViewById(R.id.rvSalesHistory);
        rvSalesHistory.setLayoutManager(new LinearLayoutManager(this));

        realm = Realm.getDefaultInstance();

        // Fetch ALL transactions across all users, newest first
        RealmResults<SaleLog> allSales = realm.where(SaleLog.class)
                .sort("timestamp", Sort.DESCENDING)
                .findAll();


        adapter = new SalesHistoryAdapter(this, allSales);
        rvSalesHistory.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // keep view updated if new sales are logged while activity is open
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}