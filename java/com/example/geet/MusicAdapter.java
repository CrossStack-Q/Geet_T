package com.example.geet;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context mContext,ArrayList<MusicFiles> mFiles)
    {
        this.mFiles= mFiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        holder.artist_name.setText(mFiles.get(position).getArtist());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if (image != null)
        {
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        }
        else
        {
            Glide.with(mContext)
                    .load(R.drawable.dev)
                    .into(holder.album_art);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,PlayerActivity.class);
                intent.putExtra("position",position);
                mContext.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_delete_from_device:
                                Toast.makeText(mContext, "File Deleted", Toast.LENGTH_SHORT).show();
                                deletefile(position, view);
                                break;
                        }
                        return true;
                    }
                });

            }
        });

    }
    private void deletefile(int position,View view)
    {
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(position).getId()));

        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted) {
            mContext.getContentResolver().delete(contentUri,null,null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view, "File Deleted", Snackbar.LENGTH_LONG)
//                .setActionTextColor(mContext.getResources().getColor(android.R.color.holo_blue_dark))
                    .show();
        }
        else
        {
            // may be in sdCard
            Snackbar.make(view, "give Permission to Delete", Snackbar.LENGTH_LONG)
//                .setActionTextColor(mContext.getResources().getColor(android.R.color.holo_blue_dark))
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView file_name,artist_name,duration_total;
        ImageView album_art,menuMore;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            menuMore =itemView.findViewById(R.id.menuMore);
            file_name=itemView.findViewById(R.id.music_file_name);
            artist_name=itemView.findViewById(R.id.artist_name);
            album_art=itemView.findViewById(R.id.music_img);

//            duration_total=itemView.findViewById(R.id.duration_total1);

        }
    }
    private byte[] getAlbumArt(String uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }





}
