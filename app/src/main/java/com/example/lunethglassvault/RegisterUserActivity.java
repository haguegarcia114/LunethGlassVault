package com.example.lunethglassvault;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import io.realm.Realm;

public class RegisterUserActivity extends AppCompatActivity {

    private ImageView imgRegisterUserPhoto;
    private Button btnTakePhoto, btnSaveNewUser;
    private EditText etRegUsername, etRegFullName, etRegPassword, etRegRole;

    private Realm realm;
    private Uri photoUri;
    private String editingUserUuid = null;

    private final ActivityResultLauncher<Intent> imageActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == ImageActivity.RESULT_CODE_IMAGE_TAKEN && result.getData() != null) {
                    byte[] rawJpeg = result.getData().getByteArrayExtra("rawJpeg");
                    if (rawJpeg != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(rawJpeg, 0, rawJpeg.length);
                        imgRegisterUserPhoto.setImageBitmap(bitmap);
                        saveCroppedImageToFile(rawJpeg);
                    }
                }
            });

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchImageActivity();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        realm = Realm.getDefaultInstance();

        imgRegisterUserPhoto = findViewById(R.id.imgRegisterUserPhoto);
        btnTakePhoto = findViewById(R.id.btnSelectPhoto);
        btnSaveNewUser = findViewById(R.id.btnSaveNewUser);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegFullName = findViewById(R.id.etRegFullName);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegRole = findViewById(R.id.etRegRole);

        if (getIntent().hasExtra("userUuid")) {
            editingUserUuid = getIntent().getStringExtra("userUuid");
            if (editingUserUuid != null && !editingUserUuid.isEmpty()) {
                preFillUserData(editingUserUuid);
            }
        }

        imgRegisterUserPhoto.setOnClickListener(v -> checkPermissionAndLaunchImageActivity());
        btnTakePhoto.setOnClickListener(v -> checkPermissionAndLaunchImageActivity());

        btnSaveNewUser.setOnClickListener(v -> saveUserToRealm());
    }

    private void preFillUserData(String uuid) {
        User user = realm.where(User.class).equalTo("uuid", uuid).findFirst();
        if (user != null && user.isValid()) {
            etRegUsername.setText(user.getUsername());
            etRegFullName.setText(user.getFullName());
            etRegPassword.setText(user.getPassword());
            etRegRole.setText(user.getRole());

            if (user.getImageUri() != null && !user.getImageUri().isEmpty()) {
                photoUri = Uri.parse(user.getImageUri());
                imgRegisterUserPhoto.setImageURI(photoUri);
            }

            btnSaveNewUser.setText("Update User");
        }
    }

    private void checkPermissionAndLaunchImageActivity() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchImageActivity();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchImageActivity() {
        Intent intent = new Intent(this, ImageActivity.class);
        imageActivityLauncher.launch(intent);
    }

    private void saveCroppedImageToFile(byte[] jpegBytes) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = new File(storageDir, "USER_PHOTO_" + timeStamp + ".jpg");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jpegBytes);
            fos.flush();
            fos.close();

            photoUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUserToRealm() {
        String username = etRegUsername.getText().toString().trim();
        String fullName = etRegFullName.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String role = etRegRole.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and Password are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        final String imageUriString = (photoUri != null) ? photoUri.toString() : null;

        realm.executeTransaction(r -> {
            User user = null;

            if (editingUserUuid != null) {
                user = r.where(User.class).equalTo("uuid", editingUserUuid).findFirst();
            }

            // Fallback / Create Mode
            if (user == null) {
                String newUuid = UUID.randomUUID().toString();

                try {
                    user = r.createObject(User.class, newUuid);
                } catch (Exception e) {
                    user = r.createObject(User.class);
                    user.setUuid(newUuid);
                }
            }

            if (user != null) {
                user.setUsername(username);
                user.setPassword(password);
                user.setFullName(fullName.isEmpty() ? username : fullName);
                user.setRole(role.isEmpty() ? "Staff" : role);

                if (imageUriString != null) {
                    user.setImageUri(imageUriString);
                }
            }
        });

        Toast.makeText(this, editingUserUuid != null ? "User updated!" : "User registered!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
    }
}