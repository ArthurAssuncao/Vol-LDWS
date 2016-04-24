package com.arthurassuncao.vol_ldws.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by arthur on 25/03/16.
 */
public class IMU implements Serializable, Comparable<IMU>, Parcelable {
    private long timestamp;
    private Accelerometer acc;
    private Giroscope gir;

    public IMU() {
    }

    public IMU(Accelerometer acc, Giroscope gir) {
        this.acc = acc;
        this.gir = gir;
    }

    public Accelerometer getAcc() {
        return acc;
    }

    public void setAcc(Accelerometer acc) {
        this.acc = acc;
    }

    public Giroscope getGir() {
        return gir;
    }

    public void setGir(Giroscope gir) {
        this.gir = gir;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int compareTo(IMU o2){
        if(this.timestamp == o2.timestamp)
            return 0;
        return this.timestamp < o2.timestamp ? -1 : 1;
    }

    @Override
    public String toString() {
        return String.format("%s,-,%s,-,%d", acc.toString(), gir.toString(), timestamp);
    }

    protected IMU(Parcel in) {
        timestamp = in.readLong();
        acc = (Accelerometer) in.readValue(Accelerometer.class.getClassLoader());
        gir = (Giroscope) in.readValue(Giroscope.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeValue(acc);
        dest.writeValue(gir);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IMU> CREATOR = new Parcelable.Creator<IMU>() {
        @Override
        public IMU createFromParcel(Parcel in) {
            return new IMU(in);
        }

        @Override
        public IMU[] newArray(int size) {
            return new IMU[size];
        }
    };
}