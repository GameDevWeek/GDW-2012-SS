package de.hochschuletrier.gdw.ss12.game.components.factories;

import de.hochschuletrier.gdw.ss12.game.components.InputComponent;

public class InputComponentFactory extends SimpleComponentFactory {

    public InputComponentFactory() {
        super("Input", InputComponent.class, false);
    }
}
