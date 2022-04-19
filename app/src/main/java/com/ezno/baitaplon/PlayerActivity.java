package com.ezno.baitaplon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
//import android.widget.Toolbar;

import android.os.Bundle;
import android.os.Handler;

import com.ezno.baitaplon.R;
import com.ezno.baitaplon.SongsManager;
import com.ezno.baitaplon.SongModel;
import com.ezno.baitaplon.Util;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.io.IOException;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnCompletionListener {
    public static final int SELECT_SONG_REQUEST = 0;
    private Toolbar toolBar;

    private final int PERMISSION_REQUEST_STORAGE = 0;

    public static ArrayList<SongModel> arrSongs = new ArrayList<>();
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isLoop = false;
    private boolean isRandom = false;
    private int randomNumber = 0;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    private ImageView btnPlay;
    private ImageView btnForward;
    private ImageView btnBackward;
    private ImageView btnNext;
    private ImageView btnPrevious;
    private ImageView btnLoop;
    private ImageView btnRandom;
    private SeekBar songProgressBar;
    private TextView lblCurrentDuration;
    private TextView lblTotalDuration;
    private ImageView btnDisk;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        view = findViewById(R.id.player_view);

        // get action bar
        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        toolBar.setTitle("Mp3 Player");
        toolBar.setTitleTextColor(Color.BLACK);
        toolBar.setNavigationIcon(R.drawable.icon_app);
        setSupportActionBar(toolBar);

        // find views
        btnPlay = (ImageView) findViewById(R.id.btn_play);
        btnNext = (ImageView) findViewById(R.id.btn_next_song);
        btnPrevious = (ImageView) findViewById(R.id.btn_back_song);
        btnBackward = (ImageView) findViewById(R.id.btn_rewind);
        btnForward = (ImageView) findViewById(R.id.btn_forward);
        btnDisk = (ImageView) findViewById(R.id.imageView);
        btnLoop = (ImageView) findViewById(R.id.btn_loop);
        btnRandom = (ImageView) findViewById(R.id.btn_random);
        songProgressBar = (SeekBar) findViewById(R.id.prg_play);
        lblCurrentDuration = (TextView) findViewById(R.id.lbl_current_time);
        lblTotalDuration = (TextView) findViewById(R.id.lbl_total_time);

        btnPlay.setOnClickListener(this);
        btnForward.setOnClickListener(this);
        btnBackward.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);
        btnDisk.setOnClickListener(this);
        btnLoop.setOnClickListener(this);
        btnRandom.setOnClickListener(this);
        // listener
        songProgressBar.setOnSeekBarChangeListener(this);

        requestStoragePermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for read file permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start();
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
                    ActivityCompat.requestPermissions(PlayerActivity.this,
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.header_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.list_songs:
                Intent intent = new Intent(this, ListSongsActivity.class);
                startActivityForResult(intent, SELECT_SONG_REQUEST);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_SONG_REQUEST && resultCode == RESULT_OK) {
            currentSongIndex = data.getExtras().getInt("id");
            playSong(currentSongIndex);
        }
    }

    public void start() {
        // get all song from sd card
        SongsManager songsManager = new SongsManager();
        arrSongs = songsManager.getPlayList();

        // Mediaplayer
        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);

        if (arrSongs.size() > 0) {
            playSong(currentSongIndex);
        }
    }

    /**
     * Function to play a song
     *
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
        // Play song
        try {
            mp.reset();
            mp.setDataSource(arrSongs.get(songIndex).path);
            mp.prepare();
            play();

            // Update title of toolbar
            toolBar.setTitle(arrSongs.get(songIndex).title);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();

            // notification
            buildNotification();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        // play or pause a song
        if (id == R.id.btn_play) {
            /**
             * Play button click event
             * plays a song and changes button to pause image
             * pauses a song and changes button to play image
             * */
            if (mp.isPlaying()) {
                if (mp != null) {
                    mp.pause();
//                    stopAnimation();
                    // Changing button image to play button
                    btnPlay.setImageResource(R.drawable.play);
                    view.setBackgroundColor(0xffffffff);
                }
            } else {
                // Resume song
                if (mp != null) {
                    play();
                }
            }
        }
        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        if (id == R.id.btn_forward) {
            // get current song position
            int currentPosition = mp.getCurrentPosition();
            // check if seekForward time is lesser than song duration
            if (currentPosition + seekForwardTime <= mp.getDuration()) {
                // forward song
                mp.seekTo(currentPosition + seekForwardTime);
            } else {
                // forward to end position
                mp.seekTo(mp.getDuration());
            }
        }
        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        if (id == R.id.btn_rewind) {
            // get current song position
            int currentPosition = mp.getCurrentPosition();
            // check if seekBackward time is greater than 0 sec
            if (currentPosition - seekBackwardTime >= 0) {
                // forward song
                mp.seekTo(currentPosition - seekBackwardTime);
            } else {
                // backward to starting position
                mp.seekTo(0);
            }
        }
        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        if (id == R.id.btn_next_song) {
            // check if next song is there or not
            if (currentSongIndex < (arrSongs.size() - 1)) {
                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
            buildNotification();
        }
        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        if (id == R.id.btn_back_song) {
            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1);
                currentSongIndex = currentSongIndex - 1;
            } else {
                // play last song
                playSong(arrSongs.size() - 1);
                currentSongIndex = arrSongs.size() - 1;
            }
            buildNotification();
        }
        /**
         * Loop button click event
         * */
        if(id == R.id.btn_loop){
            isLoop=!isLoop;
            if(isLoop){
                btnLoop.setBackgroundColor(0xffa39e91);
            } else{
                btnLoop.setBackgroundColor(0xffffffff);
            }
        }

        if(id == R.id.btn_random){
            isRandom=!isRandom;
            if(isRandom){
                btnRandom.setBackgroundColor(0xffa39e91);
            } else{
                btnRandom.setBackgroundColor(0xffffffff);
            }
        }
    }

    public void play() {
        mp.start();
        // Changing button image to pause button
        btnPlay.setImageResource(R.drawable.pause);
//        startAnimation();
//        view.setBackgroundColor(0xffa39e91);
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            try {
                long totalDuration = mp.getDuration();
                long currentDuration = mp.getCurrentPosition();

                // Displaying Total Duration time
                lblTotalDuration.setText("" + Util.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                lblCurrentDuration.setText("" + Util.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int) (Util.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                songProgressBar.setProgress(progress);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            } catch (Exception ex) {

            }
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = Util.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(isLoop){
            playSong(currentSongIndex);
        }else {
            if(isRandom){
                while(randomNumber == currentSongIndex || randomNumber >= arrSongs.size()){
                    randomNumber = (int)(Math.random()* arrSongs.size());
                }
                currentSongIndex = randomNumber;
                playSong(currentSongIndex);
            } else {
                if (currentSongIndex < (arrSongs.size() - 1)) {
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }
            }
        }
        buildNotification();
    }

    private void buildNotification() {
        Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.song)
                .setContentTitle("Media Artist")
                .setContentText(arrSongs.get(currentSongIndex).title)
                .setDeleteIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.release();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }


    private void startAnimation(){
        Runnable runnable = new Runnable () {
            @Override
            public void run() {
                btnDisk.animate().rotationBy(360).withEndAction(this).setDuration(10000)
                        .setInterpolator(new LinearInterpolator()).start();
            }
        };
                btnDisk.animate().rotationBy(360).withEndAction(runnable).setDuration(10000)
                        .setInterpolator(new LinearInterpolator()).start();
            }

    private void stopAnimation() {
        btnDisk.animate().cancel();
    }
}
