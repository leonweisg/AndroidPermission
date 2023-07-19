package com.example.pdfchooser;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.simplefileexplorer.SimpleFileExplorerActivity;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "PERMISSION_TAG";
    private static final int STORAGE_PERMISSION_CODE = 100;
    private static final int READ_REQUEST_CODE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button takePermission = findViewById(R.id.button);
        takePermission.setOnClickListener(v -> {
            requestPermission();
        });

    }

    public void requestPermission() {
        try {
            Log.d(TAG, "requestPermission: try");

            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            storageActivityResultLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "requestPermission: catch", e);
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            storageActivityResultLauncher.launch(intent);
        }
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    if(Environment.isExternalStorageManager()){
                        Log.d(TAG, "onActivityResult: Manage External Storage Permission is granted.");
                        performFileSearch();
                    } else {
                        Log.d(TAG, "onActivityResult: Manage External Storage Permission is denied.");
                        Toast.makeText(MainActivity.this, "Manage External Storage Permission is denied.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length >0){
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if(write && read) {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage permissions granted");
                    performFileSearch();
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage permission denied");
                    Toast.makeText(this, "External Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    public boolean checkPermission() {
        return Environment.isExternalStorageManager();
    }

    public void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("MainActivity", "Uri: " + uri.toString());

                copyFileToInternalStorage(uri, "myFile.pdf");
            }
        }
    }

    private void copyFileToInternalStorage(Uri uri, String fileName) {
        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            outputStream = openFileOutput(fileName, MODE_PRIVATE);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
