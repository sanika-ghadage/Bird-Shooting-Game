package com.example.game;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import static com.example.game.GameView.screenRatioX;
import static com.example.game.GameView.screenRatioY;


public class bullet {
    int x,y,width,height;
    Bitmap bullet;
    bullet (Resources res) {
        bullet = BitmapFactory.decodeResource(res, R.drawable.bullet);
        width = bullet.getWidth();
        height = bullet.getHeight();

        width /= 6;
        height /= 6;


        width *= (int) screenRatioX;
        height *= (int) screenRatioY;

        bullet = Bitmap.createScaledBitmap(bullet, width, height, false);

    }
    Rect getCollisionShape(){
        return new Rect(x,y,x+width,y+height);
    }

}
