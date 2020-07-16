package edu.utep.cs5381.tappydefenderultimate.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import edu.utep.cs5381.tappydefenderultimate.R;

public class PlayerShip {
    private final Bitmap bitmap;
    private int speed;
    private int x;
    private int y;
    private static final int GRAVITY = -12;
    private static final int MIN_SPEED = 1;
    private static final int MAX_SPEED = 20;

    private boolean boosting;

    private int maxY;
    private int minY;

    private Rect hitbox;

    public void setBoosting(boolean flag) {
        boosting = flag;
    }

    public PlayerShip(Context context, int width, int height) {
        this.x = 50;
        this.y = 50;
        speed = 1;
        bitmap = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ship);
        maxY = height - bitmap.getHeight();
        minY = 0;
        hitbox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
    }

    public void update() {
        if (boosting) {
            speed += 2;
        } else {
            speed -= 5;
        }
        if (speed < MIN_SPEED) {
            speed = MIN_SPEED;
        }
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }

        y -= speed + GRAVITY;
        if (y < minY) {
            y = minY;
        }
        if (y > maxY) {
            y = maxY;
        }
        hitbox.set(x, y, x + bitmap.getWidth(), y + bitmap.getHeight());
    }

    public Rect getHitbox() {
        return hitbox;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSpeed() {
        return speed;
    }
}
