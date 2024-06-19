package ru.kaverznev.spacestudio.objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

import ru.kaverznev.spacestudio.GameResources;
import ru.kaverznev.spacestudio.GameSettings;

public class LaserObject extends GameObject {

    public Body getBody() {
        return body;
    }
    public enum LaserStateMode {
        ON,
        DENGEROUS
    }
    public LaserStateMode LaserState = LaserStateMode.DENGEROUS;

    public LaserObject(World world) {
        super(
                GameResources.LASER_DENGEROUS,
                new Random().nextInt(GameSettings.SCREEN_WIDTH),
                0,
                GameSettings.LASER_WIDTH,
                100,
                GameSettings.TRASH_BIT,
                world
        );
    }


    public void update() {
        if (LaserState == LaserStateMode.DENGEROUS) {
            LaserState = LaserStateMode.ON;
            setTexturePath(GameResources.LASER_ON);
        }
    }

    public boolean isCollidingWith(ShipObject ship) {
        Body laserBody = this.getBody();
        Fixture laserFixture = laserBody.getFixtureList().get(0);
        Vector2 laserPosition = laserFixture.getBody().getPosition();


        return laserPosition.x >= ship.getX() &&
                laserPosition.x <= ship.getX() + (float) GameSettings.SHIP_HEIGHT;
    }
}