package edu.utep.cs5381.tappydefenderultimate.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

import edu.utep.cs5381.tappydefenderultimate.R;

public class EnemyShip {

    private static final Random random = new Random();

    private Bitmap bitmap;
    private int x, y;
    private int speed = 1;
    private int maxX, minX; // move horizontally from right to left
    private int maxY, minY;
    private Rect hitbox;

    public EnemyShip(Context ctx, int screenX, int screenY){
        bitmap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.enemy);
        maxX = screenX;
        maxY = screenY;
        minX = 0;
        minY = 0;
       resetShip();
        hitbox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public void update(int playerSpeed) {
        x -= playerSpeed;
        x -= speed;
        // Improve this. duplicate code. refactor it
        if (x < minX - bitmap.getWidth()) {
            resetShip();
        }
        hitbox.set(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }

    public Bitmap getBitmap() {  return bitmap; }

    public int getX() { return x; }

    public int getY() { return y; }

    public Rect getHitBox() {
        return hitbox;
    }

    public void resetShip() {
        speed = random.nextInt(10)+10;
        x = maxX;
        y = random.nextInt(maxY) - bitmap.getHeight();
    }
}
