package com.ezno.baitaplon;

import android.os.Environment;

import com.ezno.baitaplon.SongModel;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Objects;

public class SongsManager {
    // SDCard Path
    private File MEDIA_PATH;
    private ArrayList<SongModel> songsList;

    public SongsManager() {
        MEDIA_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        songsList = new ArrayList<SongModel>();
    }

    /**
     * Function to read all mp3 files from sdcard
     * and store the details in ArrayList
     */
    public ArrayList<SongModel> getPlayList() {
        File[] files = MEDIA_PATH.listFiles(new FileExtensionFilter());

        if (files != null && files.length > 0) {
            for (File file : files) {
                SongModel model = new SongModel();
                model.title = file.getName().substring(0, (file.getName().length() - 4));
                model.path = file.getPath();

                // Adding each song to SongList
                songsList.add(model);
            }
        }
        // return songs list array
        return songsList;
    }

    /**
     * Class to filter files which are having .mp3 extension
     */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3") || name.endsWith(".MP3"));
        }
    }
}
