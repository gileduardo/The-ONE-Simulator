------------------------
-- ARQUIVOS AGREGADOS --
------------------------
1) Criada uma pasta "velosent" dentro do diretório "core/";

2) Dentro da pasta "core/velosent/" foram adicionadas duas classes:
	--------------
	- TCUCE.java - 		
	--------------
	que representa as informações de contexto que são armazenadas 
	pelos nós quando estes efetuam contatos	com os outros nós da 
	rede (id, tempo, posição em x e y, velocidade em x e y);

	-----------------------
	- ContactManager.java -
	-----------------------
	que gerencia as informações de contexto capturadas, ou seja, 
	possui a tabela TCUCE (ArrayList<TCUCE>) com todos os dados 
	armazenados ao longo dos contatos efetuados com outros nós,
	adicionando, atualizando e removendo dados desta tabela;

3) Criada uma pasta "velosent" dentro do diretório "routing/"

4) Dentro da pasta "routing/velosent/" foram adicionadas duas classes:
	---------------
	- ConMsg.java - 		
	---------------
	para que fosse possível implementar o protocolo VeloSent sem
	alterar o funcionamento do simulador, eu criei uma classe que
	me permite relacionar para cada uma das mesnagens que estão sendo
	armazenadas pelo nó, qual a conexão (com um de seus vizinhos) deve 
	ser utilizada para transmissão;

	------------------
	- Algorithm.java -
	------------------
	nela foi implementado o modelo matemático que permite determinar qual
	dos nós vizinhos, de acordo com as informações de contexto capturadas,
	deve ser o próximo a receber a mensagem que deseja-se rotear. Esta 
	classe é quem, através dos cálculos matemáticos definidos pela 
	metodologia proposta pelo VeloSent, identifica e configura as
	conexões que serão utilizadas para transmissão das mensagens 
	(classe ConMsg.java);

5) Dentro da pasta "routing/" foram adicionadas duas classes:
	---------------------
	- GreaseRouter.java -
	---------------------
	Representa o protocolo GREASE, baseado no algoritmo de "Roteamento do
	Último Encontro" (LER), que serviu como base ou trabalho relacionado
	para o desenvolvimento do VeloSent e VeloSent+. O arquivo contém a 
	metodologia de roteamento proposta pelo GREASE, relativa a "idade"
	dos contatos;

	-----------------------
	- VeloSentRouter.java -
	-----------------------
	Representa o protocolo desenvolvido por mim, em sua primeira versão, 
	baseado na ideia de "idade" dos contatos porposta pelo GREASE, porém
	adicionada de outras infomação de contexto. O arquivo contém a 
	metodologia de roteamento proposta pelo VeloSent, trabalhando em cima
	da velocidade e sentido dos nós, considerando que os mesmos se movimentam
	em MRU (Movimento Retilíneo Uniforme);

	---------------------------
	- VeloSentPlusRouter.java -
	---------------------------
	Representa o protocolo desenvolvido por mim, em sua segunda versão onde
	o modelo matemático para estimar o encontro dos nós é mais preciso, além
	de agregar mais algumas informações de contexto como: o raio de alcance
	de transmissão das antenas, a taxa de transmissão dos nós e o tempo em
	que dois nós se mantém em contato, ou janela de contato;

5) Dentro do diretório raiz do simulador:
	-----------------------
	- grease_settings.txt -
	-----------------------
	Possui as configurações utilizadas para executar a simulação do protocolo 
	GREASE (Baseado no Roteamento do Último Encontro - LER)

	-------------------------
	- velosent_settings.txt -
	-------------------------
	Possui as configurações utilizadas para executar a simulação do protocolo 
	VeloSent (Primeira Versão)

	-----------------------------
	- velosentplus_settings.txt -
	-----------------------------
	Possui as configurações utilizadas para executar a simulação do protocolo 
	VeloSentPlus (Segunda Versão)

------------------------
-- ARQUVIOS ALTERADOS --
------------------------

1) O arquivo "core/DTNHost.java" foi agregado de mais alguns métodos:
	---------------------------------
	-- PARA OBTER O CONTEXTO DO NÓ --
	---------------------------------
	1.1) public String getId();		// Retorna a "posição X" do HostDTN
	1.2) public double getLocalTime()	// Retorna o "tempo atual" do HostDTN
	1.3) public double getPosX()		// Retorna a "posição X" do HostDTN
	1.4) public double getPosY()		// Retorna a "posição Y" do HostDTN	
	1.5) public double getSpeedX() 		// Retorna a "velocidade X" do HostDTN
	1.6) public double getSpeedY() 		// Retorna a "velocidade Y" do HostDTN				
	1.7) public double getRange() 		// Retorna o "Alcance da Antena" do HostDTN

	-----------------------------------------------------------------
	-- PARA OBTER/CONFIGURAR DADOS DAS TABELAS DE CONTEXTO DOS NÓS --
	-----------------------------------------------------------------
	1.8) public void setContactLast(	// Atualiza a Tabela de contexto dos últimos contatos efetuados
		String id, double time, 
		double localtime, double px, 
		double py, double sx, 
		double sy)
		
	1.9) public TCUCE getContactLastID(String id)	// Obtém os dados de contexto do último contato 
							   para um determinado nó de destino
	     		 
	1.10) public void updateTCUCEBestAge(String id, // Atualiza a TCUCE Local para uma melhor idade de contato
		TCUCE tcuce, double age)
	
	-------------------------------------
	-- DECLAÇÃO E INSTÂNCIA DE OBJETOS --
	-------------------------------------
	1.11) Foi declarado o objeto "objCM" da classe ContactManager.java, para 
		que seja possível manipular os dados da tabela de contexto do nós DTN;

	1.12) Foi instanciado o "objCM" dentro do construtor da classe "DTNHost"; 

	1.13) Foram comentadas as seguintes importações:
		- import routing.*;
		- import core.velosent.*;

	1.14) Foram adionadas as seguintes importações:
		- import routing.*;
		- import core.velosent.*;


2) O arquivo "routing/ActiveRouter.java" recebeu um novo método: 
	--------------------------
	-- TRANSMITIR MENSAGENS --
	--------------------------
	2.1) protected int tryMessagesForYourConnection(Message m, Connection con); // Permite transmitir mensagens 
										       para determinadas conexões

==================================================================================================================
==================================================================================================================
