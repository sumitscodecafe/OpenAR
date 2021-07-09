package com.example.openar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
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
        String phoneNo = getIntent().getStringExtra("phNo");

        Glide.with(FullImageView.this).load(imgUrl).into(imageView);

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(FullImageView.this)
                        .setTitle("Delete Photo")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Remove image from firebase DB
                                DatabaseReference node = FirebaseDatabase.getInstance().getReference();
                                Query getImgUrl = node.child("image").child(phoneNo).orderByChild("imgUrl").equalTo(imgUrl);
                                getImgUrl.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot idSnapshot: dataSnapshot.getChildren()) {
                                            if(idSnapshot.hasChildren()) {
                                                idSnapshot.getRef().removeValue();
                                                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(FullImageView.this, MainActivity.class));
                                                finish();
                                            }
                                        }
                                        if (!dataSnapshot.hasChildren())
                                            Toast.makeText(getApplicationContext(), "Error, not found.", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) { }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
                    }
                });
                alertDialog.show();
            }
        });

    }
}