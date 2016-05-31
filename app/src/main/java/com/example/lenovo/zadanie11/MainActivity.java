package com.example.lenovo.zadanie11;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.selectAudioButton)
    Button selectAudioButton;

    @Bind(R.id.textName)
    TextView title;

    @Bind(R.id.textTrackLenth)
    TextView duration;

    @Bind(R.id.textCurrentSec)
    TextView currentTime;

    @Bind(R.id.seekBar)
    SeekBar seekBar;

    MediaPlayer mediaPlayer;
    MyRun myRun = new MyRun();
    Handler handler = new Handler();
    int startTime = 0;
    int endTime = 0;

    public static final int REQ_CODE_PICK_SOUNDFILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        selectAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQ_CODE_PICK_SOUNDFILE);
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && mediaPlayer != null)
                    mediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK) {
            Uri audioFile = data.getData();
            try{
                readFile(audioFile);
            }
            catch (IOException e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void readFile(Uri uri) throws IOException {
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        getTitle(uri);
        loadFile(uri);
    }

    protected void loadFile(Uri uri) throws IOException {
        mediaPlayer = MediaPlayer.create(this, uri);
        endTime = mediaPlayer.getDuration();
        duration.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) endTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime)))
        );
        seekBar.setMax(endTime);
    }

    protected void getTitle(Uri uri){
        Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Audio.Media.TITLE},
                null, null, null);
        if(cursor.moveToFirst()){
            String titleFile = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            title.setText(titleFile);
        }
    }

    public void onBackClick(View view){
        if(mediaPlayer != null)
            seekBy(-10000);
    }

    public void onPlayClick(View view){
        if(mediaPlayer != null){
            mediaPlayer.start();
            handler.postDelayed(myRun, 50);
        }
    }

    public void onPouseClick(View view){
        if(mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void onNextClick(View view){
        if(mediaPlayer != null)
            seekBy(10000);
    }

    private void seekBy(int millisecond){
        startTime = mediaPlayer.getCurrentPosition();
        if(startTime + millisecond > endTime) {
            mediaPlayer.seekTo(endTime);
            seekBar.setProgress(endTime);
        }
        else
            mediaPlayer.seekTo(startTime + millisecond);
    }

    public class MyRun implements Runnable {

        @Override
        public void run() {
            startTime = mediaPlayer.getCurrentPosition();
            currentTime.setText(String.format("%d min, %d sec",
                            TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                            TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                            toMinutes((long) startTime)))
            );
            seekBar.setProgress(startTime);
            handler.postDelayed(myRun, 50);
        }
    }
}
