package com.arthurassuncao.vol_ldws.controller;

import com.arthurassuncao.vol_ldws.model.Point;

import java.util.ArrayList;
import java.util.List;

public class EWMA {
    private volatile boolean initialized = false;
    private volatile double ucl = 0.0;
    private volatile double lcl = 0.0;
    private volatile double sigma = 2.0;
    private volatile double lambda = 0.2; // 0 a 1
    private volatile double rate;
    private volatile double std;
    private volatile double sum;
    private final List<Double> samples = new ArrayList<Double>();

    public EWMA(double lambda, double sigma) {
        this.sigma = sigma;
        this.lambda = lambda;
    }

    /**
     * Update the moving average with a new value.
     *
     * @param n the new value
     */
    public void update(double n) {
        samples.add(n);
    }

    private double sum(){
        double sum = 0.0;
        for(double sample: samples){
            sum += sample;
        }
        return sum;
    }

    private double average(){
        return sum()/samples.size();
    }

    private double variance(){
        double avg = average();
        double sumV = 0;
        for(double sample: samples){
            sumV += Math.pow((sample - avg), 2);
        }
        double variance = sumV / samples.size();
        return variance;
    }

    public double std(){
        this.std = Math.sqrt(variance());
        return std;
    }

    private void calculateZ0(){
        double z0 = average();
        rate = z0;
        std();

//		z0 = (lambda * sample) + (1-lambda) * samplePrev;
    }

    public void calculateRate(){
        double ratePrev = rate;
        double sample = samples.get(samples.size() - 1);
        rate = (lambda * sample) + (1-lambda) * ratePrev;
    }

    public void calibrate(double samplesCalib[]){
        for(double sampleCalib: samplesCalib){
            samples.add(sampleCalib);
        }
        calculateZ0();
        calculateLimits();
        reset();
    }

    public void calibrate(){
        calculateZ0();
        calculateLimits();
        reset();
    }

    private void reset(){
        samples.clear();
    }

    private void calculateLcl(){
        lcl = rate - (std*sigma*(Math.sqrt(lambda / (2-lambda))));
    }

    private void calculateUcl(){
        ucl = rate + (std*sigma*(Math.sqrt(lambda / (2-lambda))));
    }

    public void calculateLimits(){
        calculateLcl();
        calculateUcl();
    }

    public void setStd(double std){
        this.std = std;
    }

    public void setZ0(double z0){
        if(rate == 0){
            this.rate = z0;
        }
        else{
            throw new IllegalAccessError("Z0 já existe");
        }
    }

    public Point getRate(){
        double underControl = 0;
        if(rate < lcl){
            underControl = rate - lcl;
        }
        else if(rate > ucl){
            underControl = rate - ucl;
        }
        Point p = new Point(rate, underControl);
        return p;
    }

    public double getUcl() {
        return ucl;
    }

    public double getLcl() {
        return lcl;
    }
}