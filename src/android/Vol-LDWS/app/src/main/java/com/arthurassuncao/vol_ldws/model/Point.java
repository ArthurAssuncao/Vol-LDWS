package com.arthurassuncao.vol_ldws.model;

public class Point {
    public volatile double underControl = 0; // < 0 --> erro in lcl, > 0 --> erro em ucl
    public volatile double value;

    public Point(double value, double underControl) {
        this.value = value;
        this.underControl = underControl;
    }

    public double getValue() {
        return value;
    }

    // < 0 --> erro in lcl
    // > 0 --> erro em ucl
    public double getUnderControl() {
        return underControl;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
