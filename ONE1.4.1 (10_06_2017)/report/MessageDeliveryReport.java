/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * Report for of amount of messages delivered vs. time. A new report line
 * is created every time when either a message is created or delivered.
 * Messages created during the warm up period are ignored.
 * For output syntax, see {@link #HEADER}.
 */
public class MessageDeliveryReport extends Report implements MessageListener {
	public static String HEADER="# time\t\tcreated  delivered  delivered/created";
	private int created;
	private int delivered;
	private int times[] = new int[6]; 

	/**
	 * Constructor.
	 */
	public MessageDeliveryReport() {
		init();
	}
	
	@Override
	public void init() {
		super.init();
		created = 0;
		delivered = 0;
		for(int a=0; a<6; a++) { this.times[a] = 0; }
		write(HEADER);
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to, 
			boolean firstDelivery) {
		if (firstDelivery && !isWarmup() && !isWarmupID(m.getId())) {
			delivered++;
			reportValues();
		}
	}

	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}
		created++;
		reportValues();
	}
	
	/**
	 * Writes the current values to report file
	 */
	private void reportValues() {

		boolean flag = false;
		double prob = (1.0 * delivered) / created;

		if(getSimTime() > 850 && getSimTime() < 950 && this.times[0] == 0) {
			this.times[0] = 1; flag = true; 	// 15 minutos
		}
		else if(getSimTime() > 1750 && getSimTime() < 1850 && this.times[1] == 0) {
			this.times[1] = 1; flag = true;	// 30 minutos
		}
		else if(getSimTime() > 3550 && getSimTime() < 3650 && this.times[2] == 0) {
			this.times[2] = 1; flag = true;	// 1 hora
		}
		else if(getSimTime() > 7150 && getSimTime() < 7250 && this.times[3] == 0) {
			this.times[3] = 1; flag = true;	// 2 horas
		}
		else if(getSimTime() > 14350 && getSimTime() < 14450 && this.times[4] == 0) {
			this.times[4] = 1; flag = true;	// 4 horas
		}
		else if(getSimTime() > 21500 && this.times[5] == 0) {
			this.times[5] = 1; flag = true;	// 6 horas
		}

		if(flag == true) {
			write(format(getSimTime()) + "\t\t" + created + "\t\t" + delivered + 
				"\t\t" + format(prob));
		}
	}

	// nothing to implement for the rest
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {}

	@Override
	public void done() {
		super.done();
	}
}
