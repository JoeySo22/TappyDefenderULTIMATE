package edu.utep.cs5381.tappydefenderultimate;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.utep.cs5381.tappydefenderultimate.model.EnemyShip;
import edu.utep.cs5381.tappydefenderultimate.model.PlayerShip;
import edu.utep.cs5381.tappydefenderultimate.model.SpaceDust;

public class TDView extends SurfaceView
        implements Runnable {
    // The only player
    private PlayerShip player;
    // Holder for drawing and updating
    private SurfaceHolder holder;
    // For drawing
    private Canvas canvas;
    private Paint paint;
    // Our threadsafe collection of ships.
    private List<EnemyShip> enemyShips = new CopyOnWriteArrayList<>();
    // Our threadsafe collection of dusts (lol).
    private List<SpaceDust> dusts = new CopyOnWriteArrayList<>();
    private Thread gameThread;
    // Play and Pause flag
    private boolean playing;
    // Paint for just dust
    private Paint dustPaint;

    public TDView(Context ctx, int x, int y) {
        super(ctx);
        // Init our player with bitmap
        player = new PlayerShip(ctx, x, y, BitmapFactory.decodeResource(
                ctx.getResources(), R.drawable.ship));
        // Get our holder from our view supplied
        holder = getHolder();
        paint = new Paint();
        dustPaint = new Paint();
        dustPaint.setColor(Color.WHITE);
        // Create all of our enemy ships, can change for more.
        for (int i = 1; i <= 3; i++){
            enemyShips.add(new EnemyShip(ctx, x, y, BitmapFactory.decodeResource(
                    ctx.getResources(), R.drawable.enemy)));
        }
        // Create loldusts
        for (int i = 1; i <= 20; i++)
            dusts.add(new SpaceDust(ctx, x, y));
    }


    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            control();
        }
    }
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    private void update() {
        // Each enemyship needs to check if there's collision. If so we "destroy" and reset the ship
        for (EnemyShip enemy: enemyShips) {
            if (Rect.intersects(player.getHitbox(), enemy.getHitbox())) {
                Log.d("Collision Detection", "Enemy collided into player!");
                enemy.resetShip();
            }
        }
        // Player needs to update as well
        player.update(0);
        // Enemy ship gets notice of speed of player.
        for (EnemyShip enemy: enemyShips) {
            enemy.update(player.getSpeed());
        }
        // Transposition for dust too
        for (SpaceDust dust: dusts) {
            // Doesn't need to know of any speed. 0 placeholder
            dust.update(0);
        }
    }

    private void draw() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            // Black background
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            // Draw Dust
            for (SpaceDust dust: dusts) {
                canvas.drawCircle(dust.getX(), dust.getY(), dust.getRadius(),dustPaint);
            }
            // Draw Player
            // Self explanitory, debug hitboxes so we can see them.
            Paint debugPaint = new Paint();
            debugPaint.setColor(Color.WHITE);
            canvas.drawRect(player.getHitbox(), debugPaint);
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),  player.getY(), paint);
            // Draw Enemies
            for (EnemyShip enemy: enemyShips) {
                canvas.drawRect(enemy.getHitbox(), debugPaint);
                canvas.drawBitmap(enemy.getBitmap(), enemy.getX(), enemy.getY(), paint);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            // This slows down our CPU so that we can catch frames and changes.
            // tempo of the game, can increase or decrease.
            gameThread.sleep(17); // in milliseconds
        } catch (InterruptedException e) {
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            // Simple controls for our player. Goes up and speeds up when pressed down.
            case MotionEvent.ACTION_DOWN:
                player.setBoosting(true);
                break;
            case MotionEvent.ACTION_UP:
                player.setBoosting(false);
                break;
        }
        return true;
    }
}
