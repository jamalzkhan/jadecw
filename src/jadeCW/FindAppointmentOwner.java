package jadeCW;

import java.util.HashMap;
import java.util.HashSet;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class FindAppointmentOwner extends Behaviour {

	public int state = 0;
	private PatientAgent patientAgent;
	private HashMap<Integer, HashSet<Integer>> higherPriorities;
	private int assignedPriority;
	private MessageTemplate reqTemplate;
	
	/*
	 * Behaviour is used to find a patient with higher priority 
	 * and request if it is willing to swap
	 * 
	 * We're using states again here:
	 *  - State 0: Used to find a more preferred slot and send it's
	 *  		owner a request
	 *  - State 1: Receive a confirmation who owns this resource
	 *  - State 2: Stop the behaviour
	 */

	public FindAppointmentOwner(PatientAgent patientAgent){
		super(patientAgent);
		this.patientAgent = patientAgent;
	}

	@Override
	public void action() {
		switch (state) {
		case 0:
			requestPreferredAppointment();
			break;
		case 1:
			try {
				getPreferredAppointmentConfirmation();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private int getNextTimeSlot(){

		for (int i = 0; i<assignedPriority; i++){
			higherPriorities.get(i);
		}

		return -1;
	}

	public void requestPreferredAppointment(){
		
		if (patientAgent.highPriorityAppointmentOwner != null)
			return;
		
		if (patientAgent.allocatedAppointment == -2)
			return;
		
		
		this.assignedPriority = patientAgent.preferedAppointmentPriority();

		if (assignedPriority == -1)
			assignedPriority = patientAgent.preferences.size();

		//Highest priority reached!
		if (assignedPriority == 0){
			state = 2;
			return;
		}

		higherPriorities = new HashMap<Integer, HashSet<Integer>>();
		for (int i = 0; i< assignedPriority; i++){
			higherPriorities.put(i, patientAgent.preferences.get(i));
		}

		Integer nextSlot = null;
		for (Integer i : higherPriorities.keySet()){
			HashSet<Integer> prefs = higherPriorities.get(i);
			for (Integer j : prefs){
				if (!patientAgent.excluded.contains(j)){
					nextSlot = j;
					break;
				}
			}
		}

		// If all have been checked then we empty the excluded set and regenerate it with an empty set
		if (nextSlot == null){
			patientAgent.swappingAttempts++;
						
			if (patientAgent.swappingAttempts == 3){
				state = 2;
				return;
			}

			patientAgent.excluded.clear();
			state = 0;
			return;
		}

		String conversationId = "find-appointment-owner";

		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(patientAgent.allocationAgent);
		request.setContent(nextSlot.toString());
		System.out.println(patientAgent.getName() + " requesting slot " + Integer.toString((nextSlot+1)));
		patientAgent.swapSlot = nextSlot;
		request.setSender(patientAgent.getAID());
		request.setConversationId(conversationId);
		request.setReplyWith(conversationId + " " + System.currentTimeMillis());
		patientAgent.send(request);
		reqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
				MessageTemplate.MatchInReplyTo(request.getReplyWith())); 
		state = 1;

	}

	public void getPreferredAppointmentConfirmation() throws UnreadableException{
		ACLMessage reply = patientAgent.receive(reqTemplate);
		
		if (reply != null) {
			if (!reqTemplate.match(reply)) {
				System.err.println("Message template doesn't match!");
			} else {
				if (reply.getPerformative() == ACLMessage.FAILURE) {
					System.out.println(reply.getContent());
					state = 0;
				} else {
					AID resourceOwner = (AID) reply.getContentObject();
					patientAgent.highPriorityAppointmentOwner = resourceOwner;
					System.out.println("Slot " + (patientAgent.swapSlot+1) + " owned by " + resourceOwner.getName());
					patientAgent.addBehaviour(new ProposeSwap(patientAgent));
					state = 2;
				}
			}
		} else {
			block();
		}
	}

	@Override
	public boolean done() {
		return false;
	}

}
