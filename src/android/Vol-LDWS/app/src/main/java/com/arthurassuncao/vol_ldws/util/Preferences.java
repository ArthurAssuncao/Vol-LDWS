package com.arthurassuncao.vol_ldws.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class Preferences {
	public static final String NOME_PREFERENCIAS = "com.arthurassuncao.vol_ldws";
	private SharedPreferences settings;
	private Context contexto;

	public Preferences(Context contexto) {
		//Restaura as preferencias gravadas
		this.contexto = contexto;
		settings = this.contexto.getSharedPreferences(NOME_PREFERENCIAS, 0);
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getPreferencias(){
		if(settings != null){
			return (Map<String, String>)settings.getAll();
		}
		return null;
	}

	public void addPreferencia(String nome, int valor){
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(nome, valor);

		//Confirma a gravação dos dados
		editor.commit();
	}

	public void addPreferencia(String nome, String valor){
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(nome, valor);

		//Confirma a gravação dos dados
		editor.commit();
	}

	public void addPreferencia(String nome, boolean valor){
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(nome, valor);

		//Confirma a gravação dos dados
		editor.commit();
	}
}
