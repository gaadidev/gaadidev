package com.gaadi.neon.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gaadi.neon.interfaces.UpdateSelection;
import com.gaadi.neon.util.ApplicationController;
import com.scanlibrary.R;

import java.util.HashSet;

/**
 * Created by Lakshay
 * @since 02-03-2015.
 *
 */
public class SelectFilesAdapter extends CursorAdapter implements View.OnClickListener {

    public HashSet<String> selectedArr = new HashSet<>();
    private Context context;
    private UpdateSelection updateSelection;
    private FilesHolder filesHolder;
    private boolean stopSelection;

    public SelectFilesAdapter(Context context, Cursor c, int flags, UpdateSelection updateSelection) {
        super(context, c, flags);
        this.updateSelection = updateSelection;
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.rlSelectFiles) {
            actionSelectUnSelect(v);
        }
    }

    private void actionSelectUnSelect(View v) {
        FilesHolder holder = (FilesHolder) v.getTag();
        String imagePath = (String) holder.transparentLayer.getTag();
        if (imagePath.substring(imagePath.lastIndexOf(".")+1).equalsIgnoreCase("bmp")) {
            Toast.makeText(context, context.getString(R.string.wrong_format), Toast.LENGTH_SHORT).show();
            return;
        }
        int position = (int) holder.selection_view.getTag();
        if (View.VISIBLE == holder.transparentLayer.getVisibility()) {
            holder.transparentLayer.setVisibility(View.INVISIBLE);
            holder.selection_view.setVisibility(View.INVISIBLE);
            selectedArr.remove(String.valueOf(position));
            this.updateSelection.updateSelected(imagePath, false);
        } else if (stopSelection) {
             Toast.makeText(context,context.getString(R.string.selected_max_photos),Toast.LENGTH_SHORT).show();
        } else {
            holder.transparentLayer.setVisibility(View.VISIBLE);
            holder.selection_view.setVisibility(View.VISIBLE);
            holder.selectedImage.setBackgroundColor(ContextCompat.getColor(context,R.color.transparent_white));
            this.updateSelection.updateSelected(imagePath, true);
            selectedArr.add(String.valueOf(position));
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View convertView = View.inflate(context, R.layout.select_files, null);
        filesHolder = new FilesHolder();
        filesHolder.selectedImage = (ImageView) convertView.findViewById(R.id.imageSelected);
        filesHolder.selection_view = (ImageView) convertView.findViewById(R.id.selection_view);
        filesHolder.transparentLayer = (ImageView) convertView.findViewById(R.id.vTransparentLayer);
        convertView.setTag(filesHolder);
        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        filesHolder = (FilesHolder) view.getTag();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        view.setOnClickListener(this);

        filesHolder.transparentLayer.setTag(path);
        filesHolder.selection_view.setTag(cursor.getPosition());

        Glide.with(context).load(path)
                .placeholder(R.drawable.default_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(filesHolder.selectedImage);

        if ((ApplicationController.selectedFiles != null && ApplicationController.selectedFiles.contains(path))) {
            filesHolder.transparentLayer.setVisibility(View.VISIBLE);
            filesHolder.selection_view.setVisibility(View.VISIBLE);
        } else {
            filesHolder.transparentLayer.setVisibility(View.INVISIBLE);
            filesHolder.selection_view.setVisibility(View.INVISIBLE);
        }

        if (selectedArr.contains(String.valueOf(cursor.getPosition()))) {
            filesHolder.transparentLayer.setVisibility(View.VISIBLE);
            filesHolder.selection_view.setVisibility(View.VISIBLE);
        }
    }

    public void setStopSelection(boolean stopSelection) {
        this.stopSelection = stopSelection;
    }

    public class FilesHolder {
        ImageView selectedImage;
        ImageView transparentLayer;
        ImageView selection_view;

    }
}
