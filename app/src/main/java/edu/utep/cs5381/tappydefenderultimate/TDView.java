package edu.utep.cs5381.tappydefenderultimate;

import android.content.Context;
import android.content.SharedPreferences;
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
import edu.utep.cs5381.tappydefenderultimate.model.SoundEffect;
import edu.utep.cs5381.tappydefenderultimate.model.SpaceDust;

public class TDView extends SurfaceView
        implements Runnable {
    private final Context context;
    private final int x;
    private final int y;
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
    // Remain distance for a win!
    private float distanceRemaining;
    // Current time
    private long timeTaken;
    // Start time
    private long timeStarted;
    // Fastest time record. Beat this!
    private long fastestTime;
    // Text for paint stuff
    private Paint textPaint;
    // Flag for game ending
    private boolean gameEnded;
    // For sounds
    private SoundEffect soundEffect;
    // For storing our important information
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public TDView(Context ctx, int x, int y) {
        super(ctx);
        this.context = ctx;
        this.x = x;
        this.y = y;
        // Get our holder from our view supplied
        holder = getHolder();
        paint = new Paint();
        dustPaint = new Paint();
        dustPaint.setColor(Color.WHITE);
        textPaint = new Paint();
        soundEffect = new SoundEffect(context);
        preferences = context.getSharedPreferences("HiScores", Context.MODE_PRIVATE);
        editor = preferences.edit();
        fastestTime = preferences.getLong("fastestTime", 1000000);
        startGame(ctx, x, y);
    }

    private void startGame(Context context, int x, int y) {
        // Init our player with bitmap
        player = new PlayerShip(context, x, y, BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ship));
        // Create all of our enemy ships, can change for more.
        enemyShips.clear();
        for (int i = 1; i <= 3; i++){
            enemyShips.add(new EnemyShip(context, x, y, BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.enemy)));
        }
        // Create loldusts
        dusts.clear();
        for (int i = 1; i <= 20; i++)
            dusts.add(new SpaceDust(context, x, y));
        distanceRemaining = 10000; // 10km
        timeTaken = 0;
        timeStarted = System.currentTimeMillis();
        // Flag that game is no longer ended
        gameEnded = false;
    }

    // Time in milliseconds
    private String formatTime(String label, long time) {
        return String.format("%s:%d.%03ds", label, time/1000, time %1000);
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
        // flag for collisions
        boolean hitDetected = false;
        // Each enemyship needs to check if there's collision. If so we "destroy" and reset the ship
        for (EnemyShip enemy: enemyShips) {
            if (Rect.intersects(player.getHitbox(), enemy.getHitbox())) {
                Log.d("Collision Detection", "Enemy collided into player!");
                // raise flag because player collided
                hitDetected = true;
                enemy.resetShip();
            }
        }
        // handle the collision changes
        if (hitDetected) {
            soundEffect.play(SoundEffect.Sound.BUMP);
            if (player.reduceShieldStrength() <= 0) {
                soundEffect.play(SoundEffect.Sound.DESTROYED);
                gameEnded = true;
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
        // If the game is still running...
        if (!gameEnded) {
            distanceRemaining -= player.getSpeed();
            timeTaken = System.currentTimeMillis() - timeStarted;
        }
        // If our player makes it past the distance!
        if (distanceRemaining < 0) {
            soundEffect.play(SoundEffect.Sound.WIN);
            if (timeTaken < fastestTime) {
                editor.putLong("fastestTime", timeTaken);
                editor.commit();
                fastestTime = timeTaken;
            }
            // Finish the game and assert the distance remaining be 0.
            distanceRemaining = 0;
            gameEnded = true;
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

            // Self explanitory, debug hitboxes so we can see them.
            //Paint debugPaint = new Paint();
            //debugPaint.setColor(Color.WHITE);
            //canvas.drawRect(player.getHitbox(), debugPaint);
            int yy = 50;
            if (!gameEnded) {
                // Draw Player
                canvas.drawBitmap(
                        player.getBitmap(),
                        player.getX(),  player.getY(), paint);
                // Draw Hud
                // Our statistics and info should overlay the dust and background but not the ships.
                textPaint.setColor(Color.argb(255,255,255,255));
                textPaint.setStrokeWidth(4);
                textPaint.setTextSize(48);
                textPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(formatTime("Fastest", fastestTime), 10, yy, textPaint);
                canvas.drawText("Shield: " + player.getShieldStrength(), 10, getHeight() - yy,
                        textPaint);
                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(formatTime("Time", timeTaken), getWidth()/2, yy, textPaint);
                canvas.drawText("Distance: " + distanceRemaining/1000 + " KM", getWidth()/2,
                        getHeight() - yy, textPaint);
                textPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText("Speed " + player.getSpeed() * 60 + "MPS", getWidth() - 10,
                        getHeight() - yy, textPaint);
            }
            else {
                // "Game Over!"
                textPaint.setTextAlign(Paint.Align.CENTER);
                textPaint.setTextSize(90);
                canvas.drawText("Game Over!", getWidth()/2, yy + 20, textPaint);
                // Fastest ....
                textPaint.setTextSize(48);
                canvas.drawText(formatTime("Fastest Time", fastestTime), getWidth()/2,
                        yy + 90, textPaint);
                // Time....
                canvas.drawText(formatTime("Time taken", timeTaken), getWidth()/2,
                        yy + 90 + 48, textPaint);
                // Distance Remaining
                canvas.drawText("Distance: " + distanceRemaining, getWidth()/2,
                        yy + 90 + 48 + 48, textPaint);
                // Tap to replay
                textPaint.setTextSize(90);
                canvas.drawText("Tap to replay, Joey/Ruth!", getWidth()/2,
                        yy + 90 + 48 + 48 + 90 , textPaint);
            }
            // Draw Enemies
            for (EnemyShip enemy: enemyShips) {
                //canvas.drawRect(enemy.getHitbox(), debugPaint);
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
                if (gameEnded) {
                    startGame(context, x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
                player.setBoosting(false);
                break;
        }
        return true;
    }
}
