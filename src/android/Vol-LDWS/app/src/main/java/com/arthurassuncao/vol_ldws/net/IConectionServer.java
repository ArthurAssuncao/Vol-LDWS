package com.arthurassuncao.vol_ldws.net;


/**
 * Interface de classe que define os métodos para conexão com o servidor.
 * 
 * @author Arthur Assunção
 */
public interface IConectionServer {
	
	/**
	 * Método que inicia a exibição de mensagens na tela do dispositivo, a partir de um evento a
	 * ser realizado.
	 * 
	 */
	public void comecarExecucao(String msg, boolean progressoIndefinido);
	
	/**
	 * Método que exibe uma mensagem referente ao processo de acesso ao 
	 * servidor na tela do dispositivo.
	 * 
	 */
	public void mostrarMensagem(String msg);
	
	/**
	 * Método que exibe uma mensagem na tela do dispositivo a partir de um evento finalizado.
	 * 
	 */
	public void terminarExecucao(String msg, int resultado, String tipo);
}
