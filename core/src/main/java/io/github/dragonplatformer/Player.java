package io.github.dragonplatformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    public Sprite sprite;
    public Body body;
    public boolean leftMove;
    public boolean rightMove;
    public boolean jump;
    public boolean glide;
    public int maxJumps;
    public int numJumps;
    private int groundContact;

    public Player(Texture texture, int width, int height, World world) {
        this.sprite = new Sprite(texture);
        this.sprite.setSize(width, height);

        BodyDef playerBodyDef = new BodyDef();
        playerBodyDef.type = BodyDef.BodyType.DynamicBody;
        playerBodyDef.position.set(5, 10);
        this.body = world.createBody(playerBodyDef);
        PolygonShape rectangle = new PolygonShape();
        rectangle.setAsBox(1, 1);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = rectangle;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        this.body.createFixture(fixtureDef);
        rectangle.dispose();
        PolygonShape jumpSensorShape = new PolygonShape();
        jumpSensorShape.setAsBox(0.2f, 0.2f, new Vector2(0f, -1f), 0);
        FixtureDef jumpSensorDef = new FixtureDef();
        jumpSensorDef.shape = jumpSensorShape;
        jumpSensorDef.isSensor = true;

        this.body.createFixture(jumpSensorDef);
        this.body.setUserData(this);

        leftMove = false;
        rightMove = false;
        groundContact = 0;
        maxJumps = 4;
        numJumps = maxJumps - 1;
    }

    public void beginContact(Fixture playerFixture, Fixture contactFixture) {
        if (playerFixture.equals(body.getFixtureList().get(1))) {
            groundContact++;
            numJumps = maxJumps;
        }
    }

    public void endContact(Fixture playerFixture, Fixture contactFixture) {
        if (playerFixture.equals(body.getFixtureList().get(1))) {
            groundContact--;
        }
    }

    public void setLeftMove(boolean leftMove) {
        if (rightMove && leftMove) this.rightMove = false;
        this.leftMove = leftMove;
    }

    public void setRightMove(boolean rightMove) {
        if (leftMove && rightMove) this.leftMove = false;
        this.rightMove = rightMove;
    }

    public void setJump(boolean jump) {
        this.jump = jump;
    }

    public void setGlide(boolean glide) {
        this.glide = glide;
    }
}
