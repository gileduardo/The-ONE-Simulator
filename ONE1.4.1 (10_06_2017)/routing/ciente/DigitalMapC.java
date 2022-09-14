/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.ciente;

import java.util.*;
import core.Coord;
import core.DTNHost;
import core.Message;
import core.SimScenario;
import movement.map.*;

public class DigitalMapC {
	
	// Coeficientes: Equação da reta (a-angular, b-linear)
	private double a, b;
	// Objeto para obter Mapa Digital
	private static SimMap objSimMap;
	// Objeto para obter menor caminho via Dijkstra
	private DijkstraPathFinder objDijks;

	public DigitalMapC () {
		this.objSimMap = null;
		this.objDijks = new DijkstraPathFinder(null);
	}

	private void setInstanceMap() {

		if(this.objSimMap == null) {
			this.objSimMap = SimScenario.getInstance().getMap();
		}
	}

	private double findStreet(Coord c1, Coord c2, Coord no) {

		// Coeficiente angular
		this.a = (c1.getY() - c2.getY()) / (c1.getX() - c2.getX());
		// Coeficiente Linear
		this.b = c1.getY() - (this.a * c1.getX());
		// Verifica se o nó está nessa rua
		return (this.a*no.getX() + this.b - no.getY());
	}

	public Coord[] getCrossings(DTNHost no) {

		double ret, menor=100000;
		Coord c[] = new Coord[2];

		setInstanceMap();

		List<MapNode> nodes = this.objSimMap.getNodes();
		for (MapNode n : nodes) {
			List<MapNode> neighbors = n.getNeighbors();
			for (MapNode ng : neighbors) {
				// Busca a rua onde o nó se encontra
				ret = findStreet(n.getLocation(), ng.getLocation(), no.getLocation());

				if(menor > Math.abs(ret)) {
					menor = Math.abs(ret);
					c[0] = n.getLocation();
					c[1] = ng.getLocation();
				}
			}
		}

		return c;		
	}

	public Coord getCloserCross(Coord estimate_pos) {
	
		double dist, menor=100000;
		Coord cross = estimate_pos;		

		setInstanceMap();

		List<MapNode> nodes = this.objSimMap.getNodes();
		for (MapNode n : nodes) {
	
			dist = n.getLocation().distance(estimate_pos);
			if(dist < menor) { 
				menor = dist;
				cross = n.getLocation();
			}

			List<MapNode> neighbors = n.getNeighbors();
			for (MapNode ng : neighbors) {

				dist = ng.getLocation().distance(estimate_pos);
				if(dist < menor) {
					menor = dist;
					cross = ng.getLocation();
				}
			}
		}

		return cross;
	}

	/*public List<MapNode> getPath(Coord origem, Coord destino) {

		MapNode o, d;
		List<MapNode> path;
		
		setInstanceMap();

		o = this.objSimMap.getNodeByCoord(origem);
		d = this.objSimMap.getNodeByCoord(destino);

		if(o == null || d == null) {
			System.out.println("============\n[COORDENADA NULA]\n============");
			path = null;
		}
		else {

			path = this.objDijks.getShortestPath(o, d);

			if(path == null) {
		
				System.out.println("============\n[CAMINHO NULO]\n============");
			}
			else {
				System.out.println("============");
				for (MapNode n : path) {
					System.out.println("(x: " + n.getLocation().getX() +
									", y: " + n.getLocation().getY() + ")");
				}
				System.out.println("============");
			}
		}

		return path;
	}*/
}
