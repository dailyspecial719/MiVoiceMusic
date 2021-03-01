package com.generally2.myvoicemusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;

    private RelativeLayout parentRelativeLayout;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";

    private ImageView playPauseBtn,nextBtb, previousBtn;
    private TextView songNameTxt;

    private ImageView imageView;
    private RelativeLayout lowerRelative;
    private Button voiceEnabledBtn;

    private String mode = "ON";

    private MediaPlayer myMediaPlayer;
    private MediaPlayer nextPlayer;
    private int position;

    private ArrayList<File> mySongs;
    private String mSongName;
    private FragmentActivity mFragments;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();  //google ad stuff
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });


        checkVoiceCommandPermission();


        playPauseBtn = findViewById(R.id.play_pause_btn);
        nextBtb = findViewById(R.id.next_btn);
        previousBtn = findViewById(R.id.previous_btn);
        songNameTxt = findViewById(R.id.songName);
        imageView = findViewById(R.id.logo);
        lowerRelative = findViewById(R.id.lower);
        voiceEnabledBtn = findViewById(R.id.voice_enabled_btn);



        parentRelativeLayout = findViewById(R.id.parentRelativeLayout);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        validateAndPlay();
        imageView.setBackgroundResource(R.drawable.music1);

        MobileAds.initialize(this, "ca-app-pub-8468860238019254~6224808409"); // change from test id to your id


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> matchesFound = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);

                if (matchesFound != null){
                    if (mode.equals("ON")){
                        keeper = matchesFound.get(0);
                        if (keeper.equals("pause")){
                            playPauseSong();
                            Toast.makeText(MainActivity.this, "Now " + keeper + "d", Toast.LENGTH_SHORT ).show();
                        }
                        else  if (keeper.equals("play")){
                            playPauseSong();
                            Toast.makeText(MainActivity.this, "Now " + keeper + "ing", Toast.LENGTH_SHORT ).show();
                        }
                        else  if (keeper.equals("next")){
                            playNextSong();
                            Toast.makeText(MainActivity.this, "Playing " + keeper + " song" , Toast.LENGTH_SHORT ).show();
                        }
                        else  if (keeper.equals("last")){
                            playPreviousSong();
                            Toast.makeText(MainActivity.this, "Playing " + keeper + " song", Toast.LENGTH_SHORT ).show();
                        }
                    }



                }

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });


        parentRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        if (mode.equals("ON")){
                            Toast.makeText(MainActivity.this, "Listening", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;
                }
                return false;
            }
        });

        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("ON")){
                    mode = "OFF";
                    voiceEnabledBtn.setText("Voice Enabled Mode - OFF");
                    lowerRelative.setVisibility(View.VISIBLE);
                } else {
                    mode = "ON";
                    voiceEnabledBtn.setText("Voice Enabled Mode - ON");
                    lowerRelative.setVisibility(View.GONE);
                }
            }
        });
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseSong();
            }
        });

        nextBtb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.getCurrentPosition() > 0){
                    playNextSong();
                }
            }
        });
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myMediaPlayer.getCurrentPosition() > 0){
                    playPreviousSong();
                }
            }
        });




    }
    @Override
    public void onBackPressed() {
        myMediaPlayer.stop();
        myMediaPlayer.release();
        super.onBackPressed();

    }



    private void validateAndPlay(){
        if (myMediaPlayer != null){
            myMediaPlayer.stop();
            myMediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mySongs = (ArrayList) bundle.getParcelableArrayList("song");
        mSongName = mySongs.get(position).getName();
        String songName = intent.getStringExtra("name");

        songNameTxt.setText(songName);
        songNameTxt.setSelected(true);

        position = bundle.getInt("position", 0);
        Uri uri = Uri.parse(mySongs.get(position).toString());

        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);
        myMediaPlayer.start();





    }

    private void checkVoiceCommandPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){

                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }

        }
    }

    private void playPauseSong(){
        imageView.setBackgroundResource(R.drawable.music1);

        if (myMediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.play);
            myMediaPlayer.pause();
        }
        else {
            playPauseBtn.setImageResource(R.drawable.pause);
            myMediaPlayer.start();

            imageView.setBackgroundResource(R.drawable.music2);

        }
    }

    private void playNextSong(){
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position + 1) %mySongs.size());

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);

        mSongName = mySongs.get(position).toString();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();


        imageView.setBackgroundResource(R.drawable.mi);

        if (myMediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.pause);
        }
        else {
            playPauseBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.music2);

        }
    }


    private void playPreviousSong(){
        myMediaPlayer.pause();
        myMediaPlayer.stop();
        myMediaPlayer.release();

        position = ((position - 1) < 0 ? mySongs.size() - 1 : (position - 1));

        Uri uri = Uri.parse(mySongs.get(position).toString());
        myMediaPlayer = MediaPlayer.create(MainActivity.this, uri);
        mSongName = mySongs.get(position).toString();
        songNameTxt.setText(mSongName);
        myMediaPlayer.start();


        imageView.setBackgroundResource(R.drawable.music1);

        if (myMediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.pause);
        }
        else {
            playPauseBtn.setImageResource(R.drawable.play);
            imageView.setBackgroundResource(R.drawable.music2);

        }
    }


}
