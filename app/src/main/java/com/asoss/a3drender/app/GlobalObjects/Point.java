package com.asoss.a3drender.app.GlobalObjects;

public class Point {
    public float x = 0;
    public float y = 0;

    public Point(float v, float v1) {
        x = v;
        y = v1;

    }

    public Point() {


    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

}
