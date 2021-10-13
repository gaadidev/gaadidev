package com.gaadi.neon.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.scanlibrary.R;

import java.util.ArrayList;

/**
 * @author lakshaygirdhar
 * @since 22-08-2016
 * @version 1.0
 */

public class FlashModeRecyclerHorizontalAdapter extends RecyclerView.Adapter<FlashModeRecyclerHorizontalAdapter.ViewHolder> {

    private static final String TAG = "FlashModeRecycler";
    private Context mContext;
    private ArrayList<String> flashList;
    private OnItemClickListener mItemClickListener;

    public FlashModeRecyclerHorizontalAdapter(Context context, ArrayList<String> imageList) {
        if (imageList == null) {
            flashList = new ArrayList<>();
        }
        else{
            this.flashList = imageList;
        }
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.flash_layout_row, null);
        return new ViewHolder(view);
    }

//    public void updateItems(ArrayList<String> mImageModel) {
//        this.flashList.clear();
//        this.flashList.addAll(mImageModel);
//        notifyDataSetChanged();
//    }

    public ArrayList<String> getItems(){
        return  flashList;
    }

//    public void addItems(ArrayList<String> mImageModel) {
//        // flashList.remove(0);
//        this.flashList.addAll(mImageModel);
//        // this.flashList.add()
//        notifyDataSetChanged();
//    }

//    public void removeItem(int position){
//        flashList.remove(position);
//        notifyDataSetChanged();
//    }


    @Override
    public void onBindViewHolder(final FlashModeRecyclerHorizontalAdapter.ViewHolder holder, int position) {

        String name = flashList.get(position);
        Log.d(TAG, "onBindViewHolder: " + name);
        holder.text.setVisibility(View.GONE);
        if ("off".equals(name)) {
            holder.icon.setImageResource(R.drawable.flash_off);
        } else if ("on".equals(name)) {
            holder.icon.setImageResource(R.drawable.flash_on);
        } else if ("auto".equals(name)) {
            holder.icon.setImageResource(R.drawable.flash_auto);
        } else if ("red-eye".equals(name)) {
            holder.icon.setImageResource(R.drawable.flash_red_eye);
        } else if ("torch".equals(name)) {
            holder.icon.setImageResource(R.drawable.flash_torch);
        } else {
            holder.text.setText(name);
            holder.text.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return flashList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView text;
        ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.colorValue);
            icon = (ImageView) itemView.findViewById(R.id.flash_icon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }

    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
