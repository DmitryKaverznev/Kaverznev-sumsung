
package ru.kaverznev.spacestudio.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import ru.kaverznev.spacestudio.GameSettings;

import java.util.Random;

public class UFOObject extends GameObject {

    private static final int paddingHorizontal = 30;
    private int livesLeft;
    public float UFO_VELOCITY = 20;

    public UFOObject(int width, int height, String texturePath, World world) {
        super(
                texturePath,
                width / 2 + paddingHorizontal + (new Random()).nextInt((GameSettings.SCREEN_WIDTH - 2 * paddingHorizontal - width)),
                GameSettings.SCREEN_HEIGHT + height / 2,
                width, height,
                GameSettings.TRASH_BIT,
                world
        );

        livesLeft = 3;
    }

    public boolean isAlive() {
        return livesLeft > 0;

    }

    public boolean isInFrame() {
        return getY() + height / 2 > 0;
    }

    @Override
    public void hit() {
        livesLeft -= 1;
        UFO_VELOCITY += 5;
    }


    public void update() {
        float horizontalMovement = (float) Math.sin((double) getY() / 20) * 75;
        body.setLinearVelocity(new Vector2(horizontalMovement, -UFO_VELOCITY));
    }

}
