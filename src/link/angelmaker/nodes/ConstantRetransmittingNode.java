package link.angelmaker.nodes;

public class ConstantRetransmittingNode {
	//TODO better name
	//TODO more like a manager, or combination of manager / node
	
	/*
	 * Heeft 2 queues. in/out
	 * Heeft ook X Nodes 'in geheugen'
	 * Stuurt Y data bits per Node.
	 * 
	 * Als er X frames achter elkaar raar doen, en de retransmits gaan mis zal het geheel mis blijven lopen zolang de queue gevuld is.
	 * Deze kans kan erg klein gemaakt worden en zou moeten resulteren in een packet drop op network.
	 * 
	 * 
	 * SENDING:
	 * while(true){
		 * Vanuitgaande normale situatie, lastSent = x
		 * while(allesprima){
			 * if(lastSent==loadNew){
				 * Haal max Y (node lengte) data uit queue. (kan dus ook niks zijn, dan wordt enkel de flags verstuurd, dus filler)
				 * stuff de data.
				 * Voeg seq en messageToSent toe.
				 * Voeg start en end flag toe.
				 * Zet deze gehele node op plek (lastSent+1)%X in geheugen.
			 * }
			 * Stuur (lastSent+1)%X. Deze heeft seq nummer en message, zoals RetransmittingNode dat ook heeft.
			 * lastSent = (lastSent+1)%X;
			 * if(lastSent = (loadNew+1)%X){
				 * loadNew = lastSent.
			 * }
		 * }
		 * Als laatste message geen 'ack' is.
		 * lastSent = message;
	 * }
	 * 
	 * 
	 * RECEIVING:
	 * Vanuitgaand normale situatie, lastReceivedCorrect = x.
	 * while(true){
		 * Vraag nieuwe node aan (vul een node met de stream).
		 * unflag de node, splits
		 * Check of verwachte lengte.
		 * Check of correct.
		 * Check of seq== (lastReceivedCorrect+1)%X.
		 * if(fullyCorrect){
			 * decode data, stop data in queueOut.
			 * lastReceivedCorrect = (lastReceivedCorrect+1)%X;
			 * messageReceived = haalMessageUitNode(node);
			 * messageToSent = prima;
		 * }else if(alleenSeqIsFout){
			 *  messageReceived = haalMessageUitNode(node);
			 *  messageToSent = lastReceivedCorrect;
		 *  }else{
			 *  messageToSent = lastReceivedCorrect;
			 *  //Ignore, dont even update messageReceived, assume sending went ok.
			 *  
			 *  
			 *  //TODO is dit een fatsoenlijke assumption? Je zou ook kunnen zeggen,
			 *  //	fout ontvangen hier is waarschijnlijk fout ontvangen daar.
			 *  //	Dit zou betekenen messageReceived = lastSent--; Punt is dat frame/node lengtes kunnen verschillen.
			 *  //	Dit zou optimalisatie zijn.	 *  
		 *  }
	 *  }
	 * 
	 */
	
	
}
