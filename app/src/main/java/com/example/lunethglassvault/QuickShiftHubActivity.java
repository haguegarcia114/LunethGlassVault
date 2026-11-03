package com.example.lunethglassvault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;

public class QuickShiftHubActivity extends AppCompatActivity {

    private TextView tvWelcomeEmployee;
    private TextView tvTotalItemsSold;
    private Button btnLogNewSale;
    private Button btnViewFacturas;
    private Button btnSalesHistory;
    private Button btnUserProfile;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_hub);

        realm = Realm.getDefaultInstance();

        // Bind Views
        tvWelcomeEmployee = findViewById(R.id.tvWelcomeEmployee);
        tvTotalItemsSold = findViewById(R.id.tvTotalItemsSold);
        btnLogNewSale = findViewById(R.id.btnLogNewSale);
        btnViewFacturas = findViewById(R.id.btnViewFacturas);
        btnSalesHistory = findViewById(R.id.btnSalesHistory);
        btnUserProfile = findViewById(R.id.btnUserProfile);

        // Header User Setup
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String savedUser = sharedPref.getString("USER_NAME", sharedPref.getString("SAVED_USERNAME", "User"));
        tvWelcomeEmployee.setText("Welcome, " + savedUser + "!");

        // Navigation Actions
        btnLogNewSale.setOnClickListener(v ->
                startActivity(new Intent(QuickShiftHubActivity.this, LogSaleActivity.class))
        );

        btnSalesHistory.setOnClickListener(v ->
                startActivity(new Intent(QuickShiftHubActivity.this, SalesHistoryActivity.class))
        );

        btnUserProfile.setOnClickListener(v ->
                startActivity(new Intent(QuickShiftHubActivity.this, EmployeeProfileActivity.class))
        );

        btnViewFacturas.setOnClickListener(v ->
                startActivity(new Intent(QuickShiftHubActivity.this, FacturaCatalog.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (realm == null || realm.isClosed()) {
            realm = Realm.getDefaultInstance();
        }
        updateDailySalesCount();
    }

    private void updateDailySalesCount() {
        if (realm == null || realm.isClosed()) return;

        long startOfToday = getStartOfTodayTimestamp();

        RealmResults<SaleLog> todaySales = realm.where(SaleLog.class)
                .greaterThanOrEqualTo("timestamp", startOfToday)
                .findAll();

        int totalItemsSoldToday = 0;
        for (SaleLog sale : todaySales) {
            totalItemsSoldToday += sale.getQuantitySold();
        }

        tvTotalItemsSold.setText("Total Items Sold Today: " + totalItemsSoldToday);
    }

    private long getStartOfTodayTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}