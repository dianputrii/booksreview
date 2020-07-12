package com.example.BooksReview;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class PostDetailActivity extends AppCompatActivity {

    TextView mTitleTv,mDetailTv;
    ImageView mImageTv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mTitleTv = findViewById(R.id.judulTv);
        mDetailTv = findViewById(R.id.deskripsiTv);
        mImageTv = findViewById(R.id.gambarTv);




        String image = getIntent().getStringExtra("gambar");
        String title = getIntent().getStringExtra("judul");
        String desc = getIntent().getStringExtra("deskripsi");


        mTitleTv.setText(title);
        mDetailTv.setText(desc);
        Picasso.get().load(image).into(mImageTv);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
