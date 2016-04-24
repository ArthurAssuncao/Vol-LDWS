package com.arthurassuncao.vol_ldws.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * Classe que implementa o método de verificação de conexão com o servidor.
 * @author Arthur Assunção
 */
public final class Conection {

	public static int TIPO_CONEXAO_NONE = 0;
	public static int TIPO_CONEXAO_MOBILE = 1;
	public static int TIPO_CONEXAO_WIFI = 2;

	/**
	 * Construtor default da classe Conection.
	 * 
	 */
	private Conection(){}

	/**
	 * Método que verifica se há uma conexão com o servidor.
	 * @param contexto recebe o contexto da activity atual.
	 * @return um boolena com o status da conexão:
	 *  - true: A conexão com o servidor foi realizada com sucesso;
	 *  - false: Não há uma conexão com o servidor.
	 */
	public static boolean isConectado(Context contexto) {
		ConnectivityManager cn = (ConnectivityManager)contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo infoRede = cn.getActiveNetworkInfo();

		if(infoRede != null && infoRede.isConnected()){
			return true;
		}
		else{
			return false;
		}
	}

	public static int getTipoConexao(Context contexto){
		ConnectivityManager conMan = (ConnectivityManager)contexto.getSystemService(Context.CONNECTIVITY_SERVICE);
		int tipoConexao;
		//mobile
		State mobile = conMan.getNetworkInfo(0).getState();
		//wifi
		State wifi = conMan.getNetworkInfo(1).getState();
		if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
			//mobile
			tipoConexao = TIPO_CONEXAO_MOBILE;
		} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
			//wifi
			tipoConexao = TIPO_CONEXAO_WIFI;
		}
		else{
			tipoConexao = TIPO_CONEXAO_NONE;
		}
		return tipoConexao;
	}
}
