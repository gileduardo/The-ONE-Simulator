/* 
 * Copyright 2008 TKK/ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.Tuple;
import core.velosent.ContactManager;
import core.velosent.TCUCE;

import routing.rpd.Algorithm;
import routing.rpd.ConMsg;

/**
 * Implementation of RPD router by Gil Eduardo de Andrade
*/
public class RPDRouter extends ActiveRouter {

	/** Context router's setting namespace ({@value})*/ 
	public static final String RPD_NS = "RPDRouter";

	/** identifier for the binary-mode setting ({@value})*/ 
	public static final String REPLICATION_MODE = "ReplicationMode";

	/** Algoritmo de roteamento utilizado para determinar próximo nó */
	private Algorithm objAlg;
	/** Lista de Conexões/Mensagens/Idades  */
	private ConMsg objConMsg;
	/** Indica se o modo de replicação de mensagens está ativo ou não*/
	private boolean useReplication;
	

	public RPDRouter (Settings s) {
		super(s);
		Settings RPDSettings = new Settings(RPD_NS);
		useReplication = RPDSettings.getBoolean(REPLICATION_MODE);
		System.out.println("[RPD] - Modo de Replicação: " + useReplication);
		initRPDRouter();
	}

	/**
	 * Copyconstructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected RPDRouter(RPDRouter r) {
		super(r);
		this.useReplication = r.useReplication;
		initRPDRouter();
	}
	
	@Override // Mudança em uma conexão
	public void changedConnection(Connection con) {

		// Nova Conexão - Contato
		if (con.isUp()) {
			// Obtém a referência do outro nó com quem acabou de fazer contato/conexão
			DTNHost otherHost = con.getOtherNode(getHost());
			// Atualiza a Tabela de Contexto dos Contatos com esse último contato efetuado
			updateTCUCE(otherHost);
			// Atualiza as melhores conexões com vizinhos para cada mensagem da lista
			updateConnectionForMessages(otherHost);
		}
		// Perdeu uma Conexão - Contato
		else {
		}
	}
	
	@Override
	public void update() {

		super.update();
		if (!canStartTransfer() ||isTransferring()) {
			return; // nothing to transfer or is currently transferring 
		}
		
		// try messages that could be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return;
		}

		// Efetua a Leitura da lista de mensagens/conexões e efetua a transmissão
		if(this.objConMsg.getListMessage() != null) {

			// List<Message> messages = new ArrayList<Message>(this.objConMsg.getListMessage());
			List<Message> messages = new ArrayList<Message>(this.getMessageCollection());
			Message m;
			Connection c;

	        	for(int i=0;  i<messages.size(); i++) {
				// Obtém as Mensagens e as suas respectivas Conexões
				m = (Message) messages.get(i);
				c = this.objConMsg.getConnectionMessage(m);

				// Caso haja uma Conexão para a Mensagem
				if(c != null) {
					// Mensagem enviada com sucesso para sua respectiva conexão
					if(tryMessagesForYourConnection(m, c) == 1) {
						double age = this.objConMsg.getAgeMessage(m);
						TCUCE best = this.objConMsg.getTcuceMessage(m);

						// Atualiza a TCUCE do destino com a melhor estimativa encontrada
						if(age != -1 && best != null)
							updateTCUCEBestAge(m.getTo().getId(), best, age, c.getOtherNode(getHost()));
					
					}
					// Não foi possível enviar a mensagem para sua respectiva conexão
					else {
						// Verificar a conexão utilizada para enviar a mensagem
						// setar com "null" talvez
					}
				}
			}
		}
	}
	
	@Override
	public MessageRouter replicate() {
		return new RPDRouter(this);
	}

	// Cria a Tabela de Contexto dos Últimos Contatos Efetuados
	public void initRPDRouter() {
		this.objAlg = new Algorithm();
		this.objConMsg = new ConMsg();
	}

	// Atualiza a tabela TCUCE com o novo contato efetuado
	public void updateTCUCE(DTNHost otherNode) {
		// Atualiza a TCUCE do ContactManager com os dados de Contexto do nó que acaba de ser contatado
		getHost().setContactLast(otherNode.getId(), otherNode.getLocalTime(), SimClock.getTime(), 
						otherNode.getPosX(), otherNode.getPosY(), otherNode.getSpeedX(), 
						otherNode.getSpeedY());
	}

	// Atualiza a melhor idade do último contato e os parâmetros de contexto
	public void updateConnectionForMessages(DTNHost node) {

		DTNHost node_to;
		TCUCE objTCUCE;
		double nodeAge; 	// idade do contato do outro nó com o destino da mensagem
		double Age; 	// idade da melhor conexão atual para esta mensagem

		// Obtém a lista de Mensagens
		List<Message> messages = new ArrayList<Message>(this.getMessageCollection());
		Message m;
	
		// Percorre a lista de mensagens - encontra se há uma conexão melhor para cada mensagem
		if(messages != null) {

                	for(int i=0;  i<messages.size(); i++) {
				// Obtém as Mensagens da lista de mensagens
				m = (Message) messages.get(i);
				// Obtém referência do nó de destino da mensagem
				node_to = m.getTo();
				// Obtém dados de contexto do nó de destino da mensagem no seu útimo contato efetuado
				objTCUCE = node.getContactLastID(node_to.getId());

				// Verifica se houve contato do vizinho com o nó de destino da mensagem
				if(objTCUCE != null) {
					// Calcula a idade do contato com o destino
					nodeAge = node.getLocalTime() - objTCUCE.getLocalTime();
					// Obtém a idade da lista de idades para essa mensagem
					Age = this.objConMsg.getAgeMessage(m);
					
					// PRIMEIRA FASE DO PROTOCOLO
					// Idade é menor - possui uma melhor estimativa do destino - atualiza os parâmetros
					if(nodeAge < Age || Age == -1) {	
						// Atualiza a TCUCE local com a melhor idade do vizinho
						updateTCUCEBestAge(node_to.getId(), objTCUCE, nodeAge, getHost());

						// Atualiza a lista de mensagens que devem ser enviadas para as 
						// melhores conexões (nós que se movem em direção ao destino)
						updateMessagesConnections(m, nodeAge, objTCUCE);
					}
					else {
					}
				}
			}
		}
	}

	// Recalcula a melhor conexão para as mensagens que possuem esse mesmo destino
	public void updateMessagesConnections(Message m, double age, TCUCE d_tcuce) {
		
		// Obtém a lista de Conexões
		List<Connection> connections = new ArrayList<Connection>(this.getConnections());
		// Variáveis auxiliares
		Connection conn, final_conn = null;
		DTNHost node = getHost();

		// Informa a idade do último contato com o nó de destino da mensagem
		objAlg.setAge(age);
		// Informa os dados de contexto do nó de destino para o algoritmo
		objAlg.setDataDestination(d_tcuce.getPx(), d_tcuce.getPy(), d_tcuce.getSx(), d_tcuce.getSy());
		// Informa os dados de contexto do nó que está de posse da mensagem nesse momento (que está rodando esse código)
		objAlg.setDataNeighbor(node.getPosX(), node.getPosY(), node.getSpeedY(), node.getSpeedY());

		// RODA O ALGORITMO DE PREDIÇÃO DA LOCALIZAÇÃO DO DESTINO
		// Percorre a lista de conexões - Cálcula o Fator de Aproximação
		// Encontra o nó que segue na direção do destino
		if(connections != null) {
                	for(int i=0;  i<connections.size(); i++) {
				// Obtém as Conexões da lista de conexões
				conn = (Connection) connections.get(i);
				// Obtém a referência do outro nó da conexão
				node = conn.getOtherNode(getHost());
				// Informa os dados de contexto do nó vizinho que possui conexão com atual ancora
				objAlg.setDataNeighbor(node.getPosX(), node.getPosY(), node.getSpeedX(), node.getSpeedY());

				// Verifica o fator de aproximação - se aproximando do destino
				if(objAlg.getFatorAprox() < 0) {			
					// Atualiza a melhor conexão para a mensagem m
					this.objConMsg.setConnectionMessage(conn, m, age, d_tcuce);
				}

			}
		}
	}

	// Atualiza a TCUCE Local
	public void updateTCUCEBestAge(String id, TCUCE tcuce, double age, DTNHost host) {
		// Busca na entrada da TCUCE um contato com
		host.updateTCUCEBestAge(id, tcuce, age);
	}

	@Override
	public void transferDone(Connection con) {

		// Mensagem transmitida		
		Message m = con.getMessage();

		// Mensagem entregue ao destino
		if(m.getTo() == con.getOtherNode(getHost())) {
			// Remove Mensagem - Lista RPD
			this.objConMsg.removeMessage(m);
		}

		if (m != null && useReplication == false) {

			// Remove Mensagem - Lista RPD
			this.objConMsg.removeMessage(m);
			// Remove Mensagem - Buffer Local
			deleteMessage(m.getId(), false);

			// System.out.println("[Removendo Mensagem]");
		}
	}
}



