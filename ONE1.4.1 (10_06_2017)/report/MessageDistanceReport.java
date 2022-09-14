/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.HashSet;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.UpdateListener;


/**
 * Message location report. Reports the location (coordinates) of messages.
 * The messages that are reported and the reporting interval can be configured.
 */
public class MessageDistanceReport extends Report implements UpdateListener {
	/** Reporting granularity -setting id ({@value}). 
	 * Defines the interval how often (seconds) a new snapshot of message 
	 * locations is created */
	public static final String GRANULARITY = "granularity";
	/** Reported messages -setting id ({@value}). 
	 * Defines the IDs of the messages that are reported 
	 * (comma separated list)*/
	public static final String REPORTED_MESSAGES = "messages";
	/** value of the granularity setting */
	protected final int granularity;
	/** time of last update*/
	protected double lastUpdate; 
	/** Identifiers of the message which are reported */
	protected HashSet<String> reportedMessages;
	
	/** Distância das mensagens e Indice onde estão armazenadas */
	protected double msg_distance[][];
	protected int msg_time[][];
	protected String msg_index[];
	protected int index;
	protected boolean flag;
	private static int N_MSG = 5;
	private static int N_AMO = 30;

	/**
	 * Constructor. Reads the settings and initializes the report module.
	 */
	public MessageDistanceReport() {
		Settings settings = getSettings();
		this.lastUpdate = 0;	
		this.granularity = settings.getInt(GRANULARITY);
		
		this.reportedMessages = new HashSet<String>();
		for (String msgId : settings.getCsvSetting(REPORTED_MESSAGES)) {
			this.reportedMessages.add(msgId);
		}
		
		// GIL EDUARDO DE ANDRADE
		this.msg_index = new String[this.N_MSG];
		this.msg_distance = new double[N_MSG][N_AMO];
		this.msg_time = new int[N_MSG][N_AMO];
		this.index = 0;
		this.flag = true;
		for(int a=0; a<this.N_MSG; a++) {
			this.msg_index[a] = null;
			for(int b=0; b<this.N_AMO; b++) {
				this.msg_distance[a][b] = 0;
				this.msg_time[a][b] = 0;
			}
		}

		init();
	}

	/**
	 * Creates a new snapshot of the message locations if "granularity" 
	 * seconds have passed since the last snapshot. 
	 * @param hosts All the hosts in the world
	 */
	public void updated(List<DTNHost> hosts) {
		double simTime = getSimTime();
		/* creates a snapshot once every granularity seconds */
		if (simTime - lastUpdate >= granularity) {
			createSnapshot(hosts);
			this.lastUpdate = simTime - simTime % granularity;
		}
	}
	
	/**
	 * Creates a snapshot of message locations 
	 * @param hosts The list of hosts in the world
	 */
	private void createSnapshot(List<DTNHost> hosts) {

		boolean isFirstMessage;
		String reportLine;
		double d = 0, cont = 0;
		
		for (DTNHost host : hosts) {
			isFirstMessage = true;
			reportLine = "";
			for (Message m : host.getMessageCollection()) {
				if (this.reportedMessages.contains(m.getId())) {

					d = host.getLocation().distance(m.getTo().getLocation());
					addDistanceMsg(m.getId(), d, (int)getSimTime());
				}
			}
		}

		if(flag && (int)getSimTime() >= (int)(getEndTime() - 60)) {
			reportDistance();
			flag = false;
		}
	}

	private void addDistanceMsg(String id, double distance, int time) {

		int indice = index;
		
		// Busca o indice	para o id
		for(int a=0; a<this.N_MSG; a++) {
			if(this.msg_index[a] != null) {			
				if(this.msg_index[a].equals(id)) { 
					indice = a;
					a = N_MSG;
				}
			}
		}

		// Nova msg - Adiciona 'id' na lista
		if(indice == index) {
			this.msg_index[indice] = id;
			index++;
		}

		// Adiciona ditância 
		for(int a=0; a<this.N_AMO; a++) {
			if(this.msg_distance[indice][a] == 0) {
				this.msg_distance[indice][a] = distance;
				this.msg_time[indice][a] = time;
				a = N_AMO;
			}
		}	
	}
	 
	private void reportDistance() {
	
		write("Message Distance for scenario " + getScenarioName() + 
				"\nsim_time: " + format(getSimTime()));

		for(int a=0; a<this.N_MSG; a++) {
			String text = "[Message: " + this.msg_index[a] + "]\n";
			for(int b=0; b<this.N_AMO; b++) {
				// if(this.msg_distance[a][b] != 0) { 
					text = text + (int)this.msg_distance[a][b] + "\n";
					// if(b == (N_AMO-1)) { text = text + "Não entregue!"; }
				//}
				//else {
				//	text = text + "Entregue";
				//	b=N_AMO;
				//}
			}
			write(text);		
		}
	}
}
