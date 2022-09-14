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
import core.Coord;
import core.greg.ContactBoss;
import core.greg.Contact;
import core.greg.ConnMsgBoss;

import routing.ciente.AlgorithmC;

/**
 * Implementation of Ciente router by Gil Eduardo de Andrade
*/
public class CienteRouter extends ActiveRouter {

	/** Context router's setting namespace ({@value})*/ 
	public static final String CIENTE_NS = "CienteRouter";

	/** Modo replicação ativo ou não*/ 
	public static final String REPLICATION_MODE = "ReplicationMode";
	/** Número de Réplicas*/ 
	public static final String NR_REPLICS = "NrReplics";
	/** Tempo que indica se a informação sobre o destino está obsoleta*/ 
	public static final String TIME_OLD = "TimeOld";
	/** Tempo que indica se a informação é antiga demais para efetuar uma estimativa*/ 
	public static final String TIME_MAX = "TimeMax";
	
	/** Algoritmo de roteamento utilizado para determinar próximo nó */
	private AlgorithmC objAlg;

	/** Lista de Conexões/Mensagens/Idades */
	private ConnMsgBoss objConMsg;

	/** Indica se as mensagens devem ser replicadas ou não */
	private boolean useReplication;

	/** Indica o número máximo de réplicas */
	private int nrReplics;

	/** Indica o tempo utilizado para definir se uma informação está defasada*/
	private double timeOld;

	/** Indica o tempo utilizado para definir se uma estimativa não deve ser feita*/
	private double timeMax;

	public CienteRouter(Settings s) {

		super(s);
		Settings CienteSettings = new Settings(CIENTE_NS);

		this.useReplication = CienteSettings.getBoolean(REPLICATION_MODE);
		System.out.println("[CIENTE] - Modo de Replicação: " + useReplication);
	
		this.nrReplics = CienteSettings.getInt(NR_REPLICS);
		System.out.println("[CIENTE] - Número de Réplicas: " + nrReplics);

		this.timeOld = CienteSettings.getDouble(TIME_OLD);
		System.out.println("[CIENTE] - Tempo Informação Obsoleta: " + timeOld + " segundos");

		this.timeMax = CienteSettings.getDouble(TIME_MAX);
		System.out.println("[CIENTE] - Tempo Máximo para Previsão: " + timeMax + " segundos");

		if(this.useReplication == false) { this.nrReplics = 0; }
		
		initCienteRouter();
	}

	/**
	 * Copyconstructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected CienteRouter(CienteRouter r) {
		super(r);
		this.useReplication = r.useReplication;
		this.nrReplics = r.nrReplics;
		this.timeOld = r.timeOld;
		this.timeMax = r.timeMax;

		if(this.useReplication == false) { this.nrReplics = 0; }

		initCienteRouter();
	}
	
	// Mudança em uma conexão
	@Override 
	public void changedConnection(Connection con) {

		// Nova Conexão - Contato
		if (con.isUp()) {
			// Obtém a referência do outro nó com quem acabou de fazer contato/conexão
			DTNHost otherHost = con.getOtherNode(getHost());

			// Verifica se Nó está Ativo
			if(otherHost.getNoAtivo() == true) { 

				// Atualiza a Tabela: Novo Contato e Troca das Tabelas
				updateTabelaContatos(otherHost);

				// Atualiza as melhores conexões com vizinhos para cada mensagem da lista
				updateConexoesMensagens(otherHost, con);
			}
		}
		// Perdeu uma Conexão - Contato
		else {
			// Atualiza Conexão-Mensagens (Remove conexão)
			this.objConMsg.removeConnection(con);
		}
	}
	
	// Atualiza a tabela de contatos com o novo contato efetuado
	public void updateTabelaContatos(DTNHost otherNode) {

		double time;
		Coord pos;
		// Define o Cruzamento para onde o Nó encontrado está indo
		// Calcula o tempo até alcançar esse cruzamento
		this.objAlg.setProximoCruzamentoNo(otherNode);
		pos = this.objAlg.getNextCross();
		time = SimClock.getTime() + this.objAlg.getTimeNextCross();

		// Atualiza a Tabela de Contatos do ContactBoss com os dados de Contexto do nó que acaba de ser contatado
		getHost().setDataContact(otherNode.getId(), time, pos.getX(), pos.getY(),
						otherNode.getVelocidadeX(), otherNode.getVelocidadeY());

		// Troca das tabelas de Contatos entre os nós
		getHost().trocaContatos(otherNode.getContatos());
	}

	// Atualiza a melhor conexão para transmitir a mesagem
	public void updateConexoesMensagens(DTNHost otherNode, Connection conn) {

		DTNHost node_to;
		Coord cruzamento_destino;
		Contact objContact, objCA;
		double tempo, pex, pey;
		int ret_alg = 0;
		boolean ret;

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
				// Configura/Atualiza o Número de Cópias da Mensagens (Msg Original)
				if(this.getNrCopiesConfig(m) == false) {
					this.setNrCopies(m, this.nrReplics);
					this.setNrCopiesConfig(m, true);
				}

				// Verifica se houve contato do vizinho com o nó de destino da mensagem
				if((getHost().getLocalTime() >= m.getTimeNextHope()) &&
						this.objAlg.getHopsGREG(m) == true) { 

					// Tempo passado sobre a informação do destino (recente ou obsoleto)
					// tempo = getHost().getLocalTime() - objContact.getTime();

					// Cruzamento onde se encontra o destino			
					// cruzamento_destino = new Coord(objContact.getPx(), objContact.getPy());

					// Informação Obsoleta
					// if(tempo > this.timeOld && tempo < this.timeMax) {
						// Verifica se Nó de Destino está Ativo
						// if(node_to.getNoAtivo() == true) { 
							// Estima cruzamento atual do Destino
					cruzamento_destino = this.objAlg.getCruzamentoEstimado(node_to);
					selectProximoSalto(m, cruzamento_destino); 
					// Add informação margem de erro ao relarório
					// this.objAlg.setRelatorioMargemErro(getHost().getLocation(), node_to.getLocation(), 0, cruzamento_destino);

						// }
					//}

					// Identifica a melhor conexão entre os vizinhos
					// if(cruzamento_destino!=null) {
						selectProximoSalto(m, cruzamento_destino); 
					// }
				}
			}
		}
	}

	public void selectProximoSalto(Message m, Coord cruzamento_destino) {
		
		// Obtém a lista de Conexões
		List<Connection> connections = new ArrayList<Connection>(this.getConnections());
		Connection conn, finalConn = null;
		DTNHost otherNode, node = null;
		double distance = -1;
		double velocity = 0;
		double time = 0;

		// Primeiro considera o próprio Âncora para manter a mensagem 'm'
		if(this.objAlg.runStrategy(getHost(), cruzamento_destino) == true) {
			// distance = this.objAlg.getDistance();
			velocity = getHost().getSpeed();
			node = null;
			finalConn = null;
		}

		// Percorre a lista de conexões - Cálcula Menor Distânica Euclidiana
		// Encontra o nó que segue na direção do destino
		if(connections != null) {
                	for(int i=0;  i<connections.size(); i++) {

				// Obtém as Conexões da lista de conexões
				conn = (Connection) connections.get(i);
				// Obtém a referência do outro nó da conexão
				otherNode = conn.getOtherNode(getHost());

				// Verifica se é o Próximo Salto da Mensagem
				if(this.objAlg.runStrategy(otherNode, cruzamento_destino) == true) {
					// Ainda não tem candidato para ser o próximo salto
					if(velocity == 0) {
						node = otherNode;
						finalConn = conn;
						velocity = node.getSpeed();
					} 
					else if(otherNode.getSpeed() > velocity) {
						node = otherNode;
						finalConn = conn;
						velocity = node.getSpeed();
					}
				}
			}

			// Configura Conexão de Envio
			if(finalConn != null && node != null) {
				
				// Configura Tempo de Espera
				time = this.objAlg.getTempoProximoCruzamento(node);
				m.setTimeNextHope(node.getLocalTime() + time); 

				// Configura Conexão para Mensagem
				this.objConMsg.updateConnMsg(finalConn, m);
			}
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
		
		// GREG - Efetua a Leitura da lista de mensagens/conexões e efetua a transmissão
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
					}
					// Não foi possível enviar a mensagem para sua respectiva conexão
					else {
					}
				}
			}
		}
	}
	
	@Override
	public MessageRouter replicate() {
		return new CienteRouter(this);
	}

	// Cria a Tabela de Contexto dos Últimos Contatos Efetuados
	public void initCienteRouter() {
		this.objAlg = new AlgorithmC(this.nrReplics);
		this.objConMsg = new ConnMsgBoss();
	}

	@Override
	public void transferDone(Connection con) {

		// Mensagem transmitida		
		Message m = con.getMessage();
		DTNHost otherNode = con.getOtherNode(getHost());

		// Mensagem entregue ao destino
		if(m.getTo() == con.getOtherNode(getHost())) {
			// Remove Mensagem - Lista Greg
			this.objConMsg.removeMessage(m);
			// Remove Mensagem - Buffer Local
			deleteMessage(m.getId(), false);
		}
		else {
			// COM REPLICAÇÃO
			if (m != null && useReplication == true) {

				// Remove mensagem
				if(this.getNrCopies(m) == 0) {
					// Remove Mensagem - Lista Greg
					this.objConMsg.removeMessage(m);
					// Remove Mensagem - Buffer Local
					deleteMessage(m.getId(), false);
					// System.out.println("[TRANSFERÊNCIA DE CUSTÓDIA]!");
				}
				// Atualiza o número de cópias - Localmente
				else {
					this.setNrCopies(m, this.getNrCopies(m)/2);
					// System.out.println("[REPLICAÇÃO]!");
				}
			}
			// SEM REPLICAÇÃO
			else {
				// Remove Mensagem - Lista Greg
				this.objConMsg.removeMessage(m);
				// Remove Mensagem - Buffer Local
				deleteMessage(m.getId(), false);
				// System.out.println("[TRANSFERÊNCIA DE CUSTÓDIA]!");
			}
		}
	}
}
