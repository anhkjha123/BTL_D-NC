package com.example.btl_dnc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.btl_dnc.NewsDetailActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.News;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    ArrayList<News> list;

    public NewsAdapter(ArrayList<News> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate;
        ImageView img;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDate = view.findViewById(R.id.tvDate);
            img = view.findViewById(R.id.imgNews);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        News n = list.get(i);

        h.tvTitle.setText(n.title != null ? n.title : "");

        if (n.createAt != null) {
            String time = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(n.createAt.toDate());
            h.tvDate.setText(time);
        } else {
            h.tvDate.setText("");
        }

        Glide.with(h.itemView.getContext())
                .load(n.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(h.img);


        h.itemView.setOnClickListener(v -> {
            android.content.Intent intent =
                    new android.content.Intent(h.itemView.getContext(), NewsDetailActivity.class);
            intent.putExtra("newsID", n.id);
            h.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}