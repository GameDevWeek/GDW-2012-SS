package de.hochschuletrier.gdw.ss12.game.components.factories;

import de.hochschuletrier.gdw.ss12.game.components.PizzaSliceComponent;

public class PizzaSliceComponentFactory extends SimpleComponentFactory {

    public PizzaSliceComponentFactory() {
        super("PizzaSlice", PizzaSliceComponent.class, true);
    }
}
