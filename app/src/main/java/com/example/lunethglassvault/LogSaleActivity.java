package com.example.lunethglassvault;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.realm.Realm;

public class LogSaleActivity extends AppCompatActivity {

    private EditText etItemCode, etFacturaNum, etQuantity, etUnitPrice;
    private ImageView imgSalePhoto;
    private Button btnTakePhoto, btnSaveSale;

    private String capturedImageUriString = "";
    private Realm realm;

    // camera
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            imgSalePhoto.setImageBitmap(imageBitmap);
                            capturedImageUriString = saveBitmapToInternalStorage(imageBitmap);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_sale);

        realm = Realm.getDefaultInstance();

        etItemCode = findViewById(R.id.etItemCode);
        etFacturaNum = findViewById(R.id.etFacturaNum);
        etQuantity = findViewById(R.id.etQuantity);
        etUnitPrice = findViewById(R.id.etUnitPrice);
        imgSalePhoto = findViewById(R.id.imgSalePhoto);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSaveSale = findViewById(R.id.btnSaveSale);

        // if passed via intent, form gets prefilled
        String passedCode = getIntent().getStringExtra("PREFILL_CODE");
        String passedFactura = getIntent().getStringExtra("PREFILL_FACTURA");
        double passedPrice = getIntent().getDoubleExtra("PREFILL_PRICE", 0.0);

        if (passedCode != null) etItemCode.setText(passedCode);
        if (passedFactura != null) etFacturaNum.setText(passedFactura);
        if (passedPrice > 0) etUnitPrice.setText(String.valueOf(passedPrice));

        // take photo (receipt)
        btnTakePhoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        });

        btnSaveSale.setOnClickListener(v -> saveSaleTransaction());
    }

    private void saveSaleTransaction() {
        String itemCode = etItemCode.getText().toString().trim();
        String facturaNum = etFacturaNum.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String priceStr = etUnitPrice.getText().toString().trim();

        // for validation purposes
        if (itemCode.isEmpty() || facturaNum.isEmpty() || qtyStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(qtyStr);
        double unitPrice = Double.parseDouble(priceStr);
        double totalPrice = quantity * unitPrice;

        // Fetch userUuid from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        String savedUuid = sharedPref.getString("USER_UUID",
                sharedPref.getString("USER_NAME",
                        sharedPref.getString("SAVED_USERNAME", "default_user")));

        // log to realm
        realm.executeTransaction(r -> {
            // record the sale
            SaleLog sale = r.createObject(SaleLog.class, UUID.randomUUID().toString());
            sale.setUserUuid(savedUuid);
            sale.setItemCode(itemCode);
            sale.setFacturaNumber(facturaNum);
            sale.setQuantitySold(quantity);
            sale.setTotalPrice(totalPrice);
            sale.setTimestamp(System.currentTimeMillis());

            if (!capturedImageUriString.isEmpty()) {
                sale.setImageUri(capturedImageUriString);
            }

            // check item in catalog
            FacturaItem stockItem = r.where(FacturaItem.class).equalTo("code", itemCode).findFirst();

            if (stockItem != null) {
                // update stock qty
                int newStock = Math.max(0, stockItem.getStockQuantity() - quantity);
                stockItem.setStockQuantity(newStock);
            } else {
                // if user enters factura that is new to the system, it makes an object automatically
                FacturaItem newItem = r.createObject(FacturaItem.class, itemCode);
                newItem.setFacturaNumber(facturaNum);
                newItem.setName("Item " + itemCode); //custom name or auto name from code
                newItem.setSupplier("Logged via Sale");
                newItem.setPrice(unitPrice);
                newItem.setStockQuantity(0); // stocks

                if (!capturedImageUriString.isEmpty()) {
                    newItem.setImageUri(capturedImageUriString);
                }
            }
        });

        Toast.makeText(this, "Sale recorded successfully!", Toast.LENGTH_SHORT).show();

        // back to catalog
        Intent intent = new Intent(LogSaleActivity.this, FacturaCatalog.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    // saves captured thumbnail locally and returns URI path string
    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        File directory = getFilesDir();
        File imageFile = new File(directory, "sale_" + System.currentTimeMillis() + ".png");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            return Uri.fromFile(imageFile).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
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