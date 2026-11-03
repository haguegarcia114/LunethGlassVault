package com.example.lunethglassvault;

import android.app.Application;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Realm
        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("lunethglassvault.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .build();

        Realm.setDefaultConfiguration(config);
    }
}