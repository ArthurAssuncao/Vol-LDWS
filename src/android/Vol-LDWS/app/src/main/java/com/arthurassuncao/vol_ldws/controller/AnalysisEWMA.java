package com.arthurassuncao.vol_ldws.controller;

import com.arthurassuncao.vol_ldws.model.IMU;
import com.arthurassuncao.vol_ldws.model.Point;
import com.arthurassuncao.vol_ldws.util.Utilities;

/**
 * Created by arthur on 20/03/16.
 */
public class AnalysisEWMA {
    EWMA ewmaAccX;
    EWMA ewmaAccY;
//    EWMA ewmaAccZ;
    EWMA ewmaGirX;
    EWMA ewmaGirY;
//    EWMA ewmaGirZ;
//    EWMA ewmaMegX;
//    EWMA ewmaMegY;
//    EWMA ewmaMegZ;
    private int errorLCL;
    private int errorUCL;
    private int errorAccX_LCL;
    private int errorAccX_UCL;
    private int errorAccY_LCL;
    private int errorAccY_UCL;

    private int errorGirX_LCL;
    private int errorGirX_UCL;
    private int errorGirY_LCL;
    private int errorGirY_UCL;

    private boolean analyzeAcc = true;
    private boolean analyzeGir = false;

    public AnalysisEWMA(){
        double lambda = 0.9;
        double sigma = 2.0;
        ewmaAccX = new EWMA(lambda, sigma);
        ewmaAccY = new EWMA(lambda, sigma);
        ewmaGirX = new EWMA(lambda, sigma);
        ewmaGirY = new EWMA(lambda, sigma);
    }

    public void calibrate(){
        ewmaAccX.calibrate();
        ewmaAccY.calibrate();
        ewmaGirX.calibrate();
        ewmaGirY.calibrate();
    }

    public double[] getLimitsMinor(){
        double[] limits = {0,0};
        double lcl = 0.0;
        double ucl = 0.0;
        double temp1 = Math.min(ewmaAccX.getUcl(), ewmaAccY.getUcl());
        double temp2 = Math.min(ewmaGirX.getUcl(), ewmaGirY.getUcl());
        ucl = Math.min(temp1, temp2);

        temp1 = Math.max(ewmaAccX.getUcl(), ewmaAccY.getUcl());
        temp2 = Math.max(ewmaGirX.getUcl(), ewmaGirY.getUcl());
        lcl = Math.max(temp1, temp2);

        limits[0] = lcl;
        limits[1] = ucl;

        return limits;
    }

    public double[] getLimitsMajor(){
        double[] limits = {0,0};
        double lcl = 0.0;
        double ucl = 0.0;
        double temp1 = Math.max(ewmaAccX.getUcl(), ewmaAccY.getUcl());
        double temp2 = Math.max(ewmaGirX.getUcl(), ewmaGirY.getUcl());
        ucl = Math.max(temp1, temp2);

        temp1 = Math.min(ewmaAccX.getUcl(), ewmaAccY.getUcl());
        temp2 = Math.min(ewmaGirX.getUcl(), ewmaGirY.getUcl());
        lcl = Math.min(temp1, temp2);

        limits[0] = lcl;
        limits[1] = ucl;

        return limits;
    }

    public double[] getLimitsAccX(){
        double[] limits = {0,0};
        double lcl = ewmaAccX.getUcl();
        double ucl = ewmaAccX.getUcl();

        limits[0] = lcl;
        limits[1] = ucl;

        return limits;
    }

    public double[] getLimitsAccY(){
        double[] limits = {0,0};
        double lcl = ewmaAccY.getUcl();
        double ucl = ewmaAccY.getUcl();

        limits[0] = lcl;
        limits[1] = ucl;

        return limits;
    }

    public double[] getLimitsGirX(){
        double[] limits = {0,0};
        double lcl = ewmaGirX.getUcl();
        double ucl = ewmaGirX.getUcl();

        limits[0] = lcl;
        limits[1] = ucl;

        return limits;
    }

    public double[] getLimitsGirY(){
        double[] limits = {0,0};
        double lcl = ewmaGirY.getUcl();
        double ucl = ewmaGirY.getUcl();

        limits[0] = lcl;
        limits[1] = ucl;

        return limits;
    }

    public void addData(IMU sensor){
        ewmaAccX.update(sensor.getAcc().getX());
        ewmaAccY.update(sensor.getAcc().getY());
        ewmaGirX.update(sensor.getGir().getX());
        ewmaGirY.update(sensor.getGir().getY());
    }

    public void calculate(){
        boolean beep = false;
        if(analyzeAcc) {
            ewmaAccX.calculateRate();
            Point pAccX = ewmaAccX.getRate();
            if (pAccX.getUnderControl() != 0) {
                beep = true;
                if (pAccX.getUnderControl() < 0) {
                    errorLCL++;
                    errorAccX_LCL++;
                } else {
                    errorUCL++;
                    errorAccX_UCL++;
                }
            }
            ewmaAccY.calculateRate();
            Point pAccY = ewmaAccY.getRate();
            if (pAccY.getUnderControl() != 0) {
                beep = true;
                if (pAccY.getUnderControl() < 0) {
                    errorLCL++;
                    errorAccY_LCL++;
                } else {
                    errorUCL++;
                    errorAccY_UCL++;
                }
            }
        }

        if(analyzeGir) {
            ewmaGirX.calculateRate();
            Point pGirX = ewmaAccX.getRate();
            if (pGirX.getUnderControl() != 0) {
                beep = true;
                if (pGirX.getUnderControl() < 0) {
                    errorLCL++;
                    errorGirX_LCL++;
                } else {
                    errorUCL++;
                    errorGirX_UCL++;
                }
            }
            ewmaGirY.calculateRate();
            Point pGirY = ewmaAccY.getRate();
            if (pGirY.getUnderControl() != 0) {
                beep = true;
                if (pGirY.getUnderControl() < 0) {
                    errorLCL++;
                    errorGirY_LCL++;
                } else {
                    errorUCL++;
                    errorGirY_UCL++;
                }
            }
        }

        if(beep){
            beep();
        }
    }

    private void beep(){
        Utilities.beep();
    }


}
