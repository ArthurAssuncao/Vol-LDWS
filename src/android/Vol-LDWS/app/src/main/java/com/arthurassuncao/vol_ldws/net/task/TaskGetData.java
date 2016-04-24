package com.arthurassuncao.vol_ldws.net.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.arthurassuncao.vol_ldws.VolLDWSActivity;
import com.arthurassuncao.vol_ldws.model.IMU;
import com.arthurassuncao.vol_ldws.net.HttpClient;
import com.arthurassuncao.vol_ldws.net.Webservice;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TaskGetData extends AsyncTask<Void, Integer, List<IMU>>{

	private Context context;
	private VolLDWSActivity activity;

	public TaskGetData(Context context) {
		this.context = context;
		this.activity = (VolLDWSActivity)context;
	}

	private List<IMU> getData(Void... params) throws IOException{
		List<IMU> imus = null;

		if(Webservice.isOnline()) {
			String dataString = getDataString(Webservice.URL_WEB_SERVICE);

			Gson gson = new Gson();
			imus = Arrays.asList(gson.fromJson(dataString, IMU[].class));
		}

		return imus;
	}

	@Override
	protected List<IMU> doInBackground(Void... params) {
		List<IMU> imus = null;

		try{
			imus = getData();
		}
		catch(IOException e){
			Log.e("GetData", "Erro ao acessar webservice");
		}

		return null;
	}

	@Override
	protected void onPostExecute(List<IMU> imus) {
		if(imus != null){
			this.activity.updateData(imus);
		}
		else{
			this.activity.updateData(null);
		}
	}

	public String getDataString(String url) throws IOException {
		String data = null;
		HttpClient httpClient = new HttpClient(url, HttpClient.GET);
		httpClient.executar();
		data = httpClient.getTextoResposta();

		return data;
	}
}
