package com.gaadi.neon.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gaadi.neon.activity.gallery.GridFilesActivity;
import com.gaadi.neon.activity.gallery.HorizontalFilesActivity;
import com.gaadi.neon.enumerations.GalleryType;
import com.gaadi.neon.model.BucketModel;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.NeonImagesHandler;
import com.scanlibrary.R;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Lakshay on 27-02-2015.
 */
public class ImagesFoldersAdapter extends BaseAdapter {

    private Activity context;
    private ArrayList<BucketModel> folders;

    public ImagesFoldersAdapter(Activity context, ArrayList<BucketModel> bucketModels) {

        this.context = context;
        this.folders = bucketModels;
    }

    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public Object getItem(int position) {
        return folders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FolderHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.gallery_folder_layout, null);

            holder = new FolderHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.ivFolderThumbnail);
            holder.countFiles = (TextView) convertView.findViewById(R.id.tvCount);
            holder.FolderName = (TextView) convertView.findViewById(R.id.tvFolderName);
            convertView.setTag(holder);
        }

        holder = (FolderHolder) convertView.getTag();
        final BucketModel bucketInfo = folders.get(position);
        if (bucketInfo.getFileCount() > 0) {
            holder.countFiles.setText(String.valueOf(bucketInfo.getFileCount()));
            holder.countFiles.setVisibility(View.VISIBLE);
        } else {
            holder.countFiles.setVisibility(View.INVISIBLE);
        }
        holder.FolderName.setText(folders.get(position).getBucketName());
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.default_placeholder);
        Glide.with(context).load("file://" + folders.get(position).getBucketCoverImagePath())
                .apply(options)
                .into(holder.imageView);

        /*Glide.with(context)
                .load("file://" + folders.get(position).getBucketCoverImagePath())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.default_placeholder)
                .centerCrop()
                .into(holder.imageView);*/
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filesIntent;
                GalleryType type = GalleryType.Grid_Structure;
                if(null != NeonImagesHandler.getSingletonInstance().getGalleryParam()){
                    type = NeonImagesHandler.getSingletonInstance().getGalleryParam().getGalleryViewType();
                }
                switch (type){
                    case Grid_Structure:
                        filesIntent = new Intent(context, GridFilesActivity.class);
                        break;

                    case Horizontal_Structure:
                        filesIntent = new Intent(context, HorizontalFilesActivity.class);
                        break;

                    default:
                        filesIntent = new Intent(context, GridFilesActivity.class);
                }
                filesIntent.putExtra(Constants.BucketName,bucketInfo.getBucketName());
                filesIntent.putExtra(Constants.BucketId,bucketInfo.getBucketId());
                context.startActivityForResult(filesIntent,Constants.destroyPreviousActivity);
            }
        });
        return convertView;
    }


}
