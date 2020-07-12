package com.example.BooksReview;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class AddPostActivity extends AppCompatActivity {

    EditText mJudulEt, mDeskEt;
    ImageView mGambarEt;
    Button mUploadBtn;

    String mStoragePath = "All_Image_Uploads/";
    MediaController mc;

    String mDatabasePath = "Data";

    Uri mFilePathUri;

    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    ProgressDialog mProgressDialog;

    int IMAGE_REQUEST_CODE = 5;

    String cTitle, cDescr, cImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add new Post");

        mJudulEt = findViewById(R.id.pJudul);
        mDeskEt = findViewById(R.id.pDeskripsi);
        mGambarEt = findViewById(R.id.pGambar);
        mUploadBtn = findViewById(R.id.pUpload);

        Bundle intent = getIntent().getExtras();
        if (intent != null){
            cTitle = intent.getString("cTitle");
            cDescr = intent.getString("cDesc");
            cImage = intent.getString("cImage");

            mJudulEt.setText(cTitle);
            mDeskEt.setText(cDescr);
            Picasso.get().load(cImage).into(mGambarEt);

            actionBar.setTitle("Update Post");
            mUploadBtn.setText("Update");
        }

        mGambarEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
            }
        });

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mUploadBtn.getText().equals("Upload")){
                    uploadDataToFirebase();

                }
                else {

                    beginUpdate();
                }

            }
        });

        mStorageReference = getInstance().getReference();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference(mDatabasePath);

        mProgressDialog = new ProgressDialog(AddPostActivity.this);
    }

    private void beginUpdate() {

        mProgressDialog.setMessage("Mengupdate...");
        mProgressDialog.show();
        deletePreviousImage();
    }

    private void deletePreviousImage() {
        StorageReference mPictureRef = getInstance().getReferenceFromUrl(cImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {


                Toast.makeText(AddPostActivity.this, "Post berhasil diupdate", Toast.LENGTH_SHORT).show();
                uploadNewImage();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });
    }

    private void uploadNewImage() {
        String imageName = System .currentTimeMillis() + ".png";
        StorageReference storageReference2 = mStorageReference.child(mStoragePath + imageName);
        Bitmap bitmap =((BitmapDrawable)mGambarEt.getDrawable()).getBitmap();
        ByteArrayOutputStream baos =  new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageReference2.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(AddPostActivity.this, "Gambar baru berhasil diupload...", Toast.LENGTH_SHORT).show();

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();

                updateDatabase(downloadUri.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
            }
        });

    }

    private void updateDatabase(final String toString) {
        final String judul = mJudulEt.getText().toString();
        final String desk = mDeskEt.getText().toString();
        FirebaseDatabase mFireDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mFireDatabase.getReference("Data");

        Query query = mRef.orderByChild("judul").equalTo(cTitle);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().child("judul").setValue(judul);
                    ds.getRef().child("deskripsi").setValue(desk);
                    ds.getRef().child("gambar").setValue(toString);

                }
                mProgressDialog.dismiss();
                Toast.makeText(AddPostActivity.this, "Post berhasil diupdate", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddPostActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadDataToFirebase() {

        if (mFilePathUri != null){



            mProgressDialog.setTitle("Gambar sedang diupload...");

            mProgressDialog.show();

            StorageReference storageReference2nd = mStorageReference.child(mStoragePath + System.currentTimeMillis() + "." +  getFileExtension(mFilePathUri));

            storageReference2nd.putFile(mFilePathUri)

                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();
                            //get title
                            String mPostTitle = mJudulEt.getText().toString().trim();
                            //get description
                            String mPostDescr = mDeskEt.getText().toString().trim();

                            //hid progress dialog
                            mProgressDialog.dismiss();
                            //show toast that image is uploaded
                            Toast.makeText(AddPostActivity.this, "Berhasil Diupload", Toast.LENGTH_SHORT).show();

                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mPostTitle, mPostDescr, downloadUrl.toString());
                            //getting image upload id
                            String imageUploadId = mDatabaseReference.push().getKey();
                            //adding image upload id's child element into databaseReference
                            mDatabaseReference.child(imageUploadId).setValue(imageUploadInfo);
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(AddPostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.setTitle("Gambar sedang diupload");
                        }
                    });

        }
        else {
            Toast.makeText(this, "Silahkan Pilih Gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null){
            mFilePathUri = data.getData();

            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePathUri);

                mGambarEt.setImageBitmap(bitmap);
            }
            catch (Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
