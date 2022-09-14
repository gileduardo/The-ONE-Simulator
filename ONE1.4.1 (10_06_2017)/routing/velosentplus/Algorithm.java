/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.velosentplus;

import java.util.*;

public class Algorithm {

    // DADOS DO NÓ DE DESTINO
    double d_px, d_py, d_sx, d_sy;
    // DADOS DO NÓ VIZINHO
    double n_px, n_py, n_sx, n_sy, n_range;
    // DADOS DA MENSAGEM E DA INTERFACE DE REDE
    int msg_size, transmit_speed;

    // DADOS PARCIAIS CALCULADOS AO LONGO DAS FASES DO PROTOCOLO
    // Idade do último contato
    double age;
    // Posição estimada do destino
    double d_Epx, d_Epy;
    // Verificador para identificar se a menor distância já ocorreu
    double beta;
    // Distancia minima entre o vizinho e o destino     
    double min_distance;
    // Tempo estimado para o sistema alcançar a distância mínima
    double min_time;
    // Tempo de Entrada e Saída do Destino no Raio de Alcance do Nó
    double time_in, time_out;

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
    public void setDataNeighbor(double n_px, double n_py, double n_sx, double n_sy, double n_range, int transmit_speed) {
	this.n_px = n_px; 
	this.n_py = n_py;
	this.n_sx = n_sx;
	this.n_sy = n_sy;
	this.n_range = n_range;
	this.transmit_speed = transmit_speed;
    }

    // Retorna a distância final entre a estimativa de posição do nó e a 
    // estimativa do destino momento da intersecção de suas trajetórias
    public double algorithmRun() {

	// SEGUNDA FASE DO ALGORITMO
	if(getEstimatePosition() == -1) return -1;

	// ===========================
	// TERCEIRA FASE - VERSÃO 2.0
	// ===========================
	// 1º Passo: Verificar se (z) ou (b) é menor que 0 (b < 0)
	//	-> SIM: utilizar o nó e efetuar os cálculos restantes
	//	-> NÃO: descartar o nó - retornar '-1'
	if(calcBeta() >= 0) return -1;
	
	// 2º Passo: Calcular a distância mínima e verificar se ela é menor que o raio de alcance do nó em questão
	//	-> SIM:	utilizar o nó e efetuar os cálculos restantes 
	//	-> NÃO: descartar o nó - retornar '-1'
	if(calcMinDistance() < 0 || calcMinDistance() < this.n_range) return -1;

	// 3º Passo: Calular o tempo mínimo para que o nó alcance a distância mínima
	//	-> retornar o tempo mínimo
	return calcMinTime();
    }

    // SEGUNDA FASE DO ALGORITMO - Determina a posição estimada do nó de destino
    private int getEstimatePosition() { 

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

    // TERCEIRA FASE DO ALGORITMO / ETAPA 01 - 	Calcula o valor de beta - permite identificar se a distância mínima 
    //						já foi alcançada anteriormente
    public double calcBeta() {

	double x0, Vx, y0, Vy;

	try {
		x0 = this.d_Epx - this.n_px;
		y0 = this.d_Epy - this.n_py;
		Vx = this.d_sx - this.n_sx;
		Vy = this.d_sy - this.n_sy;
		this.beta = (x0 * Vx) + (y0 * Vy);

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 03, calculo do Beta! = " + e.toString());
		return 0;
	}

	return this.beta;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 02 - Calcula a distância mínima entre o vizinho e o destino
    public double calcMinDistance() {

	double x0, Vx, y0, Vy;

	try {
		x0 = this.d_Epx - this.n_px;
		y0 = this.d_Epy - this.n_py;
		Vx = this.d_sx - this.n_sx;
		Vy = this.d_sy - this.n_sy;

		this.min_distance = ((y0 * Vx) - (x0 * Vy)) / Math.sqrt((Vx * Vx) + (Vy * Vy));

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 03, calculo da distância mínima! = " + e.toString());
		return -1;
	}

	return this.min_distance;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 03 - Calcula o tempo mínimo para que o sistema alcance a distância mínima
    public double calcMinTime() {
	
	double V, Vx, Vy;

	try {
		Vx = this.d_sx - this.n_sx;
		Vy = this.d_sy - this.n_sy;
		V = (Vx * Vx) + (Vy * Vy);

		this.min_time = (-1 * this.beta) / (V * V);

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 03, calculo da distância mínima! = " + e.toString());
		return -1;
	}

	return this.min_time;
	
    }



    /*	=======================================================================================
	=======================================================================================
	==== CONSIDERANDO A JANELA DE CONTATO, O TAMANHO DA MENSAGEM E TAXA DE TRANSMISSÃO ====
	=======================================================================================
	======================================================================================= 
    */

    // Recebe o tamanho da mensagem
    public void setMsgSize(int msg_size) { this.msg_size = msg_size; }

    // Retorna o tempo mínimo necessário para vizinho alcançar uma posição onde o destino encontre-se sob
    // o raio de alcance de sua tranamissão e assim transmitir a mensagem
    public double algorithmRunCW() {

	// SEGUNDA FASE DO ALGORITMO
	if(getEstimatePosition() == -1) return -1;

	// ===========================
	// TERCEIRA FASE - VERSÃO 2.1
	// ===========================
	// 1º Passo: Verificar se (z) ou (b) é menor que 0 (b < 0)
	//	-> SIM: utilizar o nó e efetuar os cálculos restantes
	//	-> NÃO: descartar o nó - retornar '-1'
	if(calcBeta() >= 0) return -1;
	
	// 2º Passo: Verficar se a Janela de Contato é maior que Tempo necessário para transmitir a mensagem ao destino
	//	-> SIM:	utilizar o nó e efetuar os cálculos restantes 
	//	-> NÃO: descartar o nó - retornar '-1'	
	if((calcContactWindow() < calcTransmissionTime()) || (calcMinDistance() < this.n_range)) return -1;

	// 3º Passo: Retorna o tempo necessário para o destino entrar no Raio de Alcance do Nó
	//	-> retornar o time_in
	return this.time_in;
    }

    public double calcContactWindow() {
	
	double t_min = calcMinTime();
	double x0, y0, Vx, Vy, V, R, value;

	try {
		x0 = this.d_Epx - this.n_px;
		y0 = this.d_Epy - this.n_py;
		Vx = this.d_sx - this.n_sx;
		Vy = this.d_sy - this.n_sy;
		V = (Vx * Vx) + (Vy * Vy);
		R = this.n_range;

		value = (Math.pow(this.beta, 2)) - (Math.pow(V, 2))*(Math.pow(x0, 2)+Math.pow(y0, 2)-Math.pow(R, 2));
		value = Math.sqrt(value) / Math.pow(V, 2);

		this.time_in = t_min - value;
		this.time_out = t_min + value;

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 03, calculo da janela de contato! = " + e.toString());
		return -1;
	}

	return (this.time_out - this.time_in);
    }	

    public double calcTransmissionTime() {

	return this.msg_size / this.transmit_speed;
    }
}
