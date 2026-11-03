package com.example.lunethglassvault;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class FacturaCatalog extends AppCompatActivity {

    private RecyclerView rvFacturaCatalog;
    private FacturaAdapter adapter;
    private Realm realm;
    private RealmResults<FacturaItem> facturaItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura_catalog);

        rvFacturaCatalog = findViewById(R.id.rvFacturaCatalog);
        rvFacturaCatalog.setLayoutManager(new LinearLayoutManager(this));


        realm = Realm.getDefaultInstance();

        facturaItems = realm.where(FacturaItem.class).findAll();

        // live results for adapter
        adapter = new FacturaAdapter(this, facturaItems);
        rvFacturaCatalog.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Live RealmResults update
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