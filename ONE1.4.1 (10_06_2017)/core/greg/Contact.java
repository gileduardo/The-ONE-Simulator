
package core.greg;

/* DADOS DOS ÚLTIMOS CONTATOS EFETUADOS */
public class Contact {

	private String id;
	private double time;
	private double px;
	private double py;
	private double sx;
	private double sy;
	private int nc;

	public Contact (String id, double time, double px, double py, double sx, double sy, int nc) {
	  
		this.id = id;
		this.time = time;
		this.px = px;
		this.py = py;
		this.sx = sx;
		this.sy = sy;
		this.nc = nc;
	}

	// Identificador do nó contactado
	public String getID() { return this.id; }
	public void setID(String id) { this.id = id; }

	// Momento do contato com o nó (tempo do nó contatado)
	public double getTime() { return this.time; }
	public void setTime(double time) { this.time = time; }

	// Posição do nó no momento do contato
	public double getPx() { return this.px; }
	public void setPx(double px) { this.px = px; }
	public double getPy() { return this.py; }
	public void setPy(double py) { this.py = py; }

	// Velocidade do nó no momento do contato
	public double getSx() { return this.sx; }
	public void setSx(double sx) { this.sx = sx; }
	public double getSy() { return this.sy; }
	public void setSy(double sy) { this.sy = sy; }

	// Informação Nova - Produzir Roteamento
	public int getNew() { return this.nc; }
	public void setNew(int nc) { this.nc = nc; }
}
