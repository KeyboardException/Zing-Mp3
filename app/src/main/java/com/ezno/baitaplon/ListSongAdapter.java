package com.ezno.baitaplon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ezno.baitaplon.R;
import com.ezno.baitaplon.SongModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.Result;

public class ListSongAdapter extends RecyclerView.Adapter<ListSongAdapter.ViewHolder> implements Filterable {
    private Context context;
    private ArrayList<SongModel> songs;
    private ArrayList<SongModel> songsFiltered;
    private ListSongsActivity activity;

    public ListSongAdapter(ListSongsActivity activity, ArrayList<SongModel> songs) {
        this.context = activity.getBaseContext();
        this.activity = activity;
        this.songs = songs;
        this.songsFiltered = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.song_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongModel song = songsFiltered.get(position);

        int realPos = songs.indexOf(song);
        holder.lblName.setText(song.title);
        holder.attachEvents(activity, realPos);
    }

    @Override
    public int getItemCount() {
        return songsFiltered.size();
    }

    public void sort() {
        Collections.sort(songsFiltered, new Comparator<SongModel>() {
            @Override
            public int compare(SongModel sm1, SongModel sm2) {
                return (sm1.title.toLowerCase().compareTo(sm2.title.toLowerCase()));
                // Muốn đảo danh sách đối thành
                // return (sm2.title.toLowerCase().compareTo(sm1.title.toLowerCase()));
            }
        });

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    filterResults.count = songs.size();
                    filterResults.values = songs;

                } else {
                    List<SongModel> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase();

                    for (SongModel itemsModel : songs) {
                        if (itemsModel.title.toLowerCase().contains(searchStr))
                            resultsModel.add(itemsModel);

                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                songsFiltered = (ArrayList<SongModel>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected View view;
        protected TextView lblName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
            lblName = (TextView) itemView.findViewById(R.id.lbl_song_name);
        }

        public void attachEvents(ListSongsActivity activity, int id) {
            view.setOnClickListener(view -> {
                // create intent
                Intent intent = new Intent();
                intent.putExtra("id", id);

                // set result
                activity.setResult(Activity.RESULT_OK, intent);
                activity.finish();
            });
        }
    }
}

