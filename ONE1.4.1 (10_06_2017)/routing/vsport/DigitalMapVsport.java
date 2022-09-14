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
import core.SimScenario;
import movement.Path;
import movement.map.*;

public class DigitalMapVsport {
	
	// Coeficientes: Equação da reta (a-angular, b-linear)
	private double a, b;
	// Objeto para obter Mapa Digital
	private static SimMap objSimMap;
	// Objeto para obter menor caminho via Dijkstra
	private DijkstraPathFinder objDijkstra;

	public DigitalMapVsport () {
		this.objSimMap = null;
		this.objDijkstra = new DijkstraPathFinder(null);
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

	public MapNode[] getCrossingsMap(DTNHost no) {

		double ret, menor=100000;
		MapNode c[] = new MapNode[2];

		setInstanceMap();

		List<MapNode> nodes = this.objSimMap.getNodes();
		for (MapNode n : nodes) {
			List<MapNode> neighbors = n.getNeighbors();
			for (MapNode ng : neighbors) {
				// Busca a rua onde o nó se encontra
				ret = findStreet(n.getLocation(), ng.getLocation(), no.getLocation());

				if(menor > Math.abs(ret)) {
					menor = Math.abs(ret);
					c[0] = n;
					c[1] = ng;
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

	public List<MapNode> getCrossingsInMaxRadius(double raio, Coord centro) {
		
		double distancia;
		List<MapNode> crossings = new ArrayList<MapNode>();

		setInstanceMap();

		List<MapNode> nodes = this.objSimMap.getNodes();
		for (MapNode n : nodes) {
	
			distancia = centro.distance(n.getLocation());
			if(distancia <= raio && !crossings.contains(n)) { 
				crossings.add(n);
			}

			List<MapNode> neighbors = n.getNeighbors();
			for (MapNode ng : neighbors) {

				distancia = centro.distance(ng.getLocation());
				if(distancia <= raio && !crossings.contains(ng)) { 
					crossings.add(ng);
				}
			}
		}

		return crossings;
	}

	public List<MapNode> getShortestMapPath(MapNode origem, MapNode destino) {
		return this.objDijkstra.getShortestPath(origem, destino);
	}

	public double getDistanceShortestMapPath(Coord orig, MapNode destino) {

		double distancia=-1;
		Coord o=null, d=null;
		List<MapNode> path; 
		MapNode origem = getCloserMapNode(orig);

		path = this.objDijkstra.getShortestPath(origem, destino);

		for (MapNode c : path) {
			if(distancia == -1) {
				distancia = 0;
				o = c.getLocation();
			}
			else {
				d = c.getLocation();
				distancia = distancia + o.distance(d);
				o = d;
			}
		}

		return distancia;
	}

	public MapNode getCloserMapNode(Coord pos) {

		double dist, menor=100000;
		MapNode node;		

		setInstanceMap();

		List<MapNode> nodes = this.objSimMap.getNodes();
		node = nodes.get(0);
		for (MapNode n : nodes) {
	
			dist = n.getLocation().distance(pos);
			if(dist < menor) { 
				menor = dist;
				node = n;
			}

			List<MapNode> neighbors = n.getNeighbors();
			for (MapNode ng : neighbors) {

				dist = ng.getLocation().distance(pos);
				if(dist < menor) {
					menor = dist;
					node = ng;
				}
			}
		}

		return node;

	}
}
