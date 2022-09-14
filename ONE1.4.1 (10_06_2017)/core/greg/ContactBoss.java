/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.greg;

import java.util.*;

public class ContactBoss {

    // Tabela de Contatos
    private ArrayList<Contact> tabela;
    private Contact objContact;

    public ContactBoss() {
        this.tabela = new ArrayList<Contact>();
    }

    public ArrayList<Contact> getTabelaContatos() { return this.tabela; }

    public int setContact(String id, double time, double px, double py, double sx, double sy) {

        int pos = getContact(id);

        // New Contact,
        if(pos == -1) {
            newContact(id, time, px, py, sx, sy);
        }
        // Update Contact
        else {
            updateContact(pos, id, time, px, py, sx, sy);
        }

        return -1;
    }

    public Contact getContactLastIndex(int index) {
        return (Contact) this.tabela.get(index);
    }

    public Contact getContactLastID(String id) {

        if(this.tabela != null) {
		for(int i=0; i<this.tabela.size(); i++) {
			this.objContact = (Contact) this.tabela.get(i);

			if(this.objContact.getID().compareTo(id) == 0)
				return this.objContact;
		}
        }

        return null;
    }

    public void newContact(String id, double time, double px, double py, double sx, double sy) {
        this.tabela.add(new Contact(id, time, px, py, sx, sy, 1));
    }

    public void updateContact(int pos, String id, double time, double px, double py, double sx, double sy) {

        this.objContact = (Contact) this.tabela.get(pos);
        this.objContact.setTime(time);
        this.objContact.setPx(px);
        this.objContact.setPy(py);
        this.objContact.setSx(sx);
        this.objContact.setSy(sy);
	  this.objContact.setNew(1);
        this.tabela.set(pos, this.objContact);
    }

    public int getContact(String id) {

        if(this.tabela != null) {
            for(int i=0; i<this.tabela.size(); i++) {
                this.objContact = (Contact) this.tabela.get(i);

                if(this.objContact.getID().compareTo(id) == 0)
                    return i;
            }
        }

        return -1;
    }

    public void trocaTabelaContato(ArrayList<Contact> tb, String IDHost) {
		
		int flag;

		if(this.tabela != null && tb != null) {
			// Tabela do Outro Nó - Remota
            	for(int i=0;  i<tb.size(); i++) {
				// Informações de Contato do Próprio Nó - não armazenar
				if(tb.get(i).getID().compareTo(IDHost) == 0) { flag = 1; }
				else { flag = 0; }

				// Tabela do Nó Corrente - Local
				for(int j=0;  j<this.tabela.size() && flag == 0; j++) {
					// Já possui o contato na tabela
					if(this.tabela.get(j).getID().compareTo(tb.get(i).getID()) == 0) {
						flag = 1;
						// As informações são mais recentes atualiza
						if(this.tabela.get(j).getTime() < tb.get(i).getTime()) {
							// Atulaliza as informações
							updateContact(	j, 
										tb.get(i).getID(), 
										tb.get(i).getTime(), 
										tb.get(i).getPx(), 
										tb.get(i).getPy(), 
										tb.get(i).getSx(), 
										tb.get(i).getSy()
									);
						}
					}
				}

				if(flag == 0) {
					 newContact(	tb.get(i).getID(), 
								tb.get(i).getTime(), 
								tb.get(i).getPx(), 
								tb.get(i).getPy(), 
								tb.get(i).getSx(), 
								tb.get(i).getSy()
							);		
				}
			}
    		}
	}

	public void setNewContact(String ID, int nc) {
		
		if(this.tabela != null) {
		      for(int i=0; i<this.tabela.size(); i++) {
		          this.objContact = (Contact) this.tabela.get(i);

		          if(this.objContact.getID().compareTo(ID) == 0)
		              this.objContact.setNew(nc);
		      }
        	}
	}

	public int getNewContact(String ID) {
		
		if(this.tabela != null) {
		      for(int i=0; i<this.tabela.size(); i++) {
		          this.objContact = (Contact) this.tabela.get(i);

		          if(this.objContact.getID().compareTo(ID) == 0)
		              return this.objContact.getNew();
		      }
        	}
	
		return 0;
	}

	public void printContactTable() {
		
		String linha;

		if(this.tabela != null) {

			System.out.println("ID\t\tTM\t\t" + this.tabela.size());
			for(int i=0;  i<this.tabela.size(); i++) {
				linha = tabela.get(i).getID() + "\t" + tabela.get(i).getTime(); 
				System.out.println(linha);
			}
			System.out.println("\n\n");
		}
	}

    public void printAContact(String id) {

        String linha;
        if(this.tabela != null) {
            for(int i=0;  i<this.tabela.size(); i++) {
                this.objContact = (Contact) this.tabela.get(i);
                if(this.objContact.getID().compareTo(id) == 0) {
                    System.out.println("--------------------------------------");
                    linha = "" + this.objContact.getID() + "  " + this.objContact.getTime() + "  " + this.objContact.getPx();
                    linha = linha + "  " + this.objContact.getPy() + "  " + this.objContact.getSx() + "  "; 
			  linha = linha + this.objContact.getSy();
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
                this.objContact = (Contact) this.tabela.get(i);
                System.out.println("--------------------------------------");
                linha = "" + this.objContact.getID() + "  " + this.objContact.getTime() + "  " + this.objContact.getPx();
                linha = linha + "  " + this.objContact.getPy() + "  " + this.objContact.getSx() + "  " + this.objContact.getSy();
                System.out.println(linha);
                System.out.println("--------------------------------------");
            }
        }
    }
}
