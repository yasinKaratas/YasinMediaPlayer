package com.example.mediaplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {
    final int seekTime = 5000;
    final File dir = new File(Environment.getStorageDirectory(), "/");
    ListView lvMediaList;
    TextView tvPosition, tvDuration, tvCurrentSong;
    SeekBar sbSeekBar;
    ImageView ivPrevius, ivFR, ivPlay, ivPause, ivFF, ivNext, ivLock, ivUnlock,
            ivRepeatAllWhite, ivRepeatAllGray, ivRepeatOneWhite, ivRepeatOneGray;
    MediaPlayer mediaPlayer;
    Handler handler = new Handler();
    Runnable runnable;
    int lvLastClickedItemID = 0;
    int _pos = 0;
    int _pausedLength = 0;
    boolean isLocked = false;
    boolean isRepeatOne = false;
    boolean isRepeatAll = false;
    List<String> songs = new ArrayList<String>();
    List<String> filePaths = new ArrayList<String>();
    List<String> titleTop = new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assigning the variables
        lvMediaList = findViewById(R.id.lvMediaList);
        lvMediaList.setAdapter(GetAllMedia(new File(dir, "/")));
        tvCurrentSong = findViewById(R.id.tvCurrentSong);
        tvPosition = findViewById(R.id.tvPosition);
        tvDuration = findViewById(R.id.tvDuration);
        sbSeekBar = findViewById(R.id.sbSeekBar);
        ivPrevius = findViewById(R.id.ivPrevius);
        ivFR = findViewById(R.id.ivFR);
        ivPlay = findViewById(R.id.ivPlay);
        ivPause = findViewById(R.id.ivPause);
        ivFF = findViewById(R.id.ivFF);
        ivNext = findViewById(R.id.ivNext);
        ivLock = findViewById(R.id.ivLock);
        ivUnlock = findViewById(R.id.ivUnlock);
        ivRepeatAllGray = findViewById(R.id.ivRepeatAllGray);
        ivRepeatAllWhite = findViewById(R.id.ivRepeatAllWhite);
        ivRepeatOneGray = findViewById(R.id.ivRepeatOneGray);
        ivRepeatOneWhite = findViewById(R.id.ivRepeatOneWhite);

        // Creating the media player
        mediaPlayer = MediaPlayer.create(this, R.raw.fani_kapa_gozunu);
        // Do events 2 times per second while music is playing...
        runnable = new Runnable() {
            @Override
            public void run() {
                sbSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this::run, 500);
                tvPosition.setText(setFormat(mediaPlayer.getCurrentPosition()));
            }
        };

        ivLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivLock.setVisibility(View.GONE);
                ivUnlock.setVisibility(View.VISIBLE);
                isLocked = true;
            }
        });

        ivUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivLock.setVisibility(View.VISIBLE);
                ivUnlock.setVisibility(View.GONE);
                isLocked = false;
            }
        });

        ivRepeatOneWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                ivRepeatOneWhite.setVisibility(View.GONE);
                ivRepeatOneGray.setVisibility(View.VISIBLE);
                isRepeatOne = false;
            }
        });

        ivRepeatOneGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                ivRepeatOneGray.setVisibility(View.GONE);
                ivRepeatOneWhite.setVisibility(View.VISIBLE);
                isRepeatOne = true;

                ivRepeatAllWhite.setVisibility(View.GONE);
                ivRepeatAllGray.setVisibility(View.VISIBLE);
                isRepeatAll = !isRepeatOne;
            }
        });

        ivRepeatAllWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                ivRepeatAllWhite.setVisibility(View.GONE);
                ivRepeatAllGray.setVisibility(View.VISIBLE);
                isRepeatAll = false;
            }
        });

        ivRepeatAllGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                ivRepeatAllGray.setVisibility(View.GONE);
                ivRepeatAllWhite.setVisibility(View.VISIBLE);
                isRepeatAll = true;

                ivRepeatOneWhite.setVisibility(View.GONE);
                ivRepeatOneGray.setVisibility(View.VISIBLE);
                isRepeatOne = !isRepeatAll;
            }
        });


        ivPrevius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                lvLastClickedItemID = _pos;
                _pos--;
                if (_pos == -1) _pos = lvMediaList.getCount() - 1;
                _pausedLength = 0;
                ivPlay.callOnClick();
            }
        });

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                lvLastClickedItemID = _pos;
                _pos++;
                if (_pos == lvMediaList.getCount()) _pos = 0;
                _pausedLength = 0;
                ivPlay.callOnClick();
            }
        });

        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                ivPlay.setVisibility(View.VISIBLE);
                ivPause.setVisibility(View.GONE);
                mediaPlayer.pause();
                _pausedLength = mediaPlayer.getCurrentPosition();
                handler.removeCallbacks(runnable);
            }
        });

        ivFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int newPosition = Math.min(duration - currentPosition, seekTime) + currentPosition;
                mediaPlayer.seekTo(newPosition);
                tvPosition.setText(setFormat(newPosition));
            }
        });

        ivFR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = currentPosition - Math.min(currentPosition, seekTime);
                mediaPlayer.seekTo(newPosition);
                tvPosition.setText(setFormat(newPosition));
            }
        });

        sbSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isLocked) return;
                if (!fromUser) return;
                mediaPlayer.seekTo(progress);
                tvPosition.setText(setFormat(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                OnCompletion();
            }
        });

        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocked) return;
                lvMediaList.getChildAt(lvLastClickedItemID).setBackgroundColor(getColor(R.color.listBackground));
                lvMediaList.getChildAt(_pos).setBackgroundColor(getColor(R.color.selectedItemBackground));

                String currentSongTitle = getString(R.string.currentSong);
                tvCurrentSong.setText(currentSongTitle + " " + titleTop.get(_pos));
                tvCurrentSong.setSelected(true);
                String uri = filePaths.get(_pos).toString();

                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    ivPause.callOnClick();
                    _pausedLength = 0;
                }

                mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(uri));
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        OnCompletion();
                    }
                });

                int duration = mediaPlayer.getDuration();
                tvDuration.setText(setFormat(duration));
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
                mediaPlayer.seekTo(_pausedLength);
                mediaPlayer.start();
                _pausedLength = 0;
                sbSeekBar.setMax(mediaPlayer.getDuration());
                handler.postDelayed(runnable, 0);
                lvLastClickedItemID = _pos;
            }
        });

        lvMediaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isLocked) return;
                ((TextView) view).setBackgroundColor(getColor(R.color.selectedItemBackground));
                _pos = position;
                _pausedLength = 0;
                ivPlay.callOnClick();

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        OnCompletion();
                    }
                });
            }
        });

    }

    private void OnCompletion() {
        ivPause.setVisibility(View.GONE);
        ivPlay.setVisibility(View.VISIBLE);
        mediaPlayer.seekTo(0);

        if (!isRepeatOne) {
            if (_pos < lvMediaList.getCount() - 1) {
                if (!isRepeatOne) _pos++;
            } else {
                if (isRepeatAll) {
                    _pos = 0;
                } else {
                    ivPause.callOnClick();
                    _pausedLength = 0;
                    return;
                }
            }
        }
        lvMediaList.setSelection(_pos);
        String uri = filePaths.get(_pos).toString();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(uri));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                OnCompletion();
            }
        });
        boolean _locked = isLocked;
        isLocked = false;
        ivPlay.callOnClick();
        isLocked = _locked;
    }

    @SuppressLint("DefaultLocate")
    private String setFormat(int duration) {
        int minute = (int) TimeUnit.MILLISECONDS.toMinutes(duration);
        int second = (int) (TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(minute));

        return String.format("%02d:%02d", minute, second);
    }

    public void getFiles(String dirPath, int level, String extention) {
        MediaMetadataRetriever metaRetriver = new MediaMetadataRetriever();
        File dir = new File(dirPath);
        File[] firstLevelFiles = dir.listFiles();
        if (firstLevelFiles != null && firstLevelFiles.length > 0) {
            for (File aFile : firstLevelFiles) {
                if (aFile.isDirectory()) getFiles(aFile.getPath(), level + 1, extention);
                else {
                    if (aFile.getName().endsWith("." + extention)) {
                        metaRetriver.setDataSource(dirPath + "/" + aFile.getName());
                        String _title = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String _artist = metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String _duration = setFormat(Integer.parseInt(metaRetriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));

                        if (_title == null) _title = "Bilinmeyen parça";
                        if (_artist == null) _artist = "Bilinmeyen sanatçı";


                        songs.add(_title + " (" + _artist + ") " + _duration);
                        filePaths.add(aFile.getAbsolutePath());
                        titleTop.add(_title + " (" + _artist + ")");

                    }
                }
            }
        }
    }

    private ArrayAdapter<String> GetAllMedia(File dir) {
        songs.clear();
        filePaths.clear();
        titleTop.clear();

        getFiles(Environment.getStorageDirectory().getPath(), 10, "mp3");
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getFiles(Environment.getExternalStorageDirectory().getPath(), 10, "mp3");
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_gallery_item,
                    songs);
            return adapter;
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "Devam etmek için harici veri alanına ulaşma izin vermelisiniz.",
                    0,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
/*            while (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            */
        }
        //GetAllMedia(dir);
        return null;

    }
}

