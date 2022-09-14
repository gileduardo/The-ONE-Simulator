/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.greg;

import java.util.*;
import core.Connection;
import core.Message;

public class ConnMsgBoss {

	private ArrayList <ConnMsg> cm;
	
	public ConnMsgBoss() {
		this.cm = new ArrayList<ConnMsg>();
	}

	public void addConnMsg(Connection c, Message m) {
		this.cm.add(new ConnMsg(c, m));
    	}

	// Atualiza ou Adiciona a Conexão 'c' para Mensagem 'm'
	public void updateConnMsg(Connection c, Message m) {

		int pos = getIndexConnection(m);
		ConnMsg objAux;

		// Nova Conexão para Mensagem
		if(pos == -1) {
			addConnMsg(c, m);
		}
		// Atualiza a Conexão da Mensagem
		else {
			objAux = this.cm.get(pos);
			objAux.setConnection(c);
			objAux.setMessage(m);
			this.cm.set(pos, objAux);
    		}
	}

	// Retorna o Index da Conexão para Mensagem 'm'
	public int getIndexConnection(Message m) {

		Message msg;

		if(this.cm != null) {
			for(int i=0;  i<this.cm.size(); i++) {
		  		msg = (Message) this.cm.get(i).getMessage();

		    		if(msg.getId().compareTo(m.getId()) == 0)
			  		return i;
			}
		}

		return -1;	
	}

	// Retorna o Objeto da Conexão para Mensagem 'm'
    	public Connection getConnectionMessage(Message m) {

		Message msg;

		if(this.cm != null) {

			for(int i=0;  i<this.cm.size(); i++) {
				msg = (Message) this.cm.get(i).getMessage();

				if(msg.getId().compareTo(m.getId()) == 0)
		  			return this.cm.get(i).getConnection();
			}
		}

		return null;	
	}

	// Retorna a Lista com todas as Mensagens
	public ArrayList<Message> getListMessage() { 

		ArrayList<Message> lista = new ArrayList<Message>();

		if(this.cm != null) {
			for(int i=0;  i<this.cm.size(); i++) {
				lista.add((Message) this.cm.get(i).getMessage());
			}
		}

		return lista; 
	}

	// Remove a mensagem 'm' que foi entrege ao destino ou transmitida (caso a replicação esteja desabilitada)
    	public int removeMessage(Message m) {

		Message msg;

		if(this.cm != null) {

			for(int i=0;  i<this.cm.size(); i++) {
				msg = (Message) this.cm.get(i).getMessage();

				if(msg.getId().compareTo(m.getId()) == 0) {
					this.cm.remove(i);
					return 1;		
				}
			}
		}

		return 0;	
	}

	// Remove a conexão 'con' que foi perdida com um nó
    	public int removeConnection(Connection con) {

		Connection c;

		if(this.cm != null) {

			for(int i=0;  i<this.cm.size(); i++) {
				c = (Connection) this.cm.get(i).getConnection();

				if(c.equals(con)) {
					this.cm.remove(i);
					return 1;		
				}
			}
		}

		return 0;	
	}
}
