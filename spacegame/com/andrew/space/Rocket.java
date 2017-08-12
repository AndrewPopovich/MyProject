package com.andrew.space;

public class Rocket extends BaseObject {

    public Rocket(double x, double y) {
        super(x, y, 1);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.setPoint(x, y, 'R');
    }

    @Override
    public void move() {
        y--;
    }
}
