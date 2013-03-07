package jadeCW;

import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AllocateAppointment extends CyclicBehaviour {

	private HospitalAgent hospitalAgent;
	
	/*
	 * This behaviour receives requests messages and allocates slots
	 * The first request to come in gets the first slot, no preferences 
	 * are taken into account at this stage
	 * 
	 */
	
	public AllocateAppointment(HospitalAgent hospitalAgent) {
		super(hospitalAgent);
		this.hospitalAgent = hospitalAgent;
	}

	@Override
	public void action() {
		String conversationId = "allocate-appointments";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage request = hospitalAgent.receive(reqTemplate);
		
		if (request != null){
			AID patientAgent = request.getSender();
			int timeSlot = allocateAppointment(new HashMap<Integer, HashSet<Integer>>(), patientAgent);
			ACLMessage reply = request.createReply();
			
			if (timeSlot == -1){
				reply.setPerformative(ACLMessage.REFUSE);
			}
			else {
				reply.setPerformative(ACLMessage.AGREE);
				reply.setContent(Integer.toString(timeSlot));
			}
			hospitalAgent.send(reply);
			
		}
		else{
			block();
		}
	}
	
	public int allocateAppointment(HashMap<Integer, HashSet<Integer>> preferences, AID agent){
		for (int i = 0; i<hospitalAgent.getAppointments().length; i++){
			if (hospitalAgent.getAppointments()[i] == null){
				hospitalAgent.getAppointments()[i] = agent;
				return i;
			}
		}
		return -1;
	}

}
