import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import io.github.dragonplatformer.Entity.Creature.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

public class PlayerTest {
    private GameHeadlessApplication application;

    @BeforeEach
    public void setUp() {
        Gdx.gl = mock(GL20.class);
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 30;
        application = new GameHeadlessApplication();
        new HeadlessApplication(application, config);
        application.create();
        while (application.screen == null) {
            Assertions.assertDoesNotThrow(() -> application.render());
        }
    }

    @Test
    public void setStateTest() {
        for (Player.PlayerState state : Player.PlayerState.values()) {
            application.screen.getPlayer().setState(state);
            Assertions.assertNotNull(application.screen.getPlayer().getCurrentAnim(), "Failed to find an animation for player state: " + state);
        }
    }
}
