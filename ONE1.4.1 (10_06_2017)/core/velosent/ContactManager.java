/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.velosent;

import java.util.*;

public class ContactManager {

    // Tabela de Contatos
    private ArrayList<TCUCE> tabela;
    private TCUCE objTCUCE;

    public ContactManager() {
        this.tabela = new ArrayList<TCUCE>();
    }

    public int setContactLast(String id, double time, double localtime, double px, double py, double sx, double sy) {

        int pos = getContact(id);

        // New Contact,
        if(pos == -1) {
            newContact(id, time, localtime, px, py, sx, sy);
        }
        // Update Contact
        else {
            updateContact(pos, id, time, localtime, px, py, sx, sy);
        }

        return -1;
    }

    public TCUCE getContactLastIndex(int index) {
        return (TCUCE) this.tabela.get(index);
    }

    public TCUCE getContactLastID(String id) {

        if(this.tabela != null) {
                for(int i=0;  i<this.tabela.size(); i++) {
                this.objTCUCE = (TCUCE) this.tabela.get(i);

                if(this.objTCUCE.getID().compareTo(id) == 0)
                    return this.objTCUCE;
            }
        }

        return null;
    }

    public void newContact(String id, double time, double localtime, double px, double py, double sx, double sy) {
        this.tabela.add(new TCUCE(id, time, localtime, px, py, sx, sy));
    }

    public void updateContact(int pos, String id, double time, double localtime, double px, double py, double sx, double sy) {

        this.objTCUCE = (TCUCE) this.tabela.get(pos);
        this.objTCUCE.setTime(time);
        this.objTCUCE.setLocalTime(localtime);
        this.objTCUCE.setPx(px);
        this.objTCUCE.setPy(py);
        this.objTCUCE.setSx(sx);
        this.objTCUCE.setSy(sy);
        this.tabela.set(pos, this.objTCUCE);
    }

    public int getContact(String id) {

        if(this.tabela != null) {
            for(int i=0;  i<this.tabela.size(); i++) {
                this.objTCUCE = (TCUCE) this.tabela.get(i);

                if(this.objTCUCE.getID().compareTo(id) == 0)
                    return i;
            }
        }

        return -1;
    }

    public void updateTCUCEBestAge(String id, TCUCE tcuce, double newLocalTime) {	
	
	setContactLast(id, tcuce.getTime(), newLocalTime, tcuce.getPx(), tcuce.getPy(), tcuce.getSx(), tcuce.getSy());
    }

    public void printAContact(String id) {

        String linha;
        if(this.tabela != null) {
            for(int i=0;  i<this.tabela.size(); i++) {
                this.objTCUCE = (TCUCE) this.tabela.get(i);
                if(this.objTCUCE.getID().compareTo(id) == 0) {
                    System.out.println("--------------------------------------");
                    linha = "" + this.objTCUCE.getID() + "  " + this.objTCUCE.getTime() + "  " + this.objTCUCE.getPx();
                    linha = linha + "  " + this.objTCUCE.getPy() + "  " + this.objTCUCE.getSx() + "  " + this.objTCUCE.getSy();
                    System.out.println(linha);
                    System.out.println("--------------------------------------");
                    return;
                }
            }
        }
    }

    public void printAllContacts() {

        String linha;
        if(this.tabela != null) {
            for(int i=0;  i<this.tabela.size(); i++) {
                this.objTCUCE = (TCUCE) this.tabela.get(i);
                System.out.println("--------------------------------------");
                linha = "" + this.objTCUCE.getID() + "  " + this.objTCUCE.getTime() + "  " + this.objTCUCE.getPx();
                linha = linha + "  " + this.objTCUCE.getPy() + "  " + this.objTCUCE.getSx() + "  " + this.objTCUCE.getSy();
                System.out.println(linha);
                System.out.println("--------------------------------------");
            }
        }
    }
}
