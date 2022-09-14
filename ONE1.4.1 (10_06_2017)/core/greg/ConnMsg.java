/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.greg;

import java.util.*;
import core.Connection;
import core.Message;

public class ConnMsg {

	private Connection c;
	private Message m;

	public ConnMsg(Connection c, Message m) {
		this.c = c;
		this.m = m;
	}
	
	// Conexões
	public void setConnection(Connection c) { this.c = c; }
	public Connection getConnection() { return this.c; }

	// Mensagens
	public void setMessage(Message m) { this.m = m; }
	public Message getMessage() { return this.m; }
}
