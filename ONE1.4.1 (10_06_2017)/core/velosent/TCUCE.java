
package core.velosent;

/* TABELA DO CONTEXTO DOS ÚLTIMOS CONTATOS EFETUADOS */
public class TCUCE {

    private String id;
    private double time;
    private double localtime;
    private double px;
    private double py;
    private double sx;
    private double sy;

    public TCUCE (String id, double time, double localtime, double px, double py, double sx, double sy) {
        
        this.id = id;
        this.time = time;
        this.localtime = localtime;
        this.px = px;
        this.py = py;
        this.sx = sx;
        this.sy = sy;
    }
   
    // Identificador do nó contactado
    public String getID() { return this.id; }
    public void setID(String id) { this.id = id; }
    // Momento do contato com o nó (tempo do nó contatado)
    public double getTime() { return this.time; }
    public void setTime(double time) { this.time = time; }
    // Momento do contato com o nó (tempo do nó que fez o contato - tempo local)
    public double getLocalTime() { return this.localtime; }
    public void setLocalTime(double localtime) { this.time = localtime; }
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
}
