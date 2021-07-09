package com.example.openar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FullImageView extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);

        imageView = findViewById(R.id.full_imageView);
        String imgUrl = getIntent().getStringExtra("imgUrl");
        Glide.with(FullImageView.this).load(imgUrl).into(imageView);
    }
}