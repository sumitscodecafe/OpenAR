package com.example.openar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<Model> imglist;
    private Context context;
    private String phoneNo;

    public MyAdapter(Context context, ArrayList<Model> list, String phNo) {
        this.context = context;
        this.imglist = list;
        this.phoneNo = phNo;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        //Load images in recycler view
        Glide.with(context).load(imglist.get(position).getImgUrl()).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FullImageView.class);
                intent.putExtra("imgUrl", imglist.get(position).getImgUrl());
                intent.putExtra("phNo", phoneNo);
                context.startActivity(intent);
                //((Activity)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imglist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
