package com.arthurassuncao.vol_ldws.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by arthur on 25/03/16.
 */
public class Giroscope implements Serializable, Parcelable {
    private double x;
    private double y;
    private double z;
    private static double FACTOR = 131;

    public Giroscope() {
    }

    public Giroscope(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x/FACTOR;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y/FACTOR;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z/FACTOR;
    }

    public void setZ(double z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return String.format("%.3f,%.3f,%.3f",getX(), getY(), getZ());
    }

    protected Giroscope(Parcel in) {
        x = in.readDouble();
        y = in.readDouble();
        z = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeDouble(z);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Giroscope> CREATOR = new Parcelable.Creator<Giroscope>() {
        @Override
        public Giroscope createFromParcel(Parcel in) {
            return new Giroscope(in);
        }

        @Override
        public Giroscope[] newArray(int size) {
            return new Giroscope[size];
        }
    };
}