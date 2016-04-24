package com.arthurassuncao.vol_ldws.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.arthurassuncao.vol_ldws.model.IMU;
import com.arthurassuncao.vol_ldws.net.HttpClient;
import com.arthurassuncao.vol_ldws.net.Webservice;
import com.arthurassuncao.vol_ldws.util.Constants;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by arthur on 28/03/16.
 */
public class ServiceUpdate extends IntentService {
    private Handler handler;
    private Thread runnable;
    private static final int INTERVAL = 2000;
    private Context context;
    private boolean stop;
    private StopServiceReceiver receiver;

    public class StopServiceReceiver extends BroadcastReceiver {
        public static final String ACTION_STOP = "com.arthurassuncao.vol_ldws.service.stop";

        @Override
        public void onReceive(Context context, Intent intent) {
            stop = true;
        }
    }

    public ServiceUpdate(){
        super(ServiceUpdate.class.getName());
    }

    public ServiceUpdate(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        IntentFilter filter = new IntentFilter(StopServiceReceiver.ACTION_STOP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new StopServiceReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        //In the work you are doing
        if(stop==true){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
            stopSelf();
        }

        start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d("service", "onStart");
        onHandleIntent(intent);
    }

    public int start() {
        Log.d("service", "start");
        handler = new Handler();

        runnable = new Thread(new Worker(this));

        handler.postDelayed(runnable, INTERVAL);
        return START_STICKY;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("service", "onStartCommand");
//        handler = new Handler();
////        context = VolLDWSActivity.getInstance();
//
//        runnable = new Thread(new Worker(this));
//
//        handler.postDelayed(runnable, INTERVAL);
//        return START_STICKY;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("service", "stop");
        Toast.makeText(this, "Serviço finalizado", Toast.LENGTH_SHORT).show();
        handler.removeCallbacks(runnable);
        try {
            if(receiver != null){
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
                receiver = null;
            }
        }
        catch (IllegalArgumentException e) {
            Log.d("Service OnDestroy", e.getMessage());
        }
    }

    class Worker implements Runnable{
        private Context context;

        public Worker(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if(((ServiceUpdate)context).stop == true){
                return;
            }
            ArrayList<IMU> imus = null;

            try{
                imus = getData();
                update(imus);
            }
            catch(IOException e){
                Log.e("GetData", "Erro ao acessar webservice");
            }

            handler.postDelayed(this, INTERVAL);
        }

        private void update(ArrayList<IMU> imus){
            if(imus != null){
                //this.activity.updateData(imus);
                /*
                * Creates a new Intent containing a Uri object
                * BROADCAST_ACTION is a custom Intent action
                */
                Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
                    // Puts the status into the Intent
                    .putParcelableArrayListExtra(Constants.EXTENDED_DATA_STATUS, imus);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
            }
            else{
                //this.activity.updateData(null);
//                Intent localIntent = new Intent(Constants.BROADCAST_ACTION)
//                        // Puts the status into the Intent
//                        .putExtra(Constants.EXTENDED_DATA_STATUS, (String) null);
//                // Broadcasts the Intent to receivers in this app.
//                LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                Log.d("update", "Nada a enviar");
            }
        }


        private ArrayList<IMU> getData(Void... params) throws IOException{
            ArrayList<IMU> imus = null;

            if(true || Webservice.isOnline()) {
                String dataString = getDataString(Webservice.URL_WEB_SERVICE);

                Gson gson = new Gson();
                IMU[] imusArray = gson.fromJson(dataString, IMU[].class);
                imus = new ArrayList<IMU>(Arrays.asList(imusArray));
            }

            return imus;
        }

        private String getDataString(String url) throws IOException {
            String data = null;
            HttpClient httpClient = new HttpClient(url, HttpClient.GET);
            httpClient.executar();
            data = httpClient.getTextoResposta();

            return data;
        }
    }

}
