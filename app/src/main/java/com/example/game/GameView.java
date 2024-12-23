package com.example.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isPlaying,isGameOver=false;
    private SharedPreferences prefs;
    private int screenX,screenY,score=0;
    public static float screenRatioX,screenRatioY;
    private List<bullet> bullets;
    private SoundPool soundPool;
    private Random random;
    private Bird[] birds;
    private Paint paint;
    private flight flight;
    private GameActivity activity;
    private int sound;
    private Background background1,background2;
    MediaPlayer backgroundSound;
    public GameView(GameActivity activity ,int screenX,int screenY){


        super(activity);
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            AudioAttributes audioAttributes=new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(AudioAttributes.USAGE_GAME).build();
            soundPool=new SoundPool.Builder().setAudioAttributes(audioAttributes).build();
            backgroundSound = MediaPlayer.create(activity.getApplication(),R.raw.backgroundsound);
            backgroundSound.start();

        }
        else {
            soundPool=new SoundPool(1, AudioManager.STREAM_MUSIC,0);
        }
        sound=soundPool.load(activity,R.raw.shoot,1);
        this.activity=activity;
        prefs=activity.getSharedPreferences("game", Context.MODE_PRIVATE);
        this.screenX=screenX;
        this.screenY=screenY;
        screenRatioX=1920f/screenY;
        screenRatioY=1080f/screenY;

        background1=new Background(screenX,screenY,getResources());
        background2=new Background(screenX,screenY,getResources());

        background2.x=screenX;

        paint=new Paint();
        paint.setTextSize(128);
        paint.setColor(Color.BLACK);

        flight=new flight(this,screenY,getResources());
        bullets=new ArrayList<>();

        birds=new Bird[4];
        for(int i=0;i<4;i++){
            Bird bird=new Bird(getResources());
            birds[i]=bird;
        }

        random=new Random();
    }

    @Override
    public void run(){
    while (isPlaying){
        update();
        draw();
        sleep();

    }
    }
    public void update() {
        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;
        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }
        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        if (flight.isGoingUp) {
            flight.y -= 10 * screenRatioY;
        } else {
            flight.y += 10 * screenRatioY;
        }

        if (flight.y < 0)
            flight.y = 0;

        if (flight.y > screenY - flight.height)
            flight.y = screenY - flight.height;

        List<bullet> Trash = new ArrayList<>();

        for (bullet bullet : bullets) {
            if (bullet.x > screenX) {
                Trash.add(bullet);
            }
            bullet.x += 20 * screenRatioX;

            for (Bird bird:birds){
                if(Rect.intersects(bird.getCollisionShape(),bullet.getCollisionShape())){
                    score++;
                    bird.x=-500;
                    bullet.x=screenX+500;
                    bird.waShot=true;

                }
            }
        }
        for (bullet bullet : Trash) {
            bullets.remove(bullet);
        }


        for (Bird bird : birds) {
            bird.x -= bird.speed;
            if (bird.x + bird.width < 0) {

                if(!bird.waShot){
                    isGameOver=true;
                    return;
                }
                int bound = (int) (2 * screenRatioX);
                bird.speed = random.nextInt(bound);

                if (bird.speed < 5 * screenRatioX) {
                    bird.speed = (int) (2* screenRatioX);
                }
                bird.x = screenX;
                bird.y = random.nextInt(screenY - bird.height);

                bird.waShot=false;
            }
            if(Rect.intersects(bird.getCollisionShape(),flight.getCollisionShape())){
                isGameOver=true;
                return;
            }
        }
    }
    public void draw(){
        if(getHolder().getSurface().isValid())
        {
            Canvas canvas=getHolder().lockCanvas();
            canvas.drawBitmap(background1.background,background1.x,background1.y,paint);
            canvas.drawBitmap(background2.background,background2.x,background2.y,paint);
            for (Bird bird:birds){
                canvas.drawBitmap(bird.getBird(),bird.x,bird.y,paint);
            }
            canvas.drawText(score+"",screenX/2f,164,paint);

            if(isGameOver){
                isPlaying=false;
                canvas.drawBitmap(flight.getDead(),flight.x,flight.y,paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting();
                return;
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y,paint);

            for (bullet bullet:bullets){
             canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);
             }

            getHolder().unlockCanvasAndPost(canvas);

        }
    }

    private void waitBeforeExiting() {
        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity , MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveIfHighScore() {
        if(prefs.getInt("highScore",0)<score){
            SharedPreferences.Editor editor=prefs.edit();
            editor.putInt("highScore:",score);
            editor.apply();
        }
    }

    public void sleep()
    {
        try {
            thread.sleep(17);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    public void resume(){
        isPlaying=true;
        thread=new Thread(this);
        thread.start();

    }

    public void pause()
    {
        try {
            isPlaying=false;
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(event.getX()<screenX/2f){
                    flight.isGoingUp=true;
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp=false;

                break;
        }
        if(event.getX()>screenX/2f)
            flight.toShoot++;
        return true;
    }

    public void newBullet() {
        if (!prefs.getBoolean("isMute",false)) {
            soundPool.play(sound,1,1,0,0,1);
        }

        bullet bullet=new bullet(getResources());
        bullet.x=flight.x+(flight.width);
        bullet.y=flight.y+(flight.height/2);
        bullets.add(bullet);

    }
}
