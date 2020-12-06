package com.example.mediaplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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
    int lvLastClickedItemID = 0, _pos = 0, _pausedLength = 0, currentPosition = 0;
    boolean isLocked = false, isRepeatOne = false, isRepeatAll = false;
    List<String> songs = new ArrayList<String>(), filePaths = new ArrayList<String>(), titleTop = new ArrayList<String>();

    void init() {
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
        mediaPlayer = new MediaPlayer();

        ivPrevius.setOnClickListener(this);
        ivFR.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivPause.setOnClickListener(this);
        ivFF.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivLock.setOnClickListener(this);
        ivUnlock.setOnClickListener(this);
        ivRepeatAllGray.setOnClickListener(this);
        ivRepeatAllWhite.setOnClickListener(this);
        ivRepeatOneGray.setOnClickListener(this);
        ivRepeatOneWhite.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivLock:
                ivLock.setVisibility(View.GONE);
                ivUnlock.setVisibility(View.VISIBLE);
                isLocked = true;
                break;

            case R.id.ivUnlock:
                ivLock.setVisibility(View.VISIBLE);
                ivUnlock.setVisibility(View.GONE);
                isLocked = false;
                break;

            case R.id.ivRepeatOneWhite:
                if (isLocked) break;
                ivRepeatOneWhite.setVisibility(View.GONE);
                ivRepeatOneGray.setVisibility(View.VISIBLE);
                isRepeatOne = false;
                break;

            case R.id.ivRepeatOneGray:
                if (isLocked) break;
                ivRepeatOneGray.setVisibility(View.GONE);
                ivRepeatOneWhite.setVisibility(View.VISIBLE);
                isRepeatOne = true;
                ivRepeatAllWhite.setVisibility(View.GONE);
                ivRepeatAllGray.setVisibility(View.VISIBLE);
                isRepeatAll = !isRepeatOne;
                break;

            case R.id.ivRepeatAllWhite:
                if (isLocked) break;
                ivRepeatAllWhite.setVisibility(View.GONE);
                ivRepeatAllGray.setVisibility(View.VISIBLE);
                isRepeatAll = false;
                break;

            case R.id.ivRepeatAllGray:
                if (isLocked) break;
                ivRepeatAllGray.setVisibility(View.GONE);
                ivRepeatAllWhite.setVisibility(View.VISIBLE);
                isRepeatAll = true;
                ivRepeatOneWhite.setVisibility(View.GONE);
                ivRepeatOneGray.setVisibility(View.VISIBLE);
                isRepeatOne = !isRepeatAll;
                break;

            case R.id.ivPrevius:
                if (isLocked) break;
                lvLastClickedItemID = _pos;
                _pos--;
                if (_pos == -1) _pos = lvMediaList.getCount() - 1;
                _pausedLength = 0;
                ivPlay.callOnClick();
                break;

            case R.id.ivNext:
                if (isLocked) break;
                lvLastClickedItemID = _pos;
                _pos++;
                if (_pos == lvMediaList.getCount()) _pos = 0;
                _pausedLength = 0;
                ivPlay.callOnClick();
                break;

            case R.id.ivPause:
                if (isLocked) break;
                ivPlay.setVisibility(View.VISIBLE);
                ivPause.setVisibility(View.GONE);
                mediaPlayer.pause();
                _pausedLength = mediaPlayer.getCurrentPosition();
                handler.removeCallbacks(runnable);
                break;

            case R.id.ivFF:
                if (isLocked) break;
                currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int newPosition = Math.min(duration - currentPosition, seekTime) + currentPosition;
                mediaPlayer.seekTo(newPosition);
                tvPosition.setText(setFormat(newPosition));
                break;

            case R.id.ivFR:
                if (isLocked) break;
                currentPosition = mediaPlayer.getCurrentPosition();
                newPosition = currentPosition - Math.min(currentPosition, seekTime);
                mediaPlayer.seekTo(newPosition);
                tvPosition.setText(setFormat(newPosition));
                break;

            case R.id.ivPlay:
                if (isLocked) break;
                if (lvMediaList.getCount() == 0) break;
                lvMediaList.getChildAt(lvLastClickedItemID).setBackgroundColor(getColor(R.color.listBackground));
                lvMediaList.getChildAt(_pos).setBackgroundColor(getColor(R.color.selectedItemBackground));
                String currentSongTitle = getString(R.string.currentSong);
                tvCurrentSong.setText(currentSongTitle + " " + titleTop.get(_pos));
                tvCurrentSong.setSelected(true);
                String uri = filePaths.get(_pos);
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
                duration = mediaPlayer.getDuration();
                tvDuration.setText(setFormat(duration));
                ivPlay.setVisibility(View.GONE);
                ivPause.setVisibility(View.VISIBLE);
                mediaPlayer.seekTo(_pausedLength);
                mediaPlayer.start();
                _pausedLength = 0;
                sbSeekBar.setMax(mediaPlayer.getDuration());
                handler.postDelayed(runnable, 0);
                lvLastClickedItemID = _pos;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SaveSettings();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        GetSettings();

        // Do events 2 times per second while music is playing...
        runnable = new Runnable() {
            @Override
            public void run() {
                sbSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this::run, 500);
                tvPosition.setText(setFormat(mediaPlayer.getCurrentPosition()));
            }
        };

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
        String uri = filePaths.get(_pos);
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
        ArrayAdapter<String> adapter = null;
        getFiles(Environment.getStorageDirectory().getPath(), 10, "mp3");
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_gallery_item,
                songs);
        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            getFiles(Environment.getExternalStorageDirectory().getPath(), 10, "mp3");
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_gallery_item,
                    songs);
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
        return adapter;

    }

    boolean SaveSettings() {
        SharedPreferences settings = getSharedPreferences("Settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("RepeatOne", ivRepeatOneWhite.getVisibility() == View.VISIBLE);
        editor.putBoolean("RepeatAll", ivRepeatAllWhite.getVisibility() == View.VISIBLE);
        editor.putBoolean("Locked", ivLock.getVisibility() == View.VISIBLE);
        editor.commit();
        return true;
    }

    boolean GetSettings() {
        SharedPreferences settings = getSharedPreferences("Settings", 0);
        if (settings.getBoolean("RepeatOne", false)) {
            ivRepeatOneGray.callOnClick();
        } else {
            ivRepeatOneWhite.callOnClick();
        }
        if (settings.getBoolean("RepeatAll", false)) {
            ivRepeatAllGray.callOnClick();
        } else {
            ivRepeatAllWhite.callOnClick();
        }
        if (settings.getBoolean("Locked", false)) {
            ivUnlock.callOnClick();
        } else {
            ivLock.callOnClick();
        }
        return true;
    }
}
