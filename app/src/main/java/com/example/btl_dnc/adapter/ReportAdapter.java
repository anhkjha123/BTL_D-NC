package com.example.btl_dnc.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.R;
import com.example.btl_dnc.ReportDetailActivity;
import com.example.btl_dnc.model.Report;

import java.util.Calendar;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    Context context;
    List<Report> list;

    public ReportAdapter(Context context, List<Report> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgReport;
        TextView txtTitle, txtDate;

        public ViewHolder(View v) {
            super(v);
            imgReport = v.findViewById(R.id.imgReport);
            txtTitle = v.findViewById(R.id.txtTitle);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int i) {
        Report r = list.get(i);

        // ===== TITLE =====
        h.txtTitle.setText(r.content != null ? r.content : "");

        // ===== DATE =====
        if (r.createdAt > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(r.createdAt);

            String date = "Ngày " + c.get(Calendar.DAY_OF_MONTH)
                    + " Tháng " + (c.get(Calendar.MONTH) + 1)
                    + " Năm " + c.get(Calendar.YEAR);

            h.txtDate.setText(date);
        } else {
            h.txtDate.setText("");
        }

        // ===== RESET IMAGE =====
        h.imgReport.setImageDrawable(null);

        // ===== LOAD BASE64 IMAGE =====
        if (r.imageBase64 != null && !r.imageBase64.isEmpty()) {

            Bitmap bmp = decodeBase64(r.imageBase64);

            if (bmp != null) {
                h.imgReport.setImageBitmap(bmp);
            } else {
                h.imgReport.setImageResource(R.drawable.ic_image_placeholder);
            }

        } else {
            h.imgReport.setImageResource(R.drawable.ic_image_placeholder);
        }

        // ===== CLICK ITEM =====
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("id", r.id);
            context.startActivity(intent);
        });
    }

    // ===== DECODE BASE64 =====
    private Bitmap decodeBase64(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}