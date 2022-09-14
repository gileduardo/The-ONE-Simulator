/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.ConnectionListener;
import core.SimReport;

public class PreviewMarginError extends Report {

	// Objeto para gerar Relatório Margem de Erro - Previsão Posição
	private static SimReport objSimReport;

	/**
	 * Constructor.
	 */
	public PreviewMarginError() { init(); }

	@Override
	protected void init() { 
		super.init();
	}

	@Override
	public void done() {

		String statsText;

		write("Preview Destination Location - Average Error: [" + getScenarioName() + "]");
		/* 	0 < distancia > 200:		d1
			200 < distancia > 400:		d2
			400 < distancia > 600:		d3
			600 < distancia > 800:		d4
			800 < distancia :			d5
		*/		
		statsText = "D[0-200]: " + objSimReport.getInstance().getRangeError(1, 0) + 
			"\nD[200-400]: " + objSimReport.getInstance().getRangeError(2, 0) + 
			"\nD[400-600]: " + objSimReport.getInstance().getRangeError(3, 0) + 
			"\nD[600-800]: " + objSimReport.getInstance().getRangeError(4, 0) + 
			"\nD[800-mais]: " + objSimReport.getInstance().getRangeError(5, 0) + 
			"\n---------------";
		
		write(statsText);
		/* 	0 < tempo > 30:		d1
			30 < tempo > 60:		d2
			60 < tempo > 90:		d3
			90 < tempo > 120:		d4
			120 < tempo :		d5
		*/
		statsText = "T[0-60]: " + objSimReport.getInstance().getRangeError(1, 1) + 
			"\nT[60-120]: " + objSimReport.getInstance().getRangeError(2, 1) + 
			"\nT[120-180]: " + objSimReport.getInstance().getRangeError(3, 1) + 
			"\nT[180-240]: " + objSimReport.getInstance().getRangeError(4, 1) + 
			"\nT[240-mais]: " + objSimReport.getInstance().getRangeError(5, 1); 

		write(statsText);

		super.done();
	}
}
