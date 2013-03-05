package jadeCW;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AllocateAppointment extends CyclicBehaviour {

	private HospitalAgent hospitalAgent;
	private int step = 0;
	
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
		
		if (reqTemplate != null){
			AID patientAgent = request.getSender();
			int timeSlot = allocateAppointment(new HashMap<Integer, HashSet<Integer>>(), patientAgent);
			ACLMessage reply; 
			
			if (timeSlot == -1){
				reply =  new ACLMessage(ACLMessage.REFUSE);
			}
			else {
				reply = new ACLMessage(ACLMessage.AGREE);
				try {
					reply.setContentObject(timeSlot);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			reply.setConversationId(request.getConversationId());
			reply.setReplyWith(request.getReplyWith());
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
