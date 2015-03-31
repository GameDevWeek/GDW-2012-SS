package de.hochschuletrier.gdw.ss12.game.components.factories;

import de.hochschuletrier.gdw.ss12.game.components.BotComponent;

public class BotComponentFactory extends SimpleComponentFactory {

    public BotComponentFactory() {
        super("Bot", BotComponent.class, true);
    }
}
