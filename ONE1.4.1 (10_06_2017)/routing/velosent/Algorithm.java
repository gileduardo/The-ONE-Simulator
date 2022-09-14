/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package routing.velosent;

import java.util.*;

public class Algorithm {

    // DADOS DO NÓ DE DESTINO
    double d_px, d_py, d_sx, d_sy;
    // DADOS DO NÓ VIZINHO
    double n_px, n_py, n_sx, n_sy;

    // DADOS PARCIAIS CALCULADOS AO LONGO DAS FASES DO PROTOCOLO
    // Idade do último contato
    double age;
    // Posição estimada do destino
    double Epx, Epy;
    // Equações da Reta - Coeficientes Angula/Linear
    double Dcoef_ang, Dcoef_lin;
    double Ncoef_ang, Ncoef_lin;
    // Ponto de Intersecção
    double interX, interY;
    // Tempo Necessário para o encontro
    double neededTime;
    // Posição final (momento do encontro) estimada do destino
    double EFpx, EFpy;
    // Distância Final entre o Vizinho e o Destino no momento da intersecção
    double final_distance;
    

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

    // Retorna a distância final entre a estimativa de posição do nó e a 
    // estimativa do destino momento da intersecção de suas trajetórias
    public double algorithmRun() {


	// SEGUNDA FASE DO ALGORITMO
	if(getEstimatePosition() == -1) return -1;
	// TERCEIRA FASE DO ALGORITMO - ETAPA 01
	if(getEquations() == -1) return -1;
	// TERCEIRA FASE DO ALGORITMO - ETAPA 02
	if(getIntersectionPoints() == -1) return -1;
	// TERCEIRA FASE DO ALGORITMO - ETAPA 03
	if(getNeededTime() == -1) return -1;
	// TERCEIRA FASE DO ALGORITMO - ETAPA 04
	if(getEstimatePositionInIntersection() == -1) return -1;
	// TERCEIRA FASE DO ALGORITMO - ETAPA 05
	if(getFinalDistance() == -1) return -1;

	return this.final_distance;
	
    }

    // SEGUNDA FASE DO ALGORITMO - Determina a posição estimada do nó de destino
    private int getEstimatePosition() { 

	try {
		// Calcula a posição estimada "X" e "Y"
		this.Epx = (this.d_sx * this.age) + this.d_px;
		this.Epy = (this.d_sy * this.age) + this.d_py;

	} catch(Exception e) {
		System.out.println("[ERRO]: FASE 02, posição estimada do destino! = " + e.toString());
		return -1;
	}

        return 0;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 01 - Determina as Equações das trajetórias: destino/vizinho
    public int getEquations() {

        double pxe, pye;

        try {
		// Equação da Reta (Trajetória): Coeficiente Angular e Coeficiente Linear - Nó Destino
		this.Dcoef_ang = (this.Epy - this.d_py) / (this.Epx - this.d_px);
		this.Dcoef_lin = this.d_py - (this.Dcoef_ang * this.d_px);
		
		// Equação da Reta (Trajetória): Coeficiente Angula e Coeficiente Linear - Nó Vizinho
		pxe = (this.n_sx * 4) + this.n_px;
		pye = (this.n_sy * 4) + this.n_py;
		this.Ncoef_ang = (pye - this.n_py) / (pxe - this.n_px);
		this.Ncoef_lin = this.n_py - (this.Ncoef_ang * this.n_px);

        } catch (Exception e) {
                System.out.println("[ERRO]: FASE 03 -> Etapa 01, equações das trajetórias = " + e.toString());
                return - 1;
        }

        return 0;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 02 - Determina o ponto de intersecção entre as trajetórias do nó vizinho/destino
    public int getIntersectionPoints() {

	try {
		this.interX = Math.round((this.Ncoef_lin - this.Dcoef_lin)/(this.Dcoef_ang - this.Ncoef_ang));
		this.interY = Math.round((this.Dcoef_ang * this.interX) + this.Dcoef_lin);

	} catch (Exception e) {
                System.out.println("[ERRO]: FASE 03 -> Etapa 02, ponto de intersecção das trajetórias = " + e.toString());
                return - 1;
        }

	return 0;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 03 - Determina tempo necessário para o vizinho chegar ao ponto de intersecção
    public int getNeededTime() {

	try {
                this.neededTime = (this.interX - this.n_px)/this.n_sx;
		
		if(this.neededTime < 0)
			return -1;

	} catch (Exception e) {
                System.out.println("[ERRO]: FASE 03 -> Etapa 03, tempo necessário para o encontro = " + e.toString());
                return - 1;
        }

	return 0;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 04 - Determina a posição estimada do destino no momento da intersecção
    public int getEstimatePositionInIntersection() {

	try {
                this.EFpx = (this.d_sx * this.neededTime) + this.d_px;
		this.EFpy = (this.d_sy * this.neededTime) + this.d_py;

	} catch (Exception e) {
                System.out.println("[ERRO]: FASE 03 -> Etapa 03, tempo necessário para o encontro = " + e.toString());
                return - 1;
        }

	return 0;
    }

    // TERCEIRA FASE DO ALGORITMO / ETAPA 05 - Determina a distância entre vizinho e destino no momento da intersecção
    public int getFinalDistance() {

	try {
                this.final_distance = distanceTwoPoints(this.interX, this.interY, this.EFpx, this.EFpy);

	} catch (Exception e) {
                System.out.println("[ERRO]: FASE 03 -> Etapa 03, tempo necessário para o encontro = " + e.toString());
                return - 1;
        }

	return 0;
    }

    // Calcula a distância entre dois pontos
    private double distanceTwoPoints(double px, double py, double px1, double py1) {
        return Math.sqrt(Math.pow((px1 - px), 2) + Math.pow((py1 - py), 2));
    }
}
