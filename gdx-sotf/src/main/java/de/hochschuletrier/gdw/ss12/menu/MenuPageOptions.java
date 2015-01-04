package de.hochschuletrier.gdw.ss12.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import de.hochschuletrier.gdw.commons.gdx.menu.MenuManager;
import de.hochschuletrier.gdw.commons.gdx.utils.ScreenUtil;
import de.hochschuletrier.gdw.ss12.Settings;

public class MenuPageOptions extends MenuPage {

    private final Label soundLabel, musicLabel;
    private final Slider soundSlider, musicSlider;
    private final TextButton soundMuteButton, musicMuteButton;
    private final TextButton fullscreenButton;

    public MenuPageOptions(Skin skin, MenuManager menuManager) {
        super(skin, "menu_bg_options");

        int y = 400;
        createLabel(100, y).setText("Vollbild Modus:");
        fullscreenButton = createToggleButton(440, y, "Aus", this::onFullscreenChanged);
        y -= 50;

        createLabel(100, y).setText("Sound:");
        soundSlider = createSlider(170, y, this::onSoundVolumeChanged);
        soundLabel = createLabel(380, y);
        soundMuteButton = createToggleButton(440, y, "Aus", this::onSoundMuteChanged);
        soundMuteButton.setChecked(true);
        soundSlider.setValue(1.0f);
        y -= 50;

        createLabel(100, y).setText("Musik:");
        musicSlider = createSlider(170, y, this::onMusicVolumeChanged);
        musicLabel = createLabel(380, y);
        musicMuteButton = createToggleButton(440, y, "Aus", this::onMusicMuteChanged);
        musicMuteButton.setChecked(true);
        musicSlider.setValue(1.0f);

        addCenteredButton(menuManager.getWidth() - 100, 54, 100, 40, "Zurück", () -> menuManager.popPage());
    }

    private TextButton createToggleButton(int x, int y, String text, Runnable runnable) {
        TextButton button = new TextButton(text, skin, "toggle");
        button.setBounds(x, y, 100, 30);
        addActor(button);

        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                runnable.run();
            }
        });
        return button;
    }

    private Label createLabel(int x, int y) {
        Label label = new Label("", skin);
        label.setBounds(x, y, 60, 30);
        addActor(label);
        return label;
    }

    private Slider createSlider(int x, int y, Runnable runnable) {
        Slider slider = new Slider(0, 1, 0.01f, false, skin);
        slider.setBounds(x, y, 200, 30);
        addActor(slider);
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent event, Actor actor) {
                runnable.run();
            }
        });
        return slider;
    }

    public final String pctToString(float value) {
        int pct = (int) (100 * value);
        return pct + "%";
    }

    private void onFullscreenChanged() {
        boolean fullscreenOn = fullscreenButton.isChecked();
        fullscreenButton.setText(fullscreenOn ? "An" : "Aus");

        ScreenUtil.setFullscreen(fullscreenOn);
    }

    private void onSoundVolumeChanged() {
        soundLabel.setText(pctToString(soundSlider.getValue()));
//        SoundStore.get().setSoundVolume(soundValue);
    }

    private void onMusicVolumeChanged() {
        musicLabel.setText(pctToString(musicSlider.getValue()));
//        SoundStore.get().setMusicVolume(soundValue);
    }

    private void onSoundMuteChanged() {
        boolean soundOn = soundMuteButton.isChecked();
        soundMuteButton.setText(soundOn ? "An" : "Aus");
//        if (SoundStore.get().soundsOn() != soundOn) {
//            SoundStore.get().setSoundsOn(soundOn);
//        }
    }

    private void onMusicMuteChanged() {
        boolean musicOn = musicMuteButton.isChecked();
        musicMuteButton.setText(musicOn ? "An" : "Aus");
//        if (SoundStore.get().isMusicOn() != musicOn) {
//            SoundStore.get().setMusicOn(musicOn);
//        }
    }


    @Override
    public void setVisible(boolean visible) {
        if(soundSlider != null && isVisible() != visible) {
            if(visible) {
                restoreSettings();
            } else {
                storeSettings();
            }
        }
        super.setVisible(visible);
    }
    
    private void restoreSettings() {
        soundSlider.setValue(Settings.SOUND_VOLUME.get());
        soundMuteButton.setChecked(Settings.SOUND_MUTE.get());
        musicSlider.setValue(Settings.MUSIC_VOLUME.get());
        musicMuteButton.setChecked(Settings.MUSIC_MUTE.get());
        fullscreenButton.setChecked(Gdx.graphics.isFullscreen());
    }

    private void storeSettings() {
        Settings.SOUND_VOLUME.set(soundSlider.getValue());
        Settings.SOUND_MUTE.set(soundMuteButton.isChecked());
        Settings.MUSIC_VOLUME.set(musicSlider.getValue());
        Settings.MUSIC_MUTE.set(musicMuteButton.isChecked());
        Settings.FULLSCREEN.set(Gdx.graphics.isFullscreen());
        Settings.flush();
    }
}
