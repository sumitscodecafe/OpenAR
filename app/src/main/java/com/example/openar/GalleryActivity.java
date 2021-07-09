package com.example.openar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.icu.lang.UProperty;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ImageView emptyImg;
    private TextView emptyTxt;
    private ArrayList<Model> imglist;
    private MyAdapter myAdapter;
    private DatabaseReference root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        recyclerView = findViewById(R.id.recycler_view);
        emptyImg = findViewById(R.id.empty_img);
        emptyImg.setVisibility(View.INVISIBLE);
        emptyTxt = findViewById(R.id.empty_txt);
        emptyTxt.setVisibility(View.INVISIBLE);
        //recyclerView.setHasFixedSize(true);
        progressBar = findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        imglist = new ArrayList<>();

        String phone_no = SharedPreference.readSharedSetting(getApplicationContext(), "ph_no", "false");
        root = FirebaseDatabase.getInstance().getReference().child("image").child(phone_no);
        root.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if(snapshot.hasChildren()) {
                        emptyImg.setVisibility(View.INVISIBLE);
                        emptyTxt.setVisibility(View.INVISIBLE);
                        Model model = dataSnapshot.getValue(Model.class);
                        imglist.add(model);
                        myAdapter = new MyAdapter(GalleryActivity.this, imglist);
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(GalleryActivity.this,4,GridLayoutManager.VERTICAL,false);
                        recyclerView.setLayoutManager(gridLayoutManager);
                        recyclerView.setAdapter(myAdapter);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
                if (!snapshot.hasChildren()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    emptyImg.setVisibility(View.VISIBLE);
                    emptyTxt.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "No image found!", Toast.LENGTH_SHORT).show();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}