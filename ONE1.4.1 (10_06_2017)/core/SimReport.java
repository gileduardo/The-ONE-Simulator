/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package core;

import java.util.*;

/**
 * Dados para relatório da margem de erro para o modelo de previsão sobre a localização
 */
public class SimReport {

	/* 	0 < distancia > 200:		d1
		200 < distancia > 400:		d2
		400 < distancia > 600:		d3
		600 < distancia > 800:		d4
		800 < distancia:			d5
	*/

	private List<Double> d1 = new ArrayList<Double>();
	private List<Double> d2 = new ArrayList<Double>();
	private List<Double> d3 = new ArrayList<Double>();
	private List<Double> d4 = new ArrayList<Double>();
	private List<Double> d5 = new ArrayList<Double>();

	private List<Double> t1 = new ArrayList<Double>();
	private List<Double> t2 = new ArrayList<Double>();
	private List<Double> t3 = new ArrayList<Double>();
	private List<Double> t4 = new ArrayList<Double>();
	private List<Double> t5 = new ArrayList<Double>();

	private int c1=0, c2=0, c3=0, c4=0, c5=0;
	private static SimReport report = null;
	
	private SimReport() {}
	
	static {
		DTNSim.registerForReset(SimReport.class.getCanonicalName());
	}
	
	/**
	 * Get the instance of the class that can also change the time.
	 * @return The instance of this report
	 */
	public static SimReport getInstance() {
		if (report == null) {
			report = new SimReport();
		}
		return report;
	}
	
	/**
	 * Returns the range error rounded to the nearest integer
	 * @return Range as integer
	 */
	public double getRangeError(int error, int tipo) {
	
		// Distância
		if(tipo == 0) {
			if(error == 1) { return getMedia(d1); }
			else if(error == 2) { return getMedia(d2); }
			else if(error == 3) { return getMedia(d3); }
			else if(error == 4) { return getMedia(d4); }
			else if(error == 5) { return getMedia(d5); }
		}
		// Tempo
		else {
			if(error == 1) { return getMedia(t1); }
			else if(error == 2) { return getMedia(t2); }
			else if(error == 3) { return getMedia(t3); }
			else if(error == 4) { return getMedia(t4); }
			else if(error == 5) { return getMedia(t5); }
		}
		
		return -1;
	}
	
	/**
	 * Sets the range error of prediction.
	 * @param r the margin of error to set
	 * @param d the distance to destination
	 */
	public void setErrorMargin(double me, double d, double t) {

		// Distancia		
		if(d <= 200) { d1.add(me); }
		else if(d > 200 && d <= 400) { d2.add(me); }
		else if(d > 400 && d <= 600) { d3.add(me); }
		else if(d > 600 && d <= 800) { d4.add(me); }
		else if(d > 800) { d5.add(me); }

		// Tempo
		if(t <= 60) { t1.add(me); }
		else if(t > 60 && t <= 120) { t2.add(me); }
		else if(t > 120 && t <= 180) { t3.add(me); }
		else if(t > 180 && t <= 240) { t4.add(me); }
		else if(t > 240) { t5.add(me); }
	}

	public double getMediana(List<Double> l) {

		int tam;
		double a, b, ret = -1;

		tam = l.size();
		Collections.sort(l);
		if(tam%2 == 0) {
			a = l.get(tam/2);
			b = l.get((tam/2)+1);
			ret = (a+b)/2;
		}
		else {
			ret = l.get((tam/2)+1);
		}

		return ret;
	}
		
	public double getMedia(List<Double> l) {

		int tam = l.size();
		double soma=0;

		for(int a=0; a<tam; a++) {
			soma = soma + l.get(a);
		}

		return soma/tam;
	}

	public static void reset() {
	}
}
