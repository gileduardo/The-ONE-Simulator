/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.ciente;

import java.util.*;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.greg.Contact;
import core.SimReport;
import core.SimScenario;
import movement.map.*;

public class AlgorithmC {
	
	// Distância Euclidian entre o Destino e o Vizinho
	private double distance;
	// Número Máximo de Saltos da Mensagem
	private int nrHops;
	// Tabela GREG - Mapeia Número de Réplicas em Número de Saltos
	private int tableGREG[] = {-1, -1, 4, 4, 3, 3, 3, 3, 3, 3, 3};
	// Próximo Cruzamento/Posição do Nó e Tempo para Alcançá-lo
	private Coord next_cross_node;
	private double time_next_cross;
	// Cruzamento Previsto para Localização do Destino
	private Coord cruz_destino;
	// Objeto para gerar Relatório Margem de Erro - Previsão Posição
	private static SimReport objSimReport;
	// Objeto para obter Mapa Digital
	public static DigitalMapC objDigitalMap;

	public AlgorithmC (int nrReplicas) { 

		if(nrReplicas > 10) { nrReplicas = 10; }
		else if(nrReplicas < 0) { nrReplicas = 0; }
	
		this.nrHops = tableGREG[nrReplicas];
		this.objDigitalMap = new DigitalMapC();
	}

	public double getDistance() { return this.distance; }
	public void setDistance(double distance) { this.distance = distance; }

	public Coord getNextCross() { return this.next_cross_node; }
	public double getTimeNextCross() { return this.time_next_cross; }

	public boolean runStrategy(DTNHost vizinho, Coord des_ca) {

		Coord viz_ca = vizinho.getLocation();
		Coord viz_pc = getProximoCruzamentoNo(vizinho);

		try {
			// Verifica se o Vizinho está se Aproximando do Cruzamento onde está o Destino
			if(viz_pc.distance(des_ca) < viz_ca.distance(des_ca)) {	
				this.distance = viz_pc.distance(des_ca);
				return true;
			}
			else {
				return false;
			}

		} catch(Exception e) {
			System.out.println("[ERRO]: Run Greedy Strategy! (" + e.toString() + "");
			return false;
		}
	}

	public boolean getHopsGREG(Message m) {

		if(this.nrHops == -1 || m.getHopCount() < this.nrHops) {
			return true;
		}
		else {
			return false;
		}
	}

	public Coord getCruzamentoEstimado(DTNHost destino) {	

		this.cruz_destino = destino.getLocation();
		return this.cruz_destino;
	}

	public Coord getPredictedCross(Contact destino, double tempo) {

		double px, py;
		
		px = (destino.getSx() * tempo) + destino.getPx();
		py = (destino.getSy() * tempo) + destino.getPy();
		this.cruz_destino = this.objDigitalMap.getCloserCross(new Coord(px, py));

		return this.cruz_destino;
	}

	public void setProximoCruzamentoNo(DTNHost no) {
	
		// if(no.getAddress() == 21) {

		Coord c[] = this.objDigitalMap.getCrossings(no);
		Coord curr_pos = no.getLocation();
		Coord prox_pos = no.getDestination();
					
		// Verifica o cruzamento para o qual está indo (menor distancia)
		if(prox_pos.distance(c[0]) < prox_pos.distance(c[1])) {
			this.next_cross_node = c[0];
			this.time_next_cross = curr_pos.distance(c[0])/no.getSpeed();
			// System.out.println("[Próximo Cruzamento c0]: " + this.time_next_cross);
		}
		else {
			this.next_cross_node = c[1];
			this.time_next_cross = curr_pos.distance(c[0])/no.getSpeed();
			// System.out.println("[Próximo Cruzamento c1]: " + this.time_next_cross);
		}
		// }
	}

	public Coord getProximoCruzamentoNo(DTNHost no) {
		
		Coord c[] = this.objDigitalMap.getCrossings(no);
		Coord prox_pos = no.getDestination();
				
		if(prox_pos != null) {			
			// Verifica o cruzamento para o qual está indo (menor distancia)
			if(prox_pos.distance(c[0]) < prox_pos.distance(c[1])) {
				prox_pos = c[0];
			}
			else {
				prox_pos = c[1];
			}
		}
		else {
			prox_pos = c[1];
		}

		return prox_pos;
	}

	public double getTempoProximoCruzamento(DTNHost no) {

		double tempo;

		Coord c[] = this.objDigitalMap.getCrossings(no);
		Coord prox_pos = no.getDestination();
		Coord curr_pos = no.getLocation();
					
		// Verifica o cruzamento para o qual está indo (menor distancia)
		if(prox_pos.distance(c[0]) < prox_pos.distance(c[1])) {
			tempo = curr_pos.distance(c[0])/no.getSpeed();
		}
		else {
			tempo = curr_pos.distance(c[1])/no.getSpeed();
		}

		return tempo;	
	}

	public void setRelatorioMargemErro(Coord origem, Coord destino, double tempo, Coord estimado) {

		double margem_erro = estimado.distance(destino);
		double distancia = origem.distance(destino);

		// Repassa dados a classe de relatório de erros
		objSimReport.getInstance().setErrorMargin(margem_erro, distancia, tempo); 

	}

}
