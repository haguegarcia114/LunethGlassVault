package com.example.lunethglassvault;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvAdminUserList;
    private Button btnAddUser, btnClearUsers, btnBack;
    private UserAdapter userAdapter;
    private Realm realm;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        realm = Realm.getDefaultInstance();

        // views
        rvAdminUserList = findViewById(R.id.rvAdminUserList);
        btnAddUser = findViewById(R.id.btnAddUser);
        btnClearUsers = findViewById(R.id.btnClearUsers);
        btnBack = findViewById(R.id.btnBack);

        rvAdminUserList.setLayoutManager(new LinearLayoutManager(this));

        // init adapter
        userAdapter = new UserAdapter(this, userList);
        rvAdminUserList.setAdapter(userAdapter);

        // navigations for admin (has back)
        btnBack.setOnClickListener(v -> finish());

        btnAddUser.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, RegisterUserActivity.class))
        );

        btnClearUsers.setOnClickListener(v -> {
            realm.executeTransaction(r -> {
                // delete all users except admin
                r.where(User.class)
                        .notEqualTo("username", "admin")
                        .findAll()
                        .deleteAllFromRealm();
            });
            loadUserList();
            Toast.makeText(this, "Cleared all non-admin users", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserList() {
        if (realm != null && !realm.isClosed()) {
            userList.clear();

            userList.addAll(realm.copyFromRealm(realm.where(User.class).findAll()));
            if (userAdapter != null) {
                userAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reloads when you return
        loadUserList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}