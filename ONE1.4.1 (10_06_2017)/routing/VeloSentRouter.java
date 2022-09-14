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

import routing.velosent.Algorithm;
import routing.velosent.ConMsg;

/**
 * Implementation of VELOSENT router by Gil Eduardo de Andrade
*/
public class VeloSentRouter extends ActiveRouter {

	/** Context router's setting namespace ({@value})*/ 
	public static final String VELOSENT_NS = "VeloSentRouter";

	/** identifier for the binary-mode setting ({@value})*/ 
	public static final String REPLICATION_MODE = "ReplicationMode";

	/** Algoritmo de roteamento utilizado para determinar próximo nó */
	private	Algorithm objAlg;
	/** Lista de Conexões/Mensagens/Idades  */
	private ConMsg objConMsg;
	/** Indica se o modo de replicação de mensagens está ativo ou não*/
	private boolean isReplication;
	

	public VeloSentRouter(Settings s) {
		super(s);
		Settings VeloSentSettings = new Settings(VELOSENT_NS);
		isReplication = VeloSentSettings.getBoolean(REPLICATION_MODE);
		initVeloSentRouter();
	}

	/**
	 * Copyconstructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected VeloSentRouter(VeloSentRouter r) {
		super(r);
		this.isReplication = r.isReplication;
		initVeloSentRouter();
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

			List<Message> messages = new ArrayList<Message>(this.objConMsg.getListMessage());
			Message m;
			Connection c;

	        	for(int i=0;  i<messages.size(); i++) {
				// Obtém as Mensagens e as suas respectivas Conexões
				m = (Message) messages.get(i);
				c = this.objConMsg.getConnectionMessage(m);

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
	
	@Override
	public MessageRouter replicate() {
		VeloSentRouter r = new VeloSentRouter(this);
		return r;
	}

	// Cria a Tabela de Contexto dos Últimos Contatos Efetuados
	public void initVeloSentRouter() {
		this.objAlg = new Algorithm();
		this.objConMsg = new ConMsg();
	}

	// Atualiza a tabela TCUCE com o novo contato efetuado
	public void updateTCUCE(DTNHost node) {
		// Atualiza a TCUCE do ContactManager com os dados de Contexto do nó que acaba de ser contatado
		getHost().setContactLast(node.getId(), node.getLocalTime(), SimClock.getTime(), 
						node.getPosX(), node.getPosY(), node.getSpeedX(), 
						node.getSpeedY());
	}

	// Atualiza a melhor idade do último contato e os parâmetros de contexto
	public void updateConnectionForMessages(DTNHost node) {

		DTNHost node_to;
		TCUCE objTCUCE;
		double nodeAge; // idade do contato do outro nó com o destino da mensagem
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
		boolean flag = false;
		// Distância final, usada da determinar o próximo nó a receber a mensagem m
		double final_distance = -1;

		// Informa a idade do último contato com o nó de destino da mensagem
		objAlg.setAge(age);
		// Informa os dados de contexto do nó de destino para o algoritmo
		objAlg.setDataDestination(d_tcuce.getPx(), d_tcuce.getPy(), d_tcuce.getSx(), d_tcuce.getSy());
		// Informa os dados de contexto do nó que está de posse da mensagem nesse momento (que está rodando esse código)
		objAlg.setDataNeighbor(node.getPosX(), node.getPosY(), node.getSpeedY(), node.getSpeedY());
		// RODA O ALGORITMO DE CONTEXTO
		// Calcula distância final para o host de posse da mensagem (nó âncora)
		// considera como a melhor inicialmente, até encontrar um vizinho com melhor possibilidade
		double distance = objAlg.algorithmRun();

		// Percorre a lista de conexões - encontra o nó que segue na direção do destino
		if(connections != null) {
                	for(int i=0;  i<connections.size(); i++) {
				// Obtém as Conexões da lista de conexões
				conn = (Connection) connections.get(i);
				// Obtém a referência do outro nó da conexão
				node = conn.getOtherNode(getHost());
				// Informa os dados de contexto do nó vizinho que possui conexão com atual ancora
				objAlg.setDataNeighbor(node.getPosX(), node.getPosY(), node.getSpeedY(), node.getSpeedY());

				// RODAR O ALGORÍTMO DE CONTEXTOobjTCUCE
				final_distance = objAlg.algorithmRun();
				// Vizinho possui melhor possibilidade de entrega que o nó atual
				// armazena sua conexão para atualizar a lista de Mensagem/Conexão
				if((final_distance < distance && final_distance != -1) || 
					(final_distance > -1 && distance == -1)) {

					final_conn = conn;
					distance = final_distance; // atualiza a melhor distância
					flag = true;
				}
			}
		}

		// Caso haja uma melhor conexão - atualiza lista de Mensagem/Conexão/Idade
		if(flag) {
			// Atualiza a melhor conexão para a mensagem m
			this.objConMsg.setConnectionMessage(final_conn, m, age, d_tcuce);
		}
	}

	// Atualiza a TCUCE Local
	public void updateTCUCEBestAge(String id, TCUCE tcuce, double age, DTNHost host) {
		// Busca na entrada da TCUCE um contato com
		host.updateTCUCEBestAge(id, tcuce, age);
	}
}


/** FUNCIONAMENTO DO "ContextRouter"
	
	-> Primeira Etapa: 	
	-----------------
	no momento em que há uma mudança em um conexão, mais exatamente quando existe uma 
	nova conexão ou contato entre o nó e um vizinho os seguintes passos são efetuados:

		1) 	"A TCUCE" - Tabela de Contexto dos Últimos Contatos Efetuados é atualizada
			para a nova conexão/contato que é efetuado, recebendo as informações de
			contexto do nó que acaba de ser encontrado.

		2)	A cada novo encontro que acontece, a TCUCE desse novo nó é consultada para 
			verificar se o mesmo possui uma melhor estimativa para os destinos de cada
			mensagem armazenada na lista de mensagens do nó âncora

		3)	Essa verificação é feita através do cálculo da idade do contato do nó
			vizinho com o destino da mensagem e comparada com a melhor idade para essa
			mensagem que está armazenada no objeto "ConMsg", sendo menor ou caso 
			a mensagem ainda não tenha uma idade (conexão de entrega) o algoritmo de
			roteamento é executado para escolher a melhor conexão/vizinho (aquele que
 			vai no sentido do destino e com maior velocidade) e assim definir a melhor 
			conexão/vizinho	para receber essa mensagem.

		4)	Durante a busca pela melhor conexão/vizinho o nó âncora considera os seus 
			próprios dados de contexto, visto que não haverá necessidade de replicar
			ou transmitir a mensagem caso o nó âncora tenha melhor oportunidade de
			entrega do que seus vizinhos. A tabela TCUCE local é atualizada com a
			melhor estimativa do nó vizinho (menor idade de contato) ou é adicionada
 			desse contato para que no futuro essa informação possa ser utilizada para
 			rotear outras mensagens.
			
		5)	Após alimentar o Algoritmo com os dados de contexto do nó de destino
			obtidos da nova conexão/contato que foi efetuada, a lista de conexões que 
			o nó âncora possui é varrida, e para cada nó vizinho dessa conexão são
			obtidos seus dados de contexto para que alimentando o Algoritmo seja
			possível efetuar os cálculos para ele.

		6)	Ao término dos cálculo de cada conexão/vizinho uma comparação entre a
			distância do nó âncora em relação ao destino no momento do encontro entre 
			eles e a distância do vizinho no momento do seu encontro com o destino.
			Tendo o vizinho um valor menor, ele será escolhido como a nova melhor 
			conexão para entrega da mensagem em questão.
			
		7) 	Quando termina de percorrer todas conexões procurando o melhor vizinho
			para entregar a mensagem para seu destino final, a lista de 
			Mensagens/Conexões/Idades é atualizada, deixando marcada a conexão que 
			deve ser utilizada para transmissão da mensagem no momento que o método
			"update()" invocado chamado.

		8)	Quando o método "update()" é invocado ele consulta a lista de mensagens e
			suas conexões/vizinhos de entrega e efetua a transmissão destas mensagens,
 			ou seja, escolhe o novo nó âncora. Caso alguma mensagem não possua uma 
			conexão/vizinho na lista a mensagem não é replicada/roteada. 
*/
