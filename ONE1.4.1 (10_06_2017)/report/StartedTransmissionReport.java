/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.List;

import core.DTNHost;
import core.Message;
import core.MessageListener;

/**
 * Report for of amount of messages delivered vs. time. A new report line
 * is created every time when either a message is created or delivered.
 * Messages created during the warm up period are ignored.
 * For output syntax, see {@link #HEADER}.
 */
public class StartedTransmissionReport extends Report implements MessageListener {

	public static String HEADER="# [Time]\t\t\t[Number of Started Transmission]";
	private double occupancy;
	private double nr_transmission;

	/**
	 * Constructor.
	 */
	public StartedTransmissionReport() {
		init();
	}
	
	@Override
	public void init() {
		super.init();
		this.nr_transmission = 0;
		write(HEADER);
	}

	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {

		if (isWarmupID(m.getId())) {
			return;
		}

		// Nova Transferência de Mensagem Iniciada
		this.nr_transmission++;
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		
		if (firstDelivery && !isWarmup() && !isWarmupID(m.getId())) {
			reportValues();
		}
	}

	public void newMessage(Message m) {
		
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}

		reportValues();
	}

	/**
	 * Writes the current values to report file
	 */
	private void reportValues() {
		write(format(getSimTime()) + "\t\t\t" + this.nr_transmission);
	}

	// nothing to implement for the rest
	public void messageDeleted(Message m, DTNHost where, boolean dropped) {}
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {}


	@Override
	public void done() {
		super.done();
	}
}

