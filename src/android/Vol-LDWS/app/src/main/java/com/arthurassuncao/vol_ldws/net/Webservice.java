package com.arthurassuncao.vol_ldws.net;

import com.arthurassuncao.vol_ldws.util.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Classe para acesso ao Web Service
 */
public class Webservice {
    public static String URL_WEB_SERVICE = Constants.URL_WEB_SERVICE;
    /*
    Verifica se o webservice ta online
     */
    public static boolean isOnline(){
        URL u = null;
        try {
            u = new URL(Constants.URL_WEB_SERVICE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection huc = (HttpURLConnection)u.openConnection();
//            huc.setRequestMethod("HEAD");

            huc.connect();
            int code = huc.getResponseCode();
            if (code == 200){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
