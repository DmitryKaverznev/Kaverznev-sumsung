package ru.kaverznev.spacestudio.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;


import ru.kaverznev.spacestudio.*;
import ru.kaverznev.spacestudio.components.*;
import ru.kaverznev.spacestudio.managers.ContactManager;
import ru.kaverznev.spacestudio.managers.MemoryManager;
import ru.kaverznev.spacestudio.objects.BulletObject;
import ru.kaverznev.spacestudio.objects.LaserObject;
import ru.kaverznev.spacestudio.objects.ShipObject;
import ru.kaverznev.spacestudio.objects.TrashObject;
import ru.kaverznev.spacestudio.objects.UFOObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
public class GameScreen extends ScreenAdapter {

    MyGdxGame myGdxGame;
    GameSession gameSession;
    ShipObject shipObject;

    ArrayList<TrashObject> trashArray;
    ArrayList<BulletObject> bulletArray;
    ArrayList<UFOObject> UFOArray;

    ArrayList<LaserObject> laserArray;

    ContactManager contactManager;

    // PLAY state UI
    MovingBackgroundView backgroundView;
    ImageView topBlackoutView;
    LiveView liveView;
    TextView scoreTextView;
    ButtonView pauseButton;


    ImageView fullBlackoutView;
    TextView pauseTextView;
    ButtonView homeButton;
    ButtonView continueButton;

    private float laserSpawnTime = 0f;

    TextView recordsTextView;
    RecordsListView recordsListView;
    ButtonView homeButton2;

    public GameScreen(MyGdxGame myGdxGame) {
        this.myGdxGame = myGdxGame;
        gameSession = new GameSession();

        contactManager = new ContactManager(myGdxGame.world);

        trashArray = new ArrayList<>();
        UFOArray = new ArrayList<>();
        bulletArray = new ArrayList<>();
        laserArray = new ArrayList<>();

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, 150,
                GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                GameResources.SHIP_IMG_PATH,
                myGdxGame.world
        );

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        topBlackoutView = new ImageView(0, 1180, GameResources.BLACKOUT_TOP_IMG_PATH);
        liveView = new LiveView(305, 1215);
        scoreTextView = new TextView(myGdxGame.commonWhiteFont, 50, 1215);
        pauseButton = new ButtonView(
                605, 1200,
                46, 54,
                GameResources.PAUSE_IMG_PATH
        );

        fullBlackoutView = new ImageView(0, 0, GameResources.BLACKOUT_FULL_IMG_PATH);
        pauseTextView = new TextView(myGdxGame.largeWhiteFont, 282, 842, "Pause");
        homeButton = new ButtonView(
                138, 695,
                200, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Home"
        );
        continueButton = new ButtonView(
                393, 695,
                200, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Continue"
        );

        recordsListView = new RecordsListView(myGdxGame.commonWhiteFont, 690);
        recordsTextView = new TextView(myGdxGame.largeWhiteFont, 206, 842, "Last records");
        homeButton2 = new ButtonView(
                280, 365,
                160, 70,
                myGdxGame.commonBlackFont,
                GameResources.BUTTON_SHORT_BG_IMG_PATH,
                "Home"
        );
    }

    @Override
    public void show() {
        restartGame();
    }

    @Override
    public void render(float delta) {

        handleInput();

        if (gameSession.state == GameState.PLAYING) {
            if (gameSession.shouldSpawnTrash()) {
                TrashObject trashObject = new TrashObject(
                        GameSettings.TRASH_WIDTH, GameSettings.TRASH_HEIGHT,
                        GameResources.TRASH_IMG_PATH,
                        myGdxGame.world
                );
                UFOObject ufoObject = new UFOObject(
                        GameSettings.UFO_WIDTH, GameSettings.UFO_HEIGHT,
                        GameResources.UFO_IMG_PATH,
                        myGdxGame.world
                );
                trashArray.add(trashObject);
                UFOArray.add(ufoObject);
                shipObject.giveSpeed(gameSession.getScore());
            }

            if (shipObject.needToShoot()) {
                BulletObject laserBullet = new BulletObject(
                        shipObject.getX(), shipObject.getY() + shipObject.height / 2,
                        GameSettings.BULLET_WIDTH, GameSettings.BULLET_HEIGHT,
                        GameResources.BULLET_IMG_PATH,
                        myGdxGame.world
                );
                bulletArray.add(laserBullet);
                if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.shootSound.play();
            }

            if (!shipObject.isAlive()) {
                gameSession.endGame();
                recordsListView.setRecords(Objects.requireNonNull(MemoryManager.loadRecordsTable()));
            }

            updateTrash();
            updateBullets();

            backgroundView.move();
            gameSession.updateScore();
            scoreTextView.setText("Score: " + gameSession.getScore());
            liveView.setLeftLives(shipObject.getLiveLeft());

            myGdxGame.stepWorld();
        }

        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            myGdxGame.touch = myGdxGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            switch (gameSession.state) {
                case PLAYING:
                    if (pauseButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        gameSession.pauseGame();
                    }
                    shipObject.move(myGdxGame.touch);
                    break;

                case PAUSED:
                    if (continueButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        gameSession.resumeGame();
                    }
                    if (homeButton.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        myGdxGame.setScreen(myGdxGame.menuScreen);
                    }
                    break;

                case ENDED:

                    if (homeButton2.isHit(myGdxGame.touch.x, myGdxGame.touch.y)) {
                        myGdxGame.setScreen(myGdxGame.menuScreen);
                    }
                    break;
            }

        }
    }

    private void draw() {

        myGdxGame.camera.update();
        myGdxGame.batch.setProjectionMatrix(myGdxGame.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        myGdxGame.batch.begin();
        backgroundView.draw(myGdxGame.batch);

        for (TrashObject trash : trashArray) trash.draw(myGdxGame.batch);
        for (UFOObject UFO : UFOArray) UFO.draw(myGdxGame.batch);

        shipObject.draw(myGdxGame.batch);
        for (BulletObject bullet : bulletArray) bullet.draw(myGdxGame.batch);
        topBlackoutView.draw(myGdxGame.batch);
        scoreTextView.draw(myGdxGame.batch);
        liveView.draw(myGdxGame.batch);
        pauseButton.draw(myGdxGame.batch);

        laserSpawnTime += Gdx.graphics.getDeltaTime();
        float laserSpawnPeriod = 5f;
        if (laserSpawnTime > laserSpawnPeriod) {
            laserSpawnTime -= laserSpawnPeriod;
            spawnLaser();
            updateLaser();
        }



        if (gameSession.state == GameState.PAUSED) {
            fullBlackoutView.draw(myGdxGame.batch);
            pauseTextView.draw(myGdxGame.batch);
            homeButton.draw(myGdxGame.batch);
            continueButton.draw(myGdxGame.batch);
        } else if (gameSession.state == GameState.ENDED) {
            fullBlackoutView.draw(myGdxGame.batch);
            recordsTextView.draw(myGdxGame.batch);
            recordsListView.draw(myGdxGame.batch);
            homeButton2.draw(myGdxGame.batch);
        }

        myGdxGame.batch.end();

    }

    private void updateTrash() {
        Iterator<TrashObject> iteratorTrash = trashArray.iterator();
        while (iteratorTrash.hasNext()) {                // использую итератор - 5 пункт ТЗ
            TrashObject trash = iteratorTrash.next();

            boolean hasToBeDestroyed = !trash.isAlive() || !trash.isInFrame();

            if (!trash.isAlive()) {
                gameSession.destructionRegistration();
                if (myGdxGame.audioManager.isSoundOn) myGdxGame.audioManager.explosionSound.play(0.2f);
            }

            if (hasToBeDestroyed) {
                myGdxGame.world.destroyBody(trash.body);
                iteratorTrash.remove();
            }
        }



        Iterator<UFOObject> iteratorUFO = UFOArray.iterator();
        while (iteratorUFO.hasNext()) {
            UFOObject UFO = iteratorUFO.next();

            boolean hasToBeDestroyed = !UFO.isAlive() || !UFO.isInFrame();

            if (!UFO.isAlive()) {
                gameSession.destructionRegistration();
                if (myGdxGame.audioManager.isSoundOn)
                    myGdxGame.audioManager.explosionSound.play(0.2f);
            }

            if (hasToBeDestroyed) {
                myGdxGame.world.destroyBody(UFO.body);
                iteratorUFO.remove();
            }

            UFO.update();
        }

        Iterator<LaserObject> iteratorLaser = laserArray.iterator();
        while (iteratorLaser.hasNext()) {
            LaserObject laser = iteratorLaser.next();

            if (laser.isCollidingWith(shipObject) && laser.LaserState == LaserObject.LaserStateMode.ON) {
                shipObject.kill();
                myGdxGame.world.destroyBody(laser.body);
                iteratorLaser.remove();
            }
        }
    }

    private void spawnLaser() {
        System.out.println("Spawn!");
        LaserObject laserObject = new LaserObject(
                myGdxGame.world
        );
        laserArray.add(laserObject);
    }

    private void updateLaser() {
        Iterator<LaserObject> iterator = laserArray.iterator();
        System.out.println("Update!");

        while (iterator.hasNext()) {
            LaserObject laser = iterator.next();

            laser.update();

            if (laser.LaserState == LaserObject.LaserStateMode.ON) {
                myGdxGame.world.destroyBody(laser.body);
                iterator.remove();
            }
        }
    }

    private void updateBullets() {
        Iterator<BulletObject> iterator = bulletArray.iterator();

        while (iterator.hasNext()) {
            BulletObject bullet = iterator.next();

            if (bullet.hasToBeDestroyed()) {
                myGdxGame.world.destroyBody(bullet.body);
                iterator.remove();
            }
        }
    }

    private void restartGame() {

        for (int i = 0; i < trashArray.size(); i++) {
            myGdxGame.world.destroyBody(trashArray.get(i).body);
            trashArray.remove(i--);
        }
        for (int i = 0; i < UFOArray.size(); i++) {
            myGdxGame.world.destroyBody(UFOArray.get(i).body);
            UFOArray.remove(i--);
        }

        if (shipObject != null) {
            myGdxGame.world.destroyBody(shipObject.body);
        }

        shipObject = new ShipObject(
                GameSettings.SCREEN_WIDTH / 2, 150,
                GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
                GameResources.SHIP_IMG_PATH,
                myGdxGame.world
        );

        bulletArray.clear();
        gameSession.startGame();
    }

}