package com.example.btl_dnc.adapter;

import android.content.Context;
import android.view.*;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.btl_dnc.R;
import com.example.btl_dnc.model.ChatMessage;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    Context context;
    ArrayList<ChatMessage> list;

    public ChatAdapter(Context context, ArrayList<ChatMessage> list) {
        this.context = context;
        this.list = list;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;

        public VH(View v) {
            super(v);
            tv = v.findViewById(R.id.tvMsg);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isUser ? 1 : 0;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 1) {
            return new VH(LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_user, parent, false));
        } else {
            return new VH(LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_bot, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(VH h, int i) {
        h.tv.setText(list.get(i).message);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}