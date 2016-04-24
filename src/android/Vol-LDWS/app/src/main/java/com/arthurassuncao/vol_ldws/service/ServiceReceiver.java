package com.arthurassuncao.vol_ldws.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.arthurassuncao.vol_ldws.VolLDWSActivity;
import com.arthurassuncao.vol_ldws.model.IMU;
import com.arthurassuncao.vol_ldws.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arthur on 03/04/16.
 */
public class ServiceReceiver extends BroadcastReceiver{

    private IServiceReceiverListener listener;

    public ServiceReceiver() {
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("serviceReceiver", "recebeu");
        ArrayList<IMU> data = intent.getParcelableArrayListExtra(Constants.EXTENDED_DATA_STATUS);
        listener.updateData(data);
    }

    public void setListener(IServiceReceiverListener listener) {
        this.listener = listener;
    }

    public interface IServiceReceiverListener{
        public void updateData(List<IMU> data);
    }
}