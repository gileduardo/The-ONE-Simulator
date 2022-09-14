
package routing.rpd;

import java.util.*;

import core.Connection;
import core.Message;
import core.velosent.TCUCE;

/* TABELA DAS MELHORES CONEXÕES PARA CADA MENSAGEM */
public class ConMsg {

    public ArrayList <Connection> list_con;
    public ArrayList <Message> list_msg;
    public ArrayList <Double> list_age;
    public ArrayList <TCUCE> list_tcuce;
    
    public ConMsg () {

	this.list_con = new ArrayList<Connection>();
	this.list_msg = new ArrayList<Message>();
	this.list_age = new ArrayList<Double>();
	this.list_tcuce = new ArrayList<TCUCE>();
    }

    public void setConnectionMessage(Connection c, Message m, double a, TCUCE t) {

	int pos = getIndexConnectionMessage(m);

	// New Connection for Message
	if(pos == -1)
		newConnectionMessage(c, m, a, t);
	// Update Connection for Message
	else
		upConnectionMessage(pos, c, a, t);
    }

    public void newConnectionMessage(Connection c, Message m, double a, TCUCE t) {
	// Adiciona a mensagem e sua conexão
        this.list_msg.add(m);
	this.list_con.add(c);
	this.list_age.add(a);
	this.list_tcuce.add(t);
    }

    public void upConnectionMessage(int pos, Connection c, double a, TCUCE t) {
	// Atualiza a conexão para mensagem
	this.list_con.set(pos, c);
	// Atualiza a idade para mensagem
	this.list_age.set(pos, a);
	// Atualiza os dados de contexto do destino
	this.list_tcuce.set(pos, t);
    }

    // Retorna o índice da melhor conexão para a mensagem m
    public int getIndexConnectionMessage(Message m) {

	Message msg;

	if(this.list_msg != null) {
            for(int i=0;  i<this.list_msg.size(); i++) {
                msg = (Message) this.list_msg.get(i);

                if(msg.getId().compareTo(m.getId()) == 0)
                    return i;
            }
        }

        return -1;	
    }
    // Retorna o objeto da melhor conexão para a mensagem m
    public Connection getConnectionMessage(Message m) {

	Message msg;

	if(this.list_msg != null) {
            for(int i=0;  i<this.list_msg.size(); i++) {
                msg = (Message) this.list_msg.get(i);

                if(msg.getId().compareTo(m.getId()) == 0)
                    return this.list_con.get(i);
            }
        }

        return null;	
    }

    // Retorna a idade da melhor conexão para a mensagem m
    public double getAgeMessage(Message m) {

	Message msg;

	if(this.list_msg != null) {
            for(int i=0;  i<this.list_msg.size(); i++) {
                msg = (Message) this.list_msg.get(i);

                if(msg.getId().compareTo(m.getId()) == 0)
                    return this.list_age.get(i);
            }
        }

        return -1;	
    }

    // Remove mensagem da lista
    public int removeMessage(Message m) {

	Message msg;

	if(this.list_msg != null) {
            for(int i=0;  i<this.list_msg.size(); i++) {
                msg = (Message) this.list_msg.get(i);

                if(msg.getId().compareTo(m.getId()) == 0) {
		     this.list_con.remove(i);
		     this.list_msg.remove(i);
		     this.list_age.remove(i);
		     this.list_tcuce.remove(i);
		     return 1;
		}    
            }
        }

	return -1;
    }

    public TCUCE getTcuceMessage(Message m) {

	Message msg;

	if(this.list_msg != null) {
            for(int i=0;  i<this.list_msg.size(); i++) {
                msg = (Message) this.list_msg.get(i);

                if(msg.getId().compareTo(m.getId()) == 0)
                    return this.list_tcuce.get(i);
            }
        }

        return null;
    }

    public List<Message> getListMessage() { return this.list_msg; }
}

