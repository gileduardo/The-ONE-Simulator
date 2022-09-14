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
 * Implementation of GREASE router by Gil Eduardo de Andrade
*/
public class GreaseRouter extends ActiveRouter {

	/** Context router's setting namespace ({@value})*/ 
	public static final String GREASE_NS = "GreaseRouter";
	
	/** Lista de Conexões/Mensagens/Idades  */
	private ConMsg objConMsg;

	public GreaseRouter(Settings s) {
		super(s);
		Settings ContextSettings = new Settings(GREASE_NS);
		initGreaseRouter();
	}

	/**
	 * Copyconstructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected GreaseRouter(GreaseRouter r) {
		super(r);
		initGreaseRouter();
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
			updateConnectionForMessages(otherHost, con);
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
					// Talvez deletar a mensagem das listas
				}
				// Não foi possível enviar a mensagem para sua respectiva conexão
				else {
					// Verificar a conexão utiliza para enviar a mensagem
					// setar com "null" talvez
				}
			}
		}
	}
	
	@Override
	public MessageRouter replicate() {
		GreaseRouter r = new GreaseRouter(this);
		return r;
	}

	// Cria a Tabela de Contexto dos Últimos Contatos Efetuados
	public void initGreaseRouter() {
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
	public void updateConnectionForMessages(DTNHost node, Connection con) {

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
				// Obtém dados de contexto do nó de destino da mesagem no seu útimo contato efetuado
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
						// Atualiza a melhor conexão para a mensagem m
						this.objConMsg.setConnectionMessage(con, m, nodeAge, objTCUCE);
					}
				}
			}
		}
	}
}
