
package ru.kaverznev.spacestudio.objects;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import ru.kaverznev.spacestudio.GameResources;
import ru.kaverznev.spacestudio.GameSettings;

public class ShipObject extends GameObject {

    long lastShotTime;
    int livesLeft;
    private int giveSpeedPoint = 1;
    private boolean speedBoosted = false;
    private int scoreCounter = 0;

    public ShipObject(int x, int y, int width, int height, String texturePath, World world) {
        super(texturePath, x, y, width, height, GameSettings.SHIP_BIT, world);
        body.setLinearDamping(10);
        livesLeft = 3;
    }

    public int getLiveLeft() {
        return livesLeft;
    }

    @Override
    public void draw(SpriteBatch batch) {
        putInFrame();
        super.draw(batch);
    }

    public void move(Vector3 vector3) {
        body.applyForceToCenter(new Vector2(
                        (vector3.x - getX()) * GameSettings.SHIP_FORCE_RATIO * giveSpeedPoint,
                        (vector3.y - getY()) * GameSettings.SHIP_FORCE_RATIO * giveSpeedPoint),
                true
        );
    }

    private void putInFrame() {
        if (getY() > (GameSettings.SCREEN_HEIGHT / 2f - height / 2f)) {
            setY((int) (GameSettings.SCREEN_HEIGHT / 2f - height / 2f));
        }
        if (getY() <= (height / 2f)) {
            setY(height / 2);
        }
        if (getX() < (-width / 2f)) {
            setX(GameSettings.SCREEN_WIDTH);
        }
        if (getX() > (GameSettings.SCREEN_WIDTH + width / 2f)) {
            setX(0);
        }
    }

    public boolean needToShoot() {
        if (TimeUtils.millis() - lastShotTime >= GameSettings.SHOOTING_COOL_DOWN) {
            lastShotTime = TimeUtils.millis();
            return true;
        }
        return false;
    }

    @Override
    public void hit() {
        livesLeft -= 1;
    }

    public boolean isAlive() {
        return livesLeft > 0;
    }

    public void giveSpeed(int score) {
        scoreCounter = score;
        if (scoreCounter >= 5000 && !speedBoosted) {
            giveSpeedPoint = 2;
            this.setTexturePath(GameResources.SHIP_FIRE_IMG_PATH);
            speedBoosted = true;
            scoreCounter = 0;

            Timer.schedule(new Task() {
                @Override
                public void run() {
                    resetSpeed();
                }
            }, 10);
        }
    }

    private void resetSpeed() {
        giveSpeedPoint = 1;
        this.setTexturePath(GameResources.SHIP_IMG_PATH);
        speedBoosted = false;
    }
}
