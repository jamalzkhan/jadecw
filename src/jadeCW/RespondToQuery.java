package jadeCW;

import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RespondToQuery extends CyclicBehaviour {

	@Override
	public void action() {
		String conversationId = "allocate-appointments";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage request = .receive(reqTemplate);
		
		if (request != null){
			
			
		}
		else{
			block();
		}
		
	}

}
