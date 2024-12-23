package com.example.game;

import static androidx.core.content.SharedPreferencesKt.edit;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.SharedPreferencesKt;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


public class GameActivity extends AppCompatActivity {


       MediaPlayer backgroundSound;

    private GameView gameView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Point point =new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        gameView=new GameView(this,point.x,point.y);

        setContentView(gameView);




    }
    protected void onPause(){
        super.onPause();
        gameView.pause();
        backgroundSound.release();
    }

    protected void onResume(){
        super.onResume();
        gameView.resume();
    }

}
