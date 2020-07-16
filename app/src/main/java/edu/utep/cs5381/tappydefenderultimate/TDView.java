package edu.utep.cs5381.tappydefenderultimate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Space;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import edu.utep.cs5381.tappydefenderultimate.model.EnemyShip;
import edu.utep.cs5381.tappydefenderultimate.model.PlayerShip;
import edu.utep.cs5381.tappydefenderultimate.model.SpaceDust;

public class TDView extends SurfaceView
        implements Runnable {
    private PlayerShip player;
    private SurfaceHolder holder;
    private Canvas canvas;
    private Paint paint;
    private List<EnemyShip> enemyShips = new CopyOnWriteArrayList<>();
    private List<SpaceDust> dusts = new CopyOnWriteArrayList<>();
    private Thread gameThread;
    private boolean playing;
    private Paint dustPaint;
    private int screenWidth;
    private int screenHeight;

    public TDView(Context ctx, int x, int y) {
        super(ctx);
        player = new PlayerShip(ctx, x, y);
        holder = getHolder();
        paint = new Paint();
        dustPaint = new Paint();
        dustPaint.setColor(Color.WHITE);
        for (int i = 1; i <= 3; i++)
            enemyShips.add(new EnemyShip(ctx, x, y));
        for (int i = 1; i <= 20; i++)
            dusts.add(new SpaceDust(ctx, x, y));
        screenWidth = x;
        screenHeight = y;
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
        for (EnemyShip enemy: enemyShips) {
            if (Rect.intersects(player.getHitbox(), enemy.getHitBox())) {
                Log.d("Collision Detection", "Enemy collided into player!");
                enemy.resetShip();
            }
        }
        player.update();
        for (EnemyShip enemy: enemyShips) {
            enemy.update(player.getSpeed());
        }
        for (SpaceDust dust: dusts) {
            dust.update();
        }
    }
    private void draw() {
        if (holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.argb(255, 0, 0, 0));
            // Draw Dust
            for (SpaceDust dust: dusts) {
                canvas.drawCircle(dust.getX(), dust.getY(), dust.getRadius(),dustPaint);
            }
            // Draw Player
            canvas.drawBitmap(
                    player.getBitmap(),
                    player.getX(),  player.getY(), paint);
            // Draw Enemies
            for (EnemyShip enemy: enemyShips) {
                canvas.drawBitmap(enemy.getBitmap(), enemy.getX(), enemy.getY(), paint);
            }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            gameThread.sleep(17); // in milliseconds
        } catch (InterruptedException e) {
        }

    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
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
