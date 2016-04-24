package com.arthurassuncao.vol_ldws;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arthurassuncao.vol_ldws.controller.AnalysisEWMA;
import com.arthurassuncao.vol_ldws.model.IMU;
import com.arthurassuncao.vol_ldws.net.task.TaskGetData;
import com.arthurassuncao.vol_ldws.service.ServiceReceiver;
import com.arthurassuncao.vol_ldws.service.ServiceUpdate;
import com.arthurassuncao.vol_ldws.util.Constants;
import com.arthurassuncao.vol_ldws.util.Utilities;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xdroid.toaster.Toaster;

public class VolLDWSActivity extends AppCompatActivity implements ServiceReceiver.IServiceReceiverListener{

    private AnalysisEWMA ewma;
    private LineChart ewmaChart;
    private boolean isMonitoring;
    private boolean isCalibrated;
    private int readingsCalibrate;
    private static Context sInstance;
    private ServiceReceiver receiverService;
    private IntentFilter mUpdateIntentFilter;
    private long count;
    ExecutorService executorService;
    private String fileName;

    private static final int MAX_READINGS_CALIBRATE = 30;
    private static final int MAX_ENTRIES_VISIBLE = 30;
    private static final boolean TEST = true;
    private static final boolean USE_GIR = true;

    private static final int REQUEST_CODE = 0x11;

    private String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // save file
            } else {
                Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        setContentView(R.layout.activity_vol_ldws);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE); // without sdk version check

        Button run = (Button)findViewById(R.id.btn_run);
        Button stop = (Button)findViewById(R.id.btn_stop);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                track(v);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop(v);
            }
        });

        executorService = Executors.newSingleThreadExecutor();

        ewma = new AnalysisEWMA();
        ewmaChart = (LineChart)findViewById(R.id.ewma_chart);
        // limita o numero de entradas visiveis
        ewmaChart.setVisibleXRangeMaximum(MAX_ENTRIES_VISIBLE);

        YAxis yAxisRight = ewmaChart.getAxisRight();
        yAxisRight.setEnabled(false);

        fillChart();

        if (android.os.Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vol_ldw, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stop(null);
//        unregisterReceiver(receiverService);
        executorService.shutdown();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addLimitsChart(double lclValue, double uclValue){
        YAxis leftAxis = ewmaChart.getAxisLeft();

        LimitLine lcl = new LimitLine((float)lclValue, "LCL");
        lcl.setLineColor(Color.LTGRAY);
        lcl.setLineWidth(2f);
        lcl.setTextColor(Color.LTGRAY);
        lcl.setTextSize(10f);

        leftAxis.addLimitLine(lcl);
        leftAxis.setDrawLimitLinesBehindData(true);


//        YAxis rightAxis = ewmaChart.getAxisRight();

        LimitLine ucl = new LimitLine((float)uclValue, "UCL");
        ucl.setLineColor(Color.LTGRAY);
        ucl.setLineWidth(2f);
        ucl.setTextColor(Color.LTGRAY);
        ucl.setTextSize(10f);

        leftAxis.addLimitLine(ucl);
        leftAxis.setDrawLimitLinesBehindData(true);
    }

    private void updateChart(IMU data){
        if(!TEST) {
            //        Log.d("updateChart", "atualizado " + data.getTimestamp());
            LineData lineData = ewmaChart.getData();

            if (lineData != null) {
                ILineDataSet setAccX = lineData.getDataSetByIndex(0);
                ILineDataSet setAccY = lineData.getDataSetByIndex(1);
                ILineDataSet setGirX = null;
                ILineDataSet setGirY = null;
                if(USE_GIR) {
                    setGirX = lineData.getDataSetByIndex(2);
                    setGirY = lineData.getDataSetByIndex(3);
                }

                int count = lineData.getXValCount();
                this.count++;
                lineData.addXValue(String.valueOf(this.count));

                lineData.addEntry(new Entry((float) data.getAcc().getX(), setAccX.getEntryCount()), 0);
                lineData.addEntry(new Entry((float) data.getAcc().getY(), setAccY.getEntryCount()), 1);
                if(USE_GIR) {
                    lineData.addEntry(new Entry((float) data.getGir().getX(), setGirX.getEntryCount()), 2);
                    lineData.addEntry(new Entry((float) data.getGir().getY(), setGirY.getEntryCount()), 3);
                }

                if (count > MAX_ENTRIES_VISIBLE + 1) {
                    lineData.removeXValue(0);
                    lineData.removeEntry(0, 0);
                    lineData.removeEntry(0, 1);
                    if(USE_GIR) {
                        lineData.removeEntry(0, 2);
                        lineData.removeEntry(0, 3);
                    }
                }

                ewmaChart.notifyDataSetChanged();
                ewmaChart.invalidate();

                // move pra ultima entrada
                ewmaChart.moveViewToX(lineData.getXValCount() - (MAX_ENTRIES_VISIBLE + 1));
            }
        }
    }

    private void fillChart(){
        ArrayList<Entry> val0 = new ArrayList<Entry>();
        ArrayList<Entry> val1 = new ArrayList<Entry>();
        ArrayList<Entry> val2 = null;
        ArrayList<Entry> val3 = null;
        if(USE_GIR) {
            val2 = new ArrayList<Entry>();
            val3 = new ArrayList<Entry>();
        }

        Entry entry0 = new Entry(0, 0); // 0 == quarter 1
        val0.add(entry0);
        val1.add(entry0);
        if(USE_GIR) {
            val2.add(entry0);
            val3.add(entry0);
        }

        LineDataSet setAccX = new LineDataSet(val0, "Acc X");
        setAccX.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAccX.setDrawValues(false);
        setAccX.setColor(Color.rgb(25, 110, 210));

        LineDataSet setAccY = new LineDataSet(val1, "Acc Y");
        setAccY.setAxisDependency(YAxis.AxisDependency.LEFT);
        setAccY.setDrawValues(false);
        setAccY.setColor(Color.rgb(76, 175, 80));

        LineDataSet setGirX = null;
        LineDataSet setGirY = null;
        if(USE_GIR) {
            setGirX = new LineDataSet(val2, "Gir X");
            setGirX.setAxisDependency(YAxis.AxisDependency.LEFT);
            setGirX.setDrawValues(false);
            setGirX.setColor(Color.rgb(255, 160, 0));

            setGirY = new LineDataSet(val3, "Gir Y");
            setGirY.setAxisDependency(YAxis.AxisDependency.LEFT);
            setGirY.setDrawValues(false);
            setGirY.setColor(Color.rgb(255, 87, 34));
        }

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setAccX);
        dataSets.add(setAccY);
        if(USE_GIR) {
            dataSets.add(setGirX);
            dataSets.add(setGirY);
        }

        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("");
        count++;

        LineData data = new LineData(xVals, dataSets);
        ewmaChart.setData(data);
        ewmaChart.invalidate(); // refresh
    }

    public void calibrate(){
        isCalibrated = false;
        Button b = (Button)findViewById(R.id.btn_run);
        b.setText(R.string.calibrando);
        Log.d("calibrate", "Calibrando");

        if(fileName == null){
            Calendar cal = Calendar.getInstance();
            int millisecond = cal.get(Calendar.MILLISECOND);
            int second = cal.get(Calendar.SECOND);
            int minute = cal.get(Calendar.MINUTE);
            //24 hour format
            int hourofday = cal.get(Calendar.HOUR_OF_DAY);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            fileName = String.format("results_%02d-%02d%02d%02d.csv", day, hourofday, minute, second);
        }

        startService();
    }

    public void monitor(){
        isMonitoring = true;
        Button b = (Button)findViewById(R.id.btn_run);
        b.setText(R.string.monitorando);
        Log.d("monitor", "Monitorando");
        stopService();
        startService();
    }

    private void dump(List<IMU> data){
        List<IMU> newData = new ArrayList<IMU>(data);
        Thread t = new Thread(new WorkerSaveFile(this, newData, fileName));
        if(!executorService.isShutdown()) {
            executorService.execute(t);
        }
    }

    public void updateData(List<IMU> data){
        if(data != null){
            Log.d("updateData", "Add data");
            Collections.sort(data);
            for(IMU imu: data){
                //Log.d("updateData", String.valueOf(imu.getTimestamp()));
                ewma.addData(imu);
                updateChart(imu);
                ewma.calculate();
            }
            //salva em arquivo
            dump(data);
        }
        else{
            Log.d("updateData", "Error");
        }
        if(!isCalibrated){
            Log.d("updateData", "Nao esta calibrado");
            if(readingsCalibrate < MAX_READINGS_CALIBRATE){
                readingsCalibrate++;
                if(readingsCalibrate == MAX_READINGS_CALIBRATE){
                    ewma.calibrate();
                    double limits[] = ewma.getLimitsAccX();
                    addLimitsChart(limits[0], limits[1]);
                    limits = ewma.getLimitsAccY();
                    addLimitsChart(limits[0], limits[1]);
                    if(USE_GIR) {
                        limits = ewma.getLimitsGirX();
                        addLimitsChart(limits[0], limits[1]);
                        limits = ewma.getLimitsGirY();
                        addLimitsChart(limits[0], limits[1]);
                    }
                    isCalibrated = true;
                    Button b = (Button)findViewById(R.id.btn_run);
                    b.setText(R.string.calibrado);
                    b.setText(R.string.monitorar);
                    monitor();
                }
                else{
                    //pass
                }
            }
        }
        else{
            //pass
//            ewma.calculate();
        }
    }

    // Monitorar
    public void track(View view){
        Utilities.beep();
        if(!isMonitoring) {
            if(isCalibrated) {
                Toast.makeText(this, "Monitorando", Toast.LENGTH_SHORT).show();
                monitor();
            }
            else{
                Toast.makeText(this, "Calibrando", Toast.LENGTH_SHORT).show();
                calibrate();
            }
        }
        else{
            Toast.makeText(this, "Monitoramento desativado", Toast.LENGTH_SHORT).show();
            isMonitoring = false;
        }
    }

    private void startService(){
        // The filter's action is BROADCAST_ACTION
        mUpdateIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);

        // Instantiates a new receiverService
        receiverService = new ServiceReceiver();
        receiverService.setListener(this);
        // Registers the ServiceReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(receiverService, mUpdateIntentFilter);

        Intent mServiceIntent = new Intent(this, ServiceUpdate.class);
        this.startService(mServiceIntent);
    }

    private void stopService(){
        Intent sIntent = new Intent();
        sIntent.setAction(ServiceUpdate.StopServiceReceiver.ACTION_STOP);
        sendBroadcast(sIntent);
        try {
            if(receiverService != null) {
                LocalBroadcastManager.getInstance(this).unregisterReceiver(receiverService);
                receiverService = null;
            }
        }
        catch (IllegalArgumentException e) {
            Log.d("stopService", e.getMessage());
        }
    }

    // Parar
    public void stop(View view){
        Log.d("stop", "Parando monitoramento");
        isMonitoring = false;
        Button b = (Button)findViewById(R.id.btn_run);
        b.setText(R.string.monitorar);
        stopService();
    }

    private class WorkerSaveFile implements Runnable{
        private List<IMU> data;
        private Context context;
        private String fileName;
        private boolean mExternalStorageAvailable;
        private boolean mExternalStorageWriteable;

        public WorkerSaveFile(Context context, List<IMU> data, String fileName){
            this.context = context;
            this.data = data;
            this.fileName = "volLDWS/log/" + fileName;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
            try {
                if(mExternalStorageAvailable) {
                    createFile();
                }
            }
            catch(IOException e){
                Log.d("Dump file", "Error ao criar: " + e.getMessage());
            }
        }

        public synchronized void run(){
            try {
                if(mExternalStorageAvailable) {
                    dump(createLine());
                }
            }
            catch(IOException e){
                Log.d("Dump file", "Error: " + e.getMessage());
            }
        }

        private synchronized String createLine(){
            StringBuffer line = new StringBuffer();
            for(IMU imu: data){
                line.append(imu.toString()+"\n");
            }
            return line.toString();
        }

        private synchronized void createFile() throws  IOException{
            File path = Environment.getExternalStorageDirectory();
            if(path == null){
                path = context.getFilesDir();
            }
            File folder = new File(path + "/volLDWS/");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdir();
            }
            if(success){
                folder = new File(path + "/volLDWS/log/");
                success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if(success){
                    File file = new File(path, fileName);
                    if(!file.exists()) {
                        file.createNewFile();
                        Toaster.toast(String.format("Arquivo %s criado", fileName));
                    }
                }
                return;
            }
            Toaster.toast(String.format("Erro ao criar arquivo %s", fileName));
        }

        private synchronized void dump(String line) throws IOException{
            // try SD
            File path = Environment.getExternalStorageDirectory();
            if(path == null){
                path = context.getFilesDir();
            }

            File file = new File(path, fileName);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file, true);
            try {
                stream.write(line.getBytes());
            } finally {
                stream.close();
            }
        }

    }

}