package com.example.lunethglassvault;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_TITLE = "extra_image_title";
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final int RESULT_CODE_IMAGE_TAKEN = 101;
    public static final int REQUEST_CAMERA_CAPTURE = 200;

    private CropImageView cropImageView;
    private ImageButton captureBtn, cropBtn, rotateBtn;
    private Button cancelBtn;

    private Uri cameraPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        cropImageView = findViewById(R.id.cropImageView);
        captureBtn = findViewById(R.id.capture);
        cropBtn = findViewById(R.id.crop);
        rotateBtn = findViewById(R.id.rotate);
        cancelBtn = findViewById(R.id.cancel);

        captureBtn.setOnClickListener(v -> openCamera());
        rotateBtn.setOnClickListener(v -> cropImageView.rotateImage(90));
        cropBtn.setOnClickListener(v -> handleCroppingAndReturn());
        cancelBtn.setOnClickListener(v -> finish());

        // launch camera immediately upon opening this Activity
        openCamera();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            // Create temp file for full-resolution camera picture
            File photoFile = File.createTempFile(
                    "camera_temp_" + System.currentTimeMillis(),
                    ".jpg",
                    getExternalFilesDir("Pictures")
            );

            cameraPhotoUri = FileProvider.getUriForFile(
                    this,
                    getString(R.string.fileAuthority),
                    photoFile
            );

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
            startActivityForResult(takePictureIntent, REQUEST_CAMERA_CAPTURE);
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating photo file", Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No camera application found on this device", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA_CAPTURE) {
            if (resultCode == RESULT_OK && cameraPhotoUri != null) {
                // Set captured photo into CropImageView
                cropImageView.setImageUriAsync(cameraPhotoUri);
            } else if (resultCode == RESULT_CANCELED) {
                // If user backs out of camera without taking a photo and no image is loaded yet, close activity
                if (cropImageView.getCroppedImage() == null) {
                    finish();
                }
            }
        }
    }

    private void handleCroppingAndReturn() {
        Bitmap croppedBitmap = cropImageView.getCroppedImage();

        if (croppedBitmap == null) {
            Toast.makeText(this, "Please capture an image first", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("rawJpeg", byteArray);
        setResult(RESULT_CODE_IMAGE_TAKEN, resultIntent);
        finish();
    }
}