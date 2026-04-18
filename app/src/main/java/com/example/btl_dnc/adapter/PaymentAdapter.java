package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.PaymentDetailActivity;
import com.example.btl_dnc.R;
import com.example.btl_dnc.model.PaymentNotification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PaymentNotification> list;

    public PaymentAdapter(Context context, ArrayList<PaymentNotification> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        PaymentNotification p = list.get(i);

        h.tvTitle.setText(p.title != null ? p.title : "Thông báo thu phí");
        h.tvContent.setText(p.content != null ? p.content : "");

        // SỬA Ở ĐÂY: Sử dụng endDate
        if (p.endDate != null) {
            String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(p.endDate.toDate());
            h.tvDeadline.setText("Hạn đóng: " + dateStr);
        } else {
            h.tvDeadline.setText("Chưa có hạn đóng");
        }

        // Xử lý ảnh Thumbnail (Base64 -> Bitmap)
        if (p.thumbnailBase64 != null && !p.thumbnailBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(p.thumbnailBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                h.imgThumb.setImageBitmap(decodedByte);
            } catch (Exception e) {
                h.imgThumb.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            h.imgThumb.setImageResource(R.drawable.placeholder_image);
        }

        // Click mở Detail
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PaymentDetailActivity.class);
            intent.putExtra("paymentID", p.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDeadline;
        ImageView imgThumb;

        public ViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            tvDeadline = v.findViewById(R.id.tvDeadline);
            imgThumb = v.findViewById(R.id.imgThumb);
        }
    }
}