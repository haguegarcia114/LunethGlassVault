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
import android.widget.ImageView;
import android.widget.TextView;
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

import io.realm.Realm;

public class FacturaDetailActivity extends AppCompatActivity {

    private ImageView imgProductPhoto;
    private TextView tvProductName, tvProductPrice, tvProductCode, tvFacturaNum, tvSupplier, tvStock;
    private Realm realm;
    private Button btnSellThisItem;
    private String productCode;

    // image activity launcher from canvas
    private final ActivityResultLauncher<Intent> imageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == ImageActivity.RESULT_CODE_IMAGE_TAKEN && result.getData() != null) {
                    byte[] rawJpeg = result.getData().getByteArrayExtra("rawJpeg");
                    if (rawJpeg != null) {
                        // Display bitmap immediately
                        Bitmap bitmap = BitmapFactory.decodeByteArray(rawJpeg, 0, rawJpeg.length);
                        imgProductPhoto.setImageBitmap(bitmap);

                        // Save image to file system & Realm
                        Uri newImageUri = saveImageToFile(rawJpeg);
                        if (newImageUri != null) {
                            updateProductImageInRealm(newImageUri.toString());
                        }
                    }
                }
            });

    // this launches the camera
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchImageActivity();
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura_detail);

        realm = Realm.getDefaultInstance();

        // views
        imgProductPhoto = findViewById(R.id.imgProductPhoto);
        tvProductName = findViewById(R.id.tvDetailName);
        tvProductPrice = findViewById(R.id.tvDetailPrice);
        tvProductCode = findViewById(R.id.tvDetailCode);
        tvFacturaNum = findViewById(R.id.tvDetailFactura);
        tvSupplier = findViewById(R.id.tvDetailSupplier);
        tvStock = findViewById(R.id.tvDetailStock);
        btnSellThisItem = findViewById(R.id.btnSellThisItem);

        // from intent, get product code
        if (getIntent().hasExtra("productCode")) {
            productCode = getIntent().getStringExtra("productCode");
        }

        // realm items
        loadProductDetails();

        // camera trigger
        imgProductPhoto.setOnClickListener(v -> checkPermissionAndLaunchCamera());
    }

    private void loadProductDetails() {
        FacturaItem item = realm.where(FacturaItem.class)
                .equalTo("code", productCode)
                .findFirst();

        tvProductName.setText(item.getName());

        tvProductPrice.setText(String.format(Locale.getDefault(), "₱%.2f", item.getPrice()));
        tvProductCode.setText("Product Code: " + item.getCode());
        tvFacturaNum.setText("Factura #: " + item.getFacturaNumber());
        tvSupplier.setText("Supplier: " + item.getSupplier());
        tvStock.setText("Stock Remaining: " + item.getStockQuantity() + " units");

        if (item.getImageUri() != null) {
            imgProductPhoto.setImageURI(Uri.parse(item.getImageUri()));
        }

        btnSellThisItem.setOnClickListener(v -> {
            Intent intent = new Intent(FacturaDetailActivity.this, LogSaleActivity.class);
            intent.putExtra("PREFILL_CODE", item.getCode());
            intent.putExtra("PREFILL_FACTURA", item.getFacturaNumber());
            intent.putExtra("PREFILL_PRICE", item.getPrice());
            startActivity(intent);
        });
    }

    private void checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchImageActivity();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchImageActivity() {
        Intent intent = new Intent(this, ImageActivity.class);
        imageLauncher.launch(intent);
    }

    private Uri saveImageToFile(byte[] jpegBytes) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "ITEM_" + (productCode != null ? productCode : "IMG") + "_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File file = new File(storageDir, imageFileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jpegBytes);
            fos.flush();
            fos.close();

            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save photo file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void updateProductImageInRealm(String imageUriString) {
        if (productCode == null) return;

        FacturaItem item = realm.where(FacturaItem.class)
                .equalTo("code", productCode)
                .findFirst();

        if (item != null) {
            realm.executeTransaction(r -> item.setImageUri(imageUriString));
            Toast.makeText(this, "Photo saved & updated successfully!", Toast.LENGTH_SHORT).show();
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