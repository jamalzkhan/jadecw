package jadeCW;

import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Property;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.Iterator;

public class PatientAgent extends Agent {
	
	private HashMap<Integer, HashSet<Integer>> preferences 
						= new HashMap<Integer, HashSet<Integer>>();

	public void setup(){

		int totalAppointments = 0;
		
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		
	  		int count = 0;
	  		preferences.put(count, new HashSet<Integer>());
	  		
	  		for (int i=0; i<args.length; i++) {
	  			if (args[i].equals("-")) {
	  				count++;
	  				preferences.put(count, new HashSet<Integer>());
	  			}
	  			else
	  				preferences.get(count).add(Integer.parseInt((String) args[i]));
	  		}
	  		
	  	}
	  	
        final String serviceType = "allocate-appointments"; 

		// Build the description used as template for the subscription
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription templateSd = new ServiceDescription();
		templateSd.setType(serviceType);
		template.addServices(templateSd);
		
		SearchConstraints sc = new SearchConstraints();
		// We want to receive 10 results at most
		//sc.setMaxResults(new Long(10));
  		
		addBehaviour(new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, sc)) {
			protected void handleInform(ACLMessage inform) {
  			System.out.println("Agent "+getLocalName()+": Notification received from DF");
  			try {
				DFAgentDescription[] results = DFService.decodeNotification(inform.getContent());
		  		if (results.length > 0) {
		  			for (int i = 0; i < results.length; ++i) {
		  				DFAgentDescription dfd = results[i];
		  				AID provider = dfd.getName();
		  				// The same agent may provide several services; we are only interested
		  				// in the weather-forcast one
		  				Iterator it = dfd.getAllServices();
		  				while (it.hasNext()) {
		  					ServiceDescription sd = (ServiceDescription) it.next();
		  					if (sd.getType().equals(serviceType)) {
	  							System.out.println("Allocate appointment service found:");
		  						System.out.println("- Service \""+sd.getName()+"\" provided by agent "+provider.getName());
		  					}
		  				}
		  			}
		  		}	
	  			System.out.println();
		  	}
		  	catch (FIPAException fe) {
		  		fe.printStackTrace();
		  	}
			}
		} );

		
	}
	
}