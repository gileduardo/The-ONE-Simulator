/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import movement.MovementModel;
import movement.Path;
import routing.MessageRouter;
import routing.RoutingInfo;

import core.velosent.TCUCE;
import core.velosent.ContactManager;

import core.greg.Contact;
import core.greg.ContactBoss;

/**
 * A DTN capable host.
 */
public class DTNHost implements Comparable<DTNHost> {

	private static int nextAddress = 0;
	private int address;

	private Coord location; 	// where is the host
	private Coord destination;	// where is it going

	private MessageRouter router;
	private MovementModel movement;
	private Path path;
	private double speed;
	private double nextTimeToMove;
	private String name;
	private List<MessageListener> msgListeners;
	private List<MovementListener> movListeners;
	private List<NetworkInterface> net;
	private ModuleCommunicationBus comBus;

	// Gil Eduardo de Andrade - GEA (ContactManager/ContactBoss - Gerenciadores de Contatos)
	private ContactManager objCM;
	private ContactBoss objCB;
	private double x_ant=0, y_ant=0;
	private double Vx=0, Vy=0;		
	private double untilTime=0;

	static {
		DTNSim.registerForReset(DTNHost.class.getCanonicalName());
		reset();
	}
	/**
	 * Creates a new DTNHost.
	 * @param msgLs Message listeners
	 * @param movLs Movement listeners
	 * @param groupId GroupID of this host
	 * @param interf List of NetworkInterfaces for the class
	 * @param comBus Module communication bus object
	 * @param mmProto Prototype of the movement model of this host
	 * @param mRouterProto Prototype of the message router of this host
	 */
	public DTNHost(List<MessageListener> msgLs,
			List<MovementListener> movLs,
			String groupId, List<NetworkInterface> interf,
			ModuleCommunicationBus comBus, 
			MovementModel mmProto, MessageRouter mRouterProto) {
		this.comBus = comBus;
		this.location = new Coord(0,0);
		this.address = getNextAddress();
		this.name = groupId+address;
		this.net = new ArrayList<NetworkInterface>();

		for (NetworkInterface i : interf) {
			NetworkInterface ni = i.replicate();
			ni.setHost(this);
			net.add(ni);
		}	

		// TODO - think about the names of the interfaces and the nodes
		//this.name = groupId + ((NetworkInterface)net.get(1)).getAddress();

		this.msgListeners = msgLs;
		this.movListeners = movLs;

		// create instances by replicating the prototypes
		this.movement = mmProto.replicate();
		this.movement.setComBus(comBus);
		setRouter(mRouterProto.replicate());

		this.location = movement.getInitialLocation();

		this.nextTimeToMove = movement.nextPathAvailable();
		this.path = null;

		if (movLs != null) { // inform movement listeners about the location
			for (MovementListener l : movLs) {
				l.initialLocation(this, this.location);
			}
		}

		// Gerenciadores Tabela de Contatos
		this.objCM = new ContactManager();
		this.objCB = new ContactBoss();
	}
	
	/**
	 * Returns a new network interface address and increments the address for
	 * subsequent calls.
	 * @return The next address.
	 */
	private synchronized static int getNextAddress() {
		return nextAddress++;	
	}

	/**
	 * Reset the host and its interfaces
	 */
	public static void reset() {
		nextAddress = 0;
	}

	/**
	 * Returns true if this node is active (false if not)
	 * @return true if this node is active (false if not)
	 */
	public boolean isActive() {
		return this.movement.isActive();
	}

	/**
	 * Set a router for this host
	 * @param router The router to set
	 */
	private void setRouter(MessageRouter router) {
		router.init(this, msgListeners);
		this.router = router;
	}

	/**
	 * Returns the router of this host
	 * @return the router of this host
	 */
	public MessageRouter getRouter() {
		return this.router;
	}

	/**
	 * Returns the network-layer address of this host.
	 */
	public int getAddress() {
		return this.address;
	}
	
	/**
	 * Returns this hosts's ModuleCommunicationBus
	 * @return this hosts's ModuleCommunicationBus
	 */
	public ModuleCommunicationBus getComBus() {
		return this.comBus;
	}
	
    /**
	 * Informs the router of this host about state change in a connection
	 * object.
	 * @param con  The connection object whose state changed
	 */
	public void connectionUp(Connection con) {
		this.router.changedConnection(con);
	}

	public void connectionDown(Connection con) {
		this.router.changedConnection(con);
	}

	/**
	 * Returns a copy of the list of connections this host has with other hosts
	 * @return a copy of the list of connections this host has with other hosts
	 */
	public List<Connection> getConnections() {
		List<Connection> lc = new ArrayList<Connection>();

		for (NetworkInterface i : net) {
			lc.addAll(i.getConnections());
		}

		return lc;
	}

	/**
	 * Returns the current location of this host. 
	 * @return The location
	 */
	public Coord getLocation() {
		return this.location;
	}

	public Coord getDestination() {
		return this.destination;
	}

	public double getSpeed() {
		return this.speed;
	}

	/**
	 * Returns the Path this node is currently traveling or null if no
	 * path is in use at the moment.
	 * @return The path this node is traveling
	 */
	public Path getPath() {
		return this.path;
	}


	/**
	 * Sets the Node's location overriding any location set by movement model
	 * @param location The location to set
	 */
	public void setLocation(Coord location) {
		this.location = location.clone();
	}

	/**
	 * Sets the Node's name overriding the default name (groupId + netAddress)
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the messages in a collection.
	 * @return Messages in a collection
	 */
	public Collection<Message> getMessageCollection() {
		return this.router.getMessageCollection();
	}

	/**
	 * Returns the number of messages this node is carrying.
	 * @return How many messages the node is carrying currently.
	 */
	public int getNrofMessages() {
		return this.router.getNrofMessages();
	}

	/**
	 * Returns the buffer occupancy percentage. Occupancy is 0 for empty
	 * buffer but can be over 100 if a created message is bigger than buffer 
	 * space that could be freed.
	 * @return Buffer occupancy percentage
	 */
	public double getBufferOccupancy() {
		double bSize = router.getBufferSize();
		double freeBuffer = router.getFreeBufferSize();
		return 100*((bSize-freeBuffer)/bSize);
	}

	/**
	 * Returns routing info of this host's router.
	 * @return The routing info.
	 */
	public RoutingInfo getRoutingInfo() {
		return this.router.getRoutingInfo();
	}

	/**
	 * Returns the interface objects of the node
	 */
	public List<NetworkInterface> getInterfaces() {
		return net;
	}

	/**
	 * Find the network interface based on the index
	 */
	protected NetworkInterface getInterface(int interfaceNo) {
		NetworkInterface ni = null;
		try {
			ni = net.get(interfaceNo-1);
		} catch (IndexOutOfBoundsException ex) {
			System.out.println("No such interface: "+interfaceNo);
			System.exit(0);
		}
		return ni;
	}

	/**
	 * Find the network interface based on the interfacetype
	 */
	protected NetworkInterface getInterface(String interfacetype) {
		for (NetworkInterface ni : net) {
			if (ni.getInterfaceType().equals(interfacetype)) {
				return ni;
			}
		}
		return null;	
	}

	/**
	 * Force a connection event
	 */
	public void forceConnection(DTNHost anotherHost, String interfaceId, 
			boolean up) {
		NetworkInterface ni;
		NetworkInterface no;

		if (interfaceId != null) {
			ni = getInterface(interfaceId);
			no = anotherHost.getInterface(interfaceId);

			assert (ni != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
			assert (no != null) : "Tried to use a nonexisting interfacetype "+interfaceId;
		} else {
			ni = getInterface(1);
			no = anotherHost.getInterface(1);
			
			assert (ni.getInterfaceType().equals(no.getInterfaceType())) : 
				"Interface types do not match.  Please specify interface type explicitly";
		}
		
		if (up) {
			ni.createConnection(no);
		} else {
			ni.destroyConnection(no);
		}
	}

	/**
	 * for tests only --- do not use!!!
	 */
	public void connect(DTNHost h) {
		System.err.println(
				"WARNING: using deprecated DTNHost.connect(DTNHost)" +
		"\n Use DTNHost.forceConnection(DTNHost,null,true) instead");
		forceConnection(h,null,true);
	}

	/**
	 * Updates node's network layer and router.
	 * @param simulateConnections Should network layer be updated too
	 */
	public void update(boolean simulateConnections) {
		if (!isActive()) {
			return;
		}
		
		if (simulateConnections) {
			for (NetworkInterface i : net) {
				i.update();
			}
		}
		this.router.update();
	}

	/**
	 * Moves the node towards the next waypoint or waits if it is
	 * not time to move yet
	 * @param timeIncrement How long time the node moves
	 */
	public void move(double timeIncrement) {		

		double possibleMovement;
		double distance;
		double dx, dy;

		// moveDebug();

		if (!isActive() || SimClock.getTime() < this.nextTimeToMove) {
			return; 
		}
		if (this.destination == null) {
			if (!setNextWaypoint()) {
				return;
			}
		}

		possibleMovement = timeIncrement * speed;
		distance = this.location.distance(this.destination);

		while (possibleMovement >= distance) {
			// node can move past its next destination
			this.location.setLocation(this.destination); // snap to destination
			possibleMovement -= distance;
			if (!setNextWaypoint()) { // get a new waypoint
				return; // no more waypoints left
			}
			distance = this.location.distance(this.destination);
		}

		// move towards the point for possibleMovement amount
		dx = (possibleMovement/distance) * (this.destination.getX() -
				this.location.getX());
		dy = (possibleMovement/distance) * (this.destination.getY() -
				this.location.getY());

		/* GIL EDUARDO */
		// Velocidade do Host
		this.Vx = (10 * dx); this.Vy = (10 * dy); 
		// Tempo até Ponto Destino
		this.untilTime = ( Math.abs(this.destination.getX() - this.location.getX()) / this.Vx);
		/* GIL EDUARDO */

		this.location.translate(dx, dy);
	}	

	/**
	 * Sets the next destination and speed to correspond the next waypoint
	 * on the path.
	 * @return True if there was a next waypoint to set, false if node still
	 * should wait
	 */
	private boolean setNextWaypoint() {
		if (path == null) {
			path = movement.getPath();
		}

		if (path == null || !path.hasNext()) {
			this.nextTimeToMove = movement.nextPathAvailable();
			this.path = null;
			return false;
		}

		this.destination = path.getNextWaypoint();
		this.speed = path.getSpeed();

		if (this.movListeners != null) {
			for (MovementListener l : this.movListeners) {
				l.newDestination(this, this.destination, this.speed);
			}
		}

		return true;
	}

	/**
	 * Sends a message from this host to another host
	 * @param id Identifier of the message
	 * @param to Host the message should be sent to
	 */
	public void sendMessage(String id, DTNHost to) {
		this.router.sendMessage(id, to);
	}

	/**
	 * Start receiving a message from another host
	 * @param m The message
	 * @param from Who the message is from
	 * @return The value returned by 
	 * {@link MessageRouter#receiveMessage(Message, DTNHost)}
	 */
	public int receiveMessage(Message m, DTNHost from) {
		int retVal = this.router.receiveMessage(m, from); 

		if (retVal == MessageRouter.RCV_OK) {
			m.addNodeOnPath(this);	// add this node on the messages path
		}

		return retVal;	
	}

	/**
	 * Requests for deliverable message from this host to be sent trough a
	 * connection.
	 * @param con The connection to send the messages trough
	 * @return True if this host started a transfer, false if not
	 */
	public boolean requestDeliverableMessages(Connection con) {
		return this.router.requestDeliverableMessages(con);
	}

	/**
	 * Informs the host that a message was successfully transferred.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 */
	public void messageTransferred(String id, DTNHost from) {
		this.router.messageTransferred(id, from);	
	}

	/**
	 * Informs the host that a message transfer was aborted.
	 * @param id Identifier of the message
	 * @param from From who the message was from
	 * @param bytesRemaining Nrof bytes that were left before the transfer
	 * would have been ready; or -1 if the number of bytes is not known
	 */
	public void messageAborted(String id, DTNHost from, int bytesRemaining) {
		this.router.messageAborted(id, from, bytesRemaining);
	}

	/**
	 * Creates a new message to this host's router
	 * @param m The message to create
	 */
	public void createNewMessage(Message m) {
		this.router.createNewMessage(m);
	}

	/**
	 * Deletes a message from this host
	 * @param id Identifier of the message
	 * @param drop True if the message is deleted because of "dropping"
	 * (e.g. buffer is full) or false if it was deleted for some other reason
	 * (e.g. the message got delivered to final destination). This effects the
	 * way the removing is reported to the message listeners.
	 */
	public void deleteMessage(String id, boolean drop) {
		this.router.deleteMessage(id, drop);
	}

	/**
	 * Returns a string presentation of the host.
	 * @return Host's name
	 */
	public String toString() {
		return name;
	}

	/**
	 * Checks if a host is the same as this host by comparing the object
	 * reference
	 * @param otherHost The other host
	 * @return True if the hosts objects are the same object
	 */
	public boolean equals(DTNHost otherHost) {
		return this == otherHost;
	}

	/**
	 * Compares two DTNHosts by their addresses.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(DTNHost h) {
		return this.getAddress() - h.getAddress();
	}

	// ================================================= //
	// ===== MÉTODOS PARA OBTER O CONTEXTO DO HOST ===== //
	// ================================================= //

	// Retorna o "ID" do HostDTN
	public String getId() { 
		return this.name;
	} 	// GEA - Gil Eduardo de Andrade

	// Retorna o "tempo atual" do HostDTN
	public double getLocalTime() {
		return SimClock.getTime();
	}	// GEA - Gil Eduardo de Andrade

	// Retorna a "posição X" do HostDTN
	public double getPosX() { 
		return this.location.getX();
	} 	// GEA - Gil Eduardo de Andrade

	// Retorna a "posição Y" do HostDTN
	public double getPosY() { 
		return this.location.getY();
	} 	// GEA - Gil Eduardo de Andrade

	// Retorna a "velocidade X" do HostDTN
	public double getSpeedX() { 

		double adjust;
		double speedX = this.speed;

		if(this.location != null && this.destination != null) { 
			
			adjust = getAdjust("x");
			speedX = speedX * adjust;

			if(this.location.getX() > this.destination.getX()) {
				speedX = (speedX * -1);
			}
		}

		// System.out.println("Velocidade(x) = " + speedX);
		return speedX;
	} 	// GEA - Gil Eduardo de Andrade

	// Retorna a "velocidade Y" do HostDTN
	public double getSpeedY() { 

		double adjust;
		double speedY = this.speed;

		if(this.location != null && this.destination != null) { 

			adjust = getAdjust("y");
			speedY = speedY * adjust;

			if(this.location.getY() > this.destination.getY()) {
				speedY = (speedY * -1);
			}
			
		}

		// System.out.println("Velocidade(y) = " + speedY);
		return speedY;
	} 	// GEA - Gil Eduardo de Andrade

	// Retorna o "Alcance da Antena" do HostDTN
	public double getRange() { 
		return this.net.get(0).getTransmitRange();
	} 	// GEA - Gil Eduardo de Andrade

	// Retorna a "Taxa de Transmissão" do HostDTN
	public int getTransmitSpeed() { 
		return this.net.get(0).getTransmitSpeed();
	}


	// Retorna o "Ajuste" utilizado acertar velocidade
	public double getAdjust(String coord) { 

		double adjust = 1;
		double dx = Math.abs(this.location.getX() - this.destination.getX());
		double dy = Math.abs(this.location.getY() - this.destination.getY());

		// Coordenada 'X'
		if(coord.equals("x")) {
			if(dy > dx) {
				adjust = dx/dy;
			}
		}
		// Coordenada 'Y'
		else if (coord.equals("y")){
			if(dx > dy) {
				adjust = dy/dx;
			}
		}

		return adjust;
	}

	// ========================================================================== //
	// ===== MÉTODOS PARA OBTER A TABELA DE CONTEXTO DOS CONTATOS EFETUADOS ===== //
	// ========================================================================== //

	// Atualiza a Tabela de contexto dos últimos contatos efetuados
	public void setContactLast(String id, double time, double localtime, double px, double py, double sx, double sy) {
		this.objCM.setContactLast(id, time, localtime, px, py, sx, sy);
	}	// GEA - Gil Eduardo de Andrade	

	// Obtém os dados de contexto do último contato para um determinado nó de destino
	public TCUCE getContactLastID(String id) {
		return this.objCM.getContactLastID(id);
	}	// GEA - Gil Eduardo de Andrade

	// Atualiza a TCUCE Local para uma melhor idade de contato
	public void updateTCUCEBestAge(String id, TCUCE tcuce, double age) {
		this.objCM.updateTCUCEBestAge(id, tcuce, (SimClock.getTime() - age));
	}	// GEA - Gil Eduardo de Andrade

	// ================================================================================= //
	// ===== MÉTODOS PARA OBTER A TABELA DE CONTEXTO DOS CONTATOS EFETUADOS - GREG ===== //
	// ================================================================================= //

	// Indica se o Nó está Ativo (possui velocidade, caminho, etc)
	public boolean getNoAtivo() {
		
		if(!isActive() || (SimClock.getTime() < this.nextTimeToMove) || this.destination == null) {
			return false;
		}
	
		return true;
	}	

	// Posição Atual - X
	public double getPx() { return this.location.getX(); }
	// Posição Atual - Y
	public double getPy() { return this.location.getX(); }
	// Cruzamento Atual - Posição Atual
	public Coord getCruzamentoAtual() { return this.location; }
	// Próximo Cruzamento - Posição Futura
	public Coord getProximoCruzamento() { return this.destination; }
	// Tempo até o Próximo Cruzamento
	public double getTempoProximoCruzamento() { return this.untilTime; }
	// Velocidade do Host em X
	public double getVelocidadeX() { return this.Vx; }
	// Velocidade do Host em Y
	public double getVelocidadeY() { return this.Vy; }
	// Tempo Necessário para Alcançar o Próximo Cruzamento
	public double getUntilTime() { return this.untilTime; }
	
	// Obtém os Dados do Contato Efetuado com o Nó "id" (Destino)
	public Contact getDataContact(String id) { return this.objCB.getContactLastID(id); }

	// Novo Contato - Adiciona a Tabela
	public void setDataContact(String id, double time, double px, double py, double sx, double sy) {
		this.objCB.setContact(id, time, px, py, sx, sy);
	}

	// Nós Trocam suas Tabelas de Contatos
	public void trocaContatos(ArrayList<Contact> tb) { this.objCB.trocaTabelaContato(tb, this.name); }

	// Atualiza Informação como já utilizada
	public void setNewContact(String ID, int nc) { this.objCB.setNewContact(ID, nc); }
	// Obtém Informação sobre novo contato
	public int getNewContact(String ID) { return this.objCB.getNewContact(ID); }

	// Obtém a Tabela de Contatos
	public ArrayList<Contact> getContatos() { return this.objCB.getTabelaContatos(); }

	public void trocaTabDebug() {
		// Debug
		if(this.name.equals("c121") && this.path != null) { this.objCB.printContactTable(); }
		else { System.out.println("Path -> [NULL]"); }
	}

	public void moveDebug() {

		double dx, dy;

		if(this.name.equals("c121") && this.path != null) {
			
			if(this.location != null && this.destination != null) {

				System.out.println("[" + SimClock.getTime() + "] - [" + this.untilTime + "]");
				System.out.println("Orig(" + this.location.getX() + ", " + this.location.getY() + ")");
				System.out.println("Dest(" + this.destination.getX() + ", " + this.destination.getY() + ")");

				if(this.x_ant != 0 && this.y_ant != 0) {

					dx = Math.abs(this.x_ant - this.location.getX());
					dy = Math.abs(this.y_ant - this.location.getY());
					System.out.println("Diff(" + this.Vx + ", " + this.Vy + ")");
				}

				this.x_ant =  this.location.getX();
				this.y_ant = this.location.getY();
			}
			// this.objCB.printContactTable();
		}
	}

	// ========================================================================== //
	// ===== MÉTODOS PARA O PROTOCOLO RAPID - NÃO FOI DESENVOLVIDO POR MIM (Gil   //
	// ===== Eduardo), MAS ESTAVA DISPONÍVEL NA PÁGINA DO SIMULADOR, IMPLEMENTADO //
	// ===== POR  Wolfgang Heetfeld E Christoph P. Mayer	                      //
	// ========================================================================== //	

	public MovementModel getMovementModel() {
		return this.movement;
	}
}
