package edu.utep.cs5381.tappydefenderultimate.model;

import android.content.Context;

import java.util.Random;

public class SpaceDust {

    private int x, y, maxX, maxY, speed, radius;
    private static final Random random = new Random();

    // This class has similarities with EnemyShip.
    public SpaceDust(Context context,int screenX, int screenY) {
        maxX = screenX;
        maxY = screenY;
        setRandomDistance();
        setRandomSpeed();
        setRandomHeight();
        radius = speed - 7;
    }

    private void setRandomDistance() {
        x = random.nextInt(maxX);
    }

    private void setRandomSpeed(){
        speed = random.nextInt(3) + 10;
    }

    //
    public void update() {
        x -= speed;
        if (x < 0) {
            setRandomSpeed();
            x = maxX;
            setRandomHeight();
        }
    }

    private void setRandomHeight() {
        y = random.nextInt(maxY);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getRadius() { return radius; }
}
