package com.arthurassuncao.vol_ldws.net;

import android.net.ParseException;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Classe que implementa os métodos para uma conexão com um servidor remoto, utilizando o protocolo 
 * HTTP.
 * @author Arthur Assunção
 * baseado em: http://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
 */
public class HttpClient {

	//	private DefaultHttpClient cliente; // objeto da classe cliente
//	private HttpRequestBase request; // objeto da classe HttpRequestBase
	private String endereco; // endereço do servidor remoto
	private String tipo; // tipo de acesso (GET / POST / PUT / DELETE / OPTIONS)
	private List<List<String>> parametros; // lista de parâmetros
	private String resposta; // objeto da classe HttpResponse
	private int responseCode;
	private HttpURLConnection connection;

	public static final String POST = "POST"; /** Constante de tipo de metodo "GET" */
	public static final String GET = "GET"; /** Constante de tipo de metodo "POST" */
	public static final String PUT = "PUT"; /** Constante de tipo de metodo "PUT" */
	public static final String DELETE = "DELETE"; /** Constante de tipo de metodo "DELETE" */
	public static final String HEAD = "HEAD"; /** Constante de tipo de metodo "HEAD" */
	public static final String OPTIONS = "OPTIONS"; /** Constante de tipo de metodo "OPTIONS" */
	private static List<HttpCookie> cookies; // Lista de objetos da classe Cookie
	private static final int TIMEOUT_CONEXAO = 5000; // Constante de tempo limite de conexão (5 segundos)
	@SuppressWarnings("unused")
	private static final int TIMEOUT_ESPERA_DADOS = 1000*60*15; // Constante de tempo máximo de espera de dados (15 minutos)
	private final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36";
	private boolean aceitarGzip;
	private String codificacao;

	/**
	 * Construtor sobrecarregado da classe HttpClient.
	 * @param endereco recebe o endereço para conexão (servidor remoto).
	 * @param tipo recebe o tipo de acesso (GET / POST / PUT / DELETE / OPTIONS).
	 */
	public HttpClient(String endereco, String tipo, String codificacao) throws ProtocolException, MalformedURLException, IOException{
		this.endereco = endereco;
		this.codificacao = codificacao;
		aceitarGzip = true;
		this.tipo = tipo;
		URL obj = new URL(endereco);
		this.connection = (HttpURLConnection) obj.openConnection();
		connection.setRequestProperty("Content-Type", "text/html; charset=UTF-8");
		switch (tipo.toUpperCase()){
			case HttpClient.POST:{
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				break;
			}
			case HttpClient.GET:{
				connection.setRequestMethod("GET");
				break;
			}
			case HttpClient.PUT:{
				connection.setRequestMethod("PUT");
				break;
			}
			case HttpClient.DELETE:{
				connection.setRequestMethod("DELETE");
				break;
			}
			case HttpClient.HEAD:{
				connection.setRequestMethod("HEAD");
				break;
			}
			case HttpClient.OPTIONS:{
				connection.setRequestMethod("OPTIONS");
				break;
			}
			default:{
				throw new IllegalArgumentException("Tipo inválido");
			}
		}
		connection.setRequestProperty("User-Agent", USER_AGENT);
		connection.setRequestProperty("Accept-Language", "pt-BR,pt;q=0.8,en-US;q=0.6,en;q=0.4");
		connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
		connection.setRequestProperty("Accept", "*/*");
		this.parametros = new ArrayList<List<String>>();
	}

	public HttpClient(String endereco, String tipo) throws ProtocolException, MalformedURLException, IOException {
		this(endereco, tipo, null);
	}


	/**
	 * Método que adiciona parâmetros na URL.
	 * @param codigo recebe o código do parâmetro.
	 * @param valor recebe o valor do parâmetro a ser setado.
	 */
	public void addParametro(String codigo, String valor) {
		List<String> pair = new ArrayList<String>();
		pair.add(codigo);
		pair.add(valor);
		parametros.add(pair);
	}

	/**
	 * Método que exclui os parâmetros da URL.
	 *
	 */
	public void limparParametros() {
		parametros.removeAll(parametros);
	}

	/**
	 * Método que executa uma conexão HTTP com o servidor.
	 *
	 */
	public void executar() throws  IOException{
		Uri.Builder builder;
		builder = new Uri.Builder();
		String urlParameters = "";
		if(parametros.size() > 0) {
			for (List<String> pair : parametros) {
				builder.appendQueryParameter(pair.get(0), pair.get(1));
			}
			urlParameters = builder.build().getEncodedQuery();
		}

		// Send post request
		if(tipo.equalsIgnoreCase(HttpClient.POST)) {
			connection.setDoInput(true);
			connection.setDoOutput(true);
//		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
//		wr.writeBytes(urlParameters);

			OutputStream os = connection.getOutputStream();
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

			wr.write(urlParameters);
			wr.flush();
			wr.close();
		}

		this.responseCode = connection.getResponseCode();
		System.out.println(String.format("\nSending '%s' request to URL : %s", tipo, endereco));
		System.out.println("Parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		InputStream inputStream = null;
		boolean isGzip = false;
		if(connection.getContentEncoding() != null){
			isGzip = connection.getContentEncoding().equalsIgnoreCase("gzip");
		}
		if (isGzip){
			inputStream = new GZIPInputStream(connection.getInputStream());
		}
		else{
			inputStream = connection.getInputStream();
		}
		BufferedReader in = null;
		if(codificacao != null) {
			in = new BufferedReader(new InputStreamReader(inputStream, codificacao));
		}
		else{
			in = new BufferedReader(new InputStreamReader(inputStream));
		}
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);

		this.resposta = response.toString();

		//print result
		System.out.println("Resposta: " + this.resposta);
	}

	/**
	 * Obtém o status da conexão.
	 * @return um <code>int</code> com o código de retorno do servidor.
	 */
	public int getStatus() {
		return responseCode;
	}

	/**
	 * Obtém o texto de resposta do servidor.
	 * @return uma <code>String</code> com o texto de resposta do servidor.
	 * @throws ParseException
	 * @throws IOException
	 */
	public String getTextoResposta(){
		return this.resposta;
	}

	/**
	 * Obtém o conteúdo da resposta.
	 * @return um objeto da classe <code>InputStream</code> contendo o conteúdo da resposta do 
	 * servidor.
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public InputStream getContent() throws IOException{
		return connection.getInputStream();
	}

	/**
	 * Método sincronizado que limpa todos os cookies da sessão atual.
	 */
	public synchronized static void limparCookies(){
		cookies.clear();
		cookies = null;
	}

	/**
	 * Método sincronizado que obtém uma lista com os cookies da sessão atuaL.
	 * @return uma lista de objetos da classe <code>Cookie</code>.
	 */
	public synchronized static List<HttpCookie> getCookies(){
		return cookies;
	}
}