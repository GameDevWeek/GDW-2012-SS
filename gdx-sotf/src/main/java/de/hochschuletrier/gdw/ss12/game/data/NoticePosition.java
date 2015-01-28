package de.hochschuletrier.gdw.ss12.game.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import java.util.function.Consumer;

public enum NoticePosition {

    TOPLEFT((Vector2 out) -> out.set(0, 0)),
    TOPMID((Vector2 out) -> out.set(Gdx.graphics.getWidth() / 2.0f, 0)),
    TOPRIGHT((Vector2 out) -> out.set(Gdx.graphics.getWidth(), 0)),
    CENTERLEFT((Vector2 out) -> out.set(0, Gdx.graphics.getHeight() / 2.0f)),
    CENTER((Vector2 out) -> out.set(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight() / 2.0f)),
    CENTERRIGHT((Vector2 out) -> out.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 2.0f)),
    BOTTOMLEFT((Vector2 out) -> out.set(0, Gdx.graphics.getHeight())),
    BOTTOMMID((Vector2 out) -> out.set(Gdx.graphics.getWidth() / 2.0f, Gdx.graphics.getHeight())),
    BOTTOMRIGHT((Vector2 out) -> out.set(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

    private final Consumer<Vector2> consumer;

    NoticePosition(Consumer<Vector2> consumer) {
        this.consumer = consumer;
    }

    public void getPosition(Vector2 out) {
        consumer.accept(out);
    }
}
