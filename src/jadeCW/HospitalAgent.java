package jadeCW;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.Property;

public class HospitalAgent extends Agent {

	public void setup(){

		int totalAppointments = 0;
		
	  	Object[] args = getArguments();
	  	if (args != null && args.length > 0) {
	  		totalAppointments = Integer.parseInt((String) args[0]);
	  	}
	  	
        String serviceName = "appointment-allocator";
        String serviceType = "allocate-appointments";
	  	
	  	// Register the service
	  	System.out.println("Agent "+getLocalName()+" registering service \"" +serviceName
	  			+"\" of type \"allocate-appointments\"");
	  	try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName(serviceName);
	  		sd.setType(serviceType);
	  		// Agents that want to use this service need to "know" the weather-forecast-ontology
	  		//sd.addOntologies("weather-forecast-ontology");
	  		// Agents that want to use this service need to "speak" the FIPA-SL language
	  		sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
	  		//sd.addProperties(new Property("country", "Italy"));
	  		dfd.addServices(sd);
	  		
	  		DFService.register(this, dfd);
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
		
	}

}
