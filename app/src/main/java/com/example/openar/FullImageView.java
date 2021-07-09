package com.example.openar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

public class FullImageView extends AppCompatActivity {

    ImageView imageView;
    Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image_view);

        imageView = findViewById(R.id.full_imageView);
        btn_delete = findViewById(R.id.btn_delete);
        String imgUrl = getIntent().getStringExtra("imgUrl");

        Glide.with(FullImageView.this).load(imgUrl).into(imageView);

//        btn_delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Remove image from firebase DB
//                DatabaseReference node = FirebaseDatabase.getInstance().getReference();
//                Query getUserCourseId = node.child("image").child(username).orderByChild("name").equalTo(model.getName());
//                getUserCourseId.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
//                        for (DataSnapshot idSnapshot: dataSnapshot.getChildren()) {
//                            if(idSnapshot.hasChildren()) {
//                                idSnapshot.getRef().removeValue();
//                                Toast.makeText(view.getContext(), "Course un-enrolled", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        if (!dataSnapshot.hasChildren())
//                            Toast.makeText(view.getContext(), "Error, not found.", Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) { }
//                });
//            }
//        });

    }
}