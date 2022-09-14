/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.rpd;

import java.util.*;

public class Algorithm {

    // DADOS DO NÓ DE DESTINO
    double d_px, d_py, d_sx, d_sy;
    // DADOS DO NÓ VIZINHO
    double n_px, n_py, n_sx, n_sy;

    // DADOS PARCIAIS CALCULADOS AO LONGO DAS FASES DO PROTOCOLO
    // Idade do último contato
    double age;
    // Posição prevista para o destino
    double d_Epx, d_Epy;
    // Verifica se os nós estão se aproximando ou distanciando
    double fator_aprox;

    public Algorithm () {
	
    }

    // Recebe a idade do último contato com nó de destino
    public void setAge(double age) { this.age = age; }

    // Recebe os dados do nó de destino no último contato feito com ele
    public void setDataDestination(double d_px, double d_py, double d_sx, double d_sy) {
	this.d_px = d_px; 
	this.d_py = d_py;
	this.d_sx = d_sx;
	this.d_sy = d_sy;
    }

    // Recebe os dados do nó vizinho que pode ser o próximo ancora
    public void setDataNeighbor(double n_px, double n_py, double n_sx, double n_sy) {
	this.n_px = n_px; 
	this.n_py = n_py;
	this.n_sx = n_sx;
	this.n_sy = n_sy;
    }

    // Predição sobre a posição do destino
    private int getPositionPrediction() { 

	try {
		// Calcula a posição estimada "X" e "Y"
		this.d_Epx = (this.d_sx * this.age) + this.d_px;
		this.d_Epy = (this.d_sy * this.age) + this.d_py;

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 02, posição estimada do destino! = " + e.toString());
		return -1;
	}

        return 0;
    }

    // Cálculo do Fator de Aproximação 
    public double getFatorAprox() {

	double x0, Vx, y0, Vy;

	if(getPositionPrediction() == -1) return 0;

	try {
		x0 = this.d_Epx - this.n_px;
		y0 = this.d_Epy - this.n_py;
		Vx = this.d_sx - this.n_sx;
		Vy = this.d_sy - this.n_sy;
		this.fator_aprox = (x0 * Vx) + (y0 * Vy);

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 03, calculo do Beta! = " + e.toString());
		return 0;
	}

	return this.fator_aprox;
    }
}
