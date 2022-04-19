package com.ezno.baitaplon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.Comparator;

public class ListSongsActivity extends AppCompatActivity {
    private Toolbar toolBar;
    private EditText search;
    private RecyclerView lstSongs;
    private ListSongAdapter adapter;

    private View view;

    private final int PERMISSION_REQUEST_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_songs);

        view = findViewById(R.id.list_song_view);
        lstSongs = (RecyclerView) findViewById(R.id.lst_songs);
        search = findViewById(R.id.list_search);

        // get action bar
        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        toolBar.setTitle("List songs");
        toolBar.setTitleTextColor(Color.BLACK);
        toolBar.setNavigationIcon(R.drawable.back);
        setSupportActionBar(toolBar);

        requestStoragePermission();
    }

    public void initAdapter() {
        // create adapter
        adapter = new ListSongAdapter(this, PlayerActivity.arrSongs);
        lstSongs.setAdapter(adapter);
        lstSongs.setLayoutManager(new LinearLayoutManager(this));

        // adapter.sort();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initAdapter();
            } else {
                Snackbar.make(view, "Không có quyền đọc file",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            Snackbar.make(view, "Cần quyền đọc file để tiếp tục",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityCompat.requestPermissions(ListSongsActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}