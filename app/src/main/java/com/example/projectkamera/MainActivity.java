package com.example.projectkamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button btnSelect, btnUpload, klikimage;

    // view for image view
    private ImageView imageView;
    private ImageView viewklik;

    public static final int RequestPermissionCode = 1 ;
    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    private  final int CAMERA_REQUEST_CODE = 1;
//    private final int PICK_IMAGE_MANTAP = 7;
    private  static final int PERMISSION_REQUEST_CODE = 7;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    Button b1, b2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#0F9D58"));
        actionBar.setBackgroundDrawable(colorDrawable);

        btnSelect =  findViewById(R.id.btnChoose);
        btnUpload =  findViewById(R.id.btnUpload);
        imageView =  findViewById(R.id.imgView);

        b1 = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Uri globalLink;
        EnableRuntimePermission();
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

//                String imageF = Environment.getExternalStorageDirectory().getPath();
//                File imageFolder = new File(imageF, "HasilFoto");
//                imageFolder.mkdir();
//                final File imageFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "HasilFoto");

//                if (!imageFolder.exists()) {
//                    boolean rv = imageFolder.mkdirs();
//                    if(rv){
//                        Toast.makeText(MainActivity.this,
//                                "Folder berhasil terbuat!!", Toast.LENGTH_SHORT).show();
//                    }
//                    else
//                    {
//                        Toast.makeText(MainActivity.this,
//                                "Gagal", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this,
//                            "Folder Gagal terbuat. Sudah ada Folder tersebut!!", Toast.LENGTH_SHORT).show();
//                }

                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    createDirectory("HasilFoto");
                }else{
                    askPermission();
                }

                final File imageFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "HasilFoto");
                imageFolder.mkdirs();
                Date d = new Date();

                CharSequence s = DateFormat.format("MM-dd-yy hh-mm-ss", d.getTime());
                File image = new File(imageFolder, "Foto" + s.toString() + ".jpg");
                Uri uriSavedImage = FileProvider.getUriForFile(
                        MainActivity.this,
                        "com.example.projectkamera.MainActivity.provider", image);
                it.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
                startActivityForResult(it,0);
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (it.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(it, CAMERA_REQUEST_CODE);
                }
                startActivityForResult(it,CAMERA_REQUEST_CODE);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SelectImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadImage();
            }
        });
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
    }

    private void createDirectory(String folderName) {

        File file = new File(Environment.getExternalStorageDirectory(),folderName);

        if (!file.exists()){
            file.mkdir();
            Toast.makeText(MainActivity.this,"Successful",Toast.LENGTH_SHORT).show();
        }else
        {

            Toast.makeText(MainActivity.this,"Folder Already Exists",Toast.LENGTH_SHORT).show();
        }
    }

    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {
        super.onActivityResult(requestCode,
                resultCode,
                data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK  ) {

            filePath = data.getData();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray(); // convert camera photo to byte array

            // save it in your external storage.
            File dir=  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "HasilFoto");
            if (!dir.exists()){
                dir.mkdir();
//                Toast.makeText(MainActivity.this,"Successful",Toast.LENGTH_SHORT).show();
            }else
            {
//                Toast.makeText(MainActivity.this,"Folder Already Exists",Toast.LENGTH_SHORT).show();
            }

            Date d = new Date();
            CharSequence s = DateFormat.format("MM-dd-yyhh-mm-ss", d.getTime());
            File output=new File(dir, "Foto" + s.toString() + ".jpg");

//            filePath = Uri.parse(data.getData());
            filePath = Uri.fromFile(output);
//            filePath = data.getData();
            FileOutputStream fo = null;

            try {
                fo = new FileOutputStream(output);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fo.write(byteArray);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fo.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(this,"Data Telah Terload ke ImageView" + output,Toast.LENGTH_LONG).show();
        }
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imageView.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(MainActivity.this,
                                                    "Gambar Terupload!!", Toast.LENGTH_SHORT).show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }

    public void EnableRuntimePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{ Manifest.permission.CAMERA}, RequestPermissionCode);
        }
    }
    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        super.onRequestPermissionsResult(RC, per, PResult);
        switch (RC) {
            case RequestPermissionCode:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}