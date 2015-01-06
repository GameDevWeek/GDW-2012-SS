package de.hochschuletrier.gdw.ss12.game.components.data;


public enum PlayerEffect {
    BANANA(1.0f),
    FIRE(1.0f),
    GREEN_SMOKE(2.0f),
    HALUCINATION(0.0f),
    PIZZA(0.0f);
    
    public final float shapeScale;
    
    PlayerEffect(float shapeScale) {
        this.shapeScale = shapeScale;
    }
    public int getBit() {
        return 1 << ordinal();
    }
}
