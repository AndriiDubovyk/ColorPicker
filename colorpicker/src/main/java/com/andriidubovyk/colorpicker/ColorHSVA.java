package com.andriidubovyk.colorpicker;

import android.graphics.Color;
import android.util.Log;

class ColorHSVA {
    private float hsv[] = new float[3];
    private float a; // alpha:      0.0-1.0
    private int colorHex;

    public ColorHSVA(int color) {
        Color.colorToHSV(color, this.hsv);
        this.a = ((float) Color.alpha(color))/255;
        calculateHexValue();
    }

    public ColorHSVA(int color, float alpha) {
        Color.colorToHSV(color, this.hsv);
        this.a = alpha;
        calculateHexValue();
    }

    public ColorHSVA(float h, float s, float v, float a) {
        this.hsv[0] = h;
        this.hsv[1] = s;
        this.hsv[2] = v;
        this.a = a;
        calculateHexValue();
    }

    public float getHue() {
        return hsv[0];
    }
    public float getSaturation() {
        return hsv[1];
    }
    public float getValue() {
        return hsv[2];
    }
    public float getAlpha() {
        return a;
    }

    public ColorHSVA(float h, float s, float v) {
        this(h, s, v, 1f);
    }

    public ColorHSVA(float h, float s) {
        this(h, s, 1f, 1f);
    }

    public void setHue(float hue) {
        this.hsv[0] = hue;
        calculateHexValue();
    }

    public void setSaturation(float saturation) {
        this.hsv[1] = saturation;
        calculateHexValue();
    }

    public void setValue(float value) {
        this.hsv[2] = value;
        calculateHexValue();
    }

    public void setAlpha(float alpha) {
        this.a = alpha;
        calculateHexValue();
    }

    public int getHexValue() {
        return colorHex;
    }

    public String getHexString(boolean showAlpha) {
        String hex = Integer.toHexString(colorHex);
        while(hex.length()<8) {
            hex = "0"+hex;
        }
        if(!showAlpha) hex = hex.substring(2);
        return "#"+hex;
    }

    @Override
    public String toString() {
        return "h: "+hsv[0]+", s: "+hsv[1]+", v: "+hsv[2]+", a: "+a;
    }

    private void calculateHexValue() {
        this.colorHex = Color.HSVToColor(hsv);
        this.colorHex = setAlpha(this.colorHex, this.a);
    }

    private static int setAlpha(int color, float value) {
        int alpha = Math.round(value*255f);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static ColorHSVA fromHexString(String colorStr, boolean showAlpha) {
        int color;
        try {
            color = Color.parseColor(colorStr);
        } catch (Exception e) {
            color = -1;
        }
        if(color == -1) return null;
        if(showAlpha) return new ColorHSVA(color);
        else return new ColorHSVA(color, 1f);

    }
}
