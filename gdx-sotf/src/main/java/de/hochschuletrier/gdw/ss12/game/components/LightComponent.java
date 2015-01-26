package de.hochschuletrier.gdw.ss12.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import de.hochschuletrier.gdw.ss12.game.Constants;
import de.hochschuletrier.gdw.ss12.game.data.Team;

public class LightComponent extends Component implements Pool.Poolable {

    public float radius;
    public float shrinkPixelPerSecond;
    public Team team;

    @Override
    public void reset() {
        radius = 0;
        shrinkPixelPerSecond = 0;
        team = null;
    }

    public void setFromPlayerRadius(float playerRadius) {
        float m = (2 * playerRadius) / (float) Constants.PLAYER_DEFAULT_SIZE;
        if (m > 1f) {
            float softM = m - 1f;
            softM /= 5f; // Korrekturfaktor, größere Radien sollen nicht so stark die Sehweite ändern
            m = softM + 1f;
        }
        float distance = Constants.PLAYER_DEFAULT_SIGHTDISTANCE * Math.max(1, m);
        radius = distance;
    }
}
