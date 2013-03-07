package jadeCW;

import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AllocateAppointment extends CyclicBehaviour {

	private HospitalAgent hospitalAgent;
	
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
		for (int i = 0; i<hospitalAgent.appointments.length; i++){
			if (hospitalAgent.appointments[i] == null){
				hospitalAgent.appointments[i] = agent;
				return i;
			}
		}
		return -1;
	}

}
