/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.vsport;

import java.util.*;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.greg.Contact;
import core.SimReport;
import core.SimScenario;
import movement.map.*;

public class AlgorithmVsport {
	
	// Distância Euclidian entre o Destino e o Vizinho
	private double distance;
	// Número Máximo de Saltos da Mensagem
	private int nrHops;
	// Tabela GREG - Mapeia Número de Réplicas em Número de Saltos
	private int tableGREG[] = {-1, -1, 4, 4, 3, 3, 3, 3, 3, 3, 3};
	// Próximo Cruzamento/Posição do Nó e Tempo para Alcançá-lo
	private Coord next_cross_node;
	private double time_next_cross;
	// Raio máximo para seleção dos cruzamentos - estimativa posição destino
	private double raioMax;
	// Cruzamento Previsto para Localização do Destino
	private Coord cruz_destino;
	// Menor Caminho no Mapa - Próximo Cruzamento de Encaminhamento
	private List<MapNode> menor_caminho;
	// Objeto para gerar Relatório Margem de Erro - Previsão Posição
	private static SimReport objSimReport;
	// Objeto para obter Mapa Digital
	public static DigitalMapVsport objDigitalMap;

	public AlgorithmVsport (int nrReplicas, double raioMax) { 

		if(nrReplicas > 10) { nrReplicas = 10; }
		else if(nrReplicas < 0) { nrReplicas = 0; }
	
		this.nrHops = tableGREG[nrReplicas];
		this.raioMax = raioMax;
		this.objDigitalMap = new DigitalMapVsport();
	}

	public Coord getNextCross() { return this.next_cross_node; }
	public double getTimeNextCross() { return this.time_next_cross; }

	public void setShortPathToDestination(DTNHost origem, Coord destino) {
		
		MapNode mp_origem = this.objDigitalMap.getCloserMapNode(origem.getLocation()); 
		MapNode mp_destino = this.objDigitalMap.getCloserMapNode(destino); 

		// System.out.println("Origem: (" + mp_origem.getLocation().getX() + 
			// " , " + mp_origem.getLocation().getY() + ")");

		//System.out.println("Destino: (" + mp_destino.getLocation().getX() + 
			//" , " + mp_destino.getLocation().getY() + ")");

		this.menor_caminho = this.objDigitalMap.getShortestMapPath(mp_origem, mp_destino);

		// System.out.println("Total: " + this.menor_caminho.size());
	}	

	public boolean runStrategy(DTNHost vizinho) {

		if(this.menor_caminho.size() > 4) {

			Coord viz_ca = vizinho.getLocation();
			Coord viz_pc = getProximoCruzamentoNo(vizinho);
			Coord prox_cruz = this.menor_caminho.get(4).getLocation();

			try {
				// Verifica se o Vizinho está se Aproximando do Cruzamento do Destino
				if(viz_pc.distance(prox_cruz) < viz_ca.distance(prox_cruz)) {	
					this.distance = viz_pc.distance(prox_cruz);
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
		else {
			return true;
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

		int pos;
		double px, py, raio_max, distancia, menor = 100000;
		Coord antigo, previsto, cross_aux = null;
		List<MapNode> crossings; 
		
		antigo = new Coord(destino.getPx(), destino.getPy());
		px = (destino.getSx() * tempo) + destino.getPx();
		py = (destino.getSy() * tempo) + destino.getPy();
		previsto = new Coord(px, py);

		raio_max = antigo.distance(previsto)/2;
		previsto.setLocation(px/2, py/2);

		if(raio_max > this.raioMax) {
			raio_max = this.raioMax;
		}

		// Encontra todos os cruzamentos dentro desse raio
		crossings = this.objDigitalMap.getCrossingsInMaxRadius(raio_max, antigo);

		// Obtém a distancia do menor caminho para todos esses cruzamentos
		for (MapNode c : crossings) {

			if(cross_aux == null) { cross_aux = c.getLocation(); }

			distancia =  this.objDigitalMap.getDistanceShortestMapPath(antigo, c);

			if(Math.abs(distancia - raio_max) < menor) {
				menor = Math.abs(distancia - raio_max);
				cross_aux = c.getLocation(); 
			}
		}
		this.cruz_destino = cross_aux; 

		// Seleciona Aleatoriamente
		// pos = new Random().nextInt(crossings.size());
		// this.cruz_destino = crossings.get(pos).getLocation();
		
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
					
		// Verifica o cruzamento para o qual está indo (menor distancia)
		if(prox_pos.distance(c[0]) < prox_pos.distance(c[1])) {
			prox_pos = c[0];
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

		// Menor Caminho entre o destino e a origem
		// this.objDigitalMap.getPath(origem, destino);
	}

}
