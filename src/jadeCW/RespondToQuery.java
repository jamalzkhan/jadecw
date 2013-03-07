package jadeCW;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RespondToQuery extends CyclicBehaviour {

	private HospitalAgent hospitalAgent;
	
	public RespondToQuery(HospitalAgent hospitalAgent) {
		super(hospitalAgent);
		this.hospitalAgent = hospitalAgent;
	}

	@Override
	public void action() {
		String conversationId = "find-appointment-owner";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage request = hospitalAgent.receive(reqTemplate);
		
		if (request != null){
			
			AID patientAgent = request.getSender();
			Integer requestedSlot = Integer.parseInt(request.getContent());
			ACLMessage reply = request.createReply();
			
			if (requestedSlot > hospitalAgent.appointments.length) {
				// fail: slot not existing
				// send rejection
				reply.setPerformative(ACLMessage.FAILURE);
				reply.setContent("Slot not existent");
				
			} else {

				AID owner = hospitalAgent.appointments[requestedSlot];
				reply.setPerformative(ACLMessage.INFORM);

				if (owner == null) {
					//slot free
					//send that it's free
					try {
						reply.setContentObject(hospitalAgent.getAID());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					//slot taken
					//send name
					try {
						reply.setContentObject(owner);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			hospitalAgent.send(reply);		
			
		}
		else{
			block();
		}
		
	}

}
