package com.example.lunethglassvault;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class EmployeeProfileActivity extends AppCompatActivity {

    private TextView tvEmployeeName, tvEmployeeRole, tvEmployeeUuid;
    private ImageView imgProfilePhoto;
    private Button btnLogout;
    private Realm realm;

    private String currentUuid;
    private String currentUsername;
    private Uri tempCameraUri;

    // activity launchers
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);

        realm = Realm.getDefaultInstance();


        tvEmployeeName = findViewById(R.id.tvEmployeeName);
        tvEmployeeRole = findViewById(R.id.tvEmployeeRole);
        tvEmployeeUuid = findViewById(R.id.tvEmployeeUuid);
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        btnLogout = findViewById(R.id.btnLogout);

        // register
        setupActivityResultLaunchers();

        // to load profile
        loadEmployeeProfile();

        // make the photo clickable (so u can change it)
        if (imgProfilePhoto != null) {
            imgProfilePhoto.setOnClickListener(v -> showImageOptionDialog());
        }

        // logout
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void setupActivityResultLaunchers() {
        // launch the camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && tempCameraUri != null) {
                        saveAndDisplayNewProfileImage(tempCameraUri.toString());
                    }
                }
        );

        // permissions for reading gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        // Take persistable permission so URI stays accessible across app reboots
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (Exception ignored) {}

                        saveAndDisplayNewProfileImage(uri.toString());
                    }
                }
        );

        // permissions for camera
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void loadEmployeeProfile() {
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        currentUuid = sharedPref.getString("USER_UUID", null);
        currentUsername = sharedPref.getString("USER_NAME", sharedPref.getString("SAVED_USERNAME", null));

        User user = getUserRecord();

        if (user != null) {
            // details
            tvEmployeeName.setText(user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : user.getUsername());
            tvEmployeeRole.setText("Role: " + (user.getRole() != null ? user.getRole() : "Sales Associate"));
            tvEmployeeUuid.setText("User ID: " + user.getUuid());

            // if there is photo, display the image
            displayProfileImage(user.getImageUri());

        } else if (currentUsername != null) {
            tvEmployeeName.setText(currentUsername);
            tvEmployeeRole.setText("Role: Sales Associate");
            tvEmployeeUuid.setText("User ID: " + (currentUuid != null ? currentUuid : "N/A"));
        } else {
            Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }
    }

    private User getUserRecord() {
        User user = null;
        if (currentUuid != null) {
            user = realm.where(User.class).equalTo("uuid", currentUuid).findFirst();
        }
        if (user == null && currentUsername != null) {
            user = realm.where(User.class).equalTo("username", currentUsername).findFirst();
        }
        return user;
    }

    private void displayProfileImage(String imageUriStr) {
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                imgProfilePhoto.setImageURI(Uri.parse(imageUriStr));
            } catch (Exception e) {

            }
        }
    }

    private void showImageOptionDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        checkCameraPermissionAndOpen();
                    } else if (which == 1) {
                        galleryLauncher.launch("image/*");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;

        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            tempCameraUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    photoFile
            );
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void saveAndDisplayNewProfileImage(String imageUriString) {
        User user = getUserRecord();

        if (user != null) {
            realm.executeTransaction(r -> user.setImageUri(imageUriString));
            displayProfileImage(imageUriString);
            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to save photo: User record missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void performLogout() {
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(EmployeeProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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