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
public class BufferOccupancyReport extends Report implements MessageListener {

	public static String HEADER="# [Time]\t\t\t[Buffer Occupancy]";
	private double occupancy;

	/**
	 * Constructor.
	 */
	public BufferOccupancyReport() {
		init();
	}
	
	@Override
	public void init() {
		super.init();
		this.occupancy = 0;
		write(HEADER);
	}

	public void messageTransferred(Message m, DTNHost from, DTNHost to, boolean firstDelivery) {
		
		if (firstDelivery && !isWarmup() && !isWarmupID(m.getId())) {
		
			// Calcula Ocupação do Buffer
			calcBufferOccupancy();
			reportValues();
		}
	}

	public void newMessage(Message m) {
		
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}

		// Calcula Ocupação do Buffer
		calcBufferOccupancy();
		reportValues();
	}

	/**
	 * Calcula a Taxa (Média) de Ocupação do Buffer dos Nós
	 */
	private void calcBufferOccupancy() {

		List<DTNHost> hosts = getScenario().getHosts();
		int total_hosts = hosts.size();

		this.occupancy = 0;
		for (int a=0; a < total_hosts; a++) {
			this.occupancy = this.occupancy + hosts.get(a).getBufferOccupancy();
		}
	
		this.occupancy = this.occupancy / total_hosts;
	}	

	
	/**
	 * Writes the current values to report file
	 */
	private void reportValues() {
		write(format(getSimTime()) + "\t\t\t" + this.occupancy);
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

