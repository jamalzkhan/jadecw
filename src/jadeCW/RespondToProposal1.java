package jadeCW;

import java.io.IOException;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class RespondToProposal1 extends CyclicBehaviour {

	PatientAgent patientAgent;
	
	/*
	 * Checks if the slot recieved has a better preference or similar one and then sends a message
	 * to swap, or it sends a rejection to the proposal made.
	 * 
	 * Also sends a message to the hospital to confirm the swap change.
	 */

	public RespondToProposal1(PatientAgent patientAgent) {
		super(patientAgent);
		this.patientAgent = patientAgent;
	}

	@Override
	public void action() {

		String conversationId = "request-swap";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage propsal = patientAgent.receive(reqTemplate);

		if (propsal != null){
			System.out.println(patientAgent.getName() + " recieved proposal from " + propsal.getSender().getName());

			SwapInfo recivedSwapInfo = null;
			try {
				recivedSwapInfo = (SwapInfo) propsal.getContentObject();
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			
			// add check if the receivedSwapInfo.swap doesn't match the patientAgent.allocated - EXIT THIS

			
			ACLMessage reply = propsal.createReply();
			try {
				reply.setContentObject(new SwapInfo(patientAgent.allocatedAppointment, recivedSwapInfo.currentSlot));
			} catch (IOException e) {
				e.printStackTrace();
			}
			//reply.setContent(Integer.toString(patientAgent.allocatedAppointment));

			if (patientAgent.getCurrentPriority() < 
					patientAgent.getPriorityOfTimeSlot(recivedSwapInfo.currentSlot)) {
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			} else if (recivedSwapInfo.swapSlot != patientAgent.allocatedAppointment) {
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				System.out.println("Check if there has recently been a swap!");
			}
			else if (propsal.getSender() == patientAgent.getAID()){
				System.out.println("I am asking myself to swap!");
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
			}
			else {
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				System.out.println(patientAgent.getName() + " was allocated this before: " + (patientAgent.allocatedAppointment+1) + " and now has " + (recivedSwapInfo.getCurrentSlot()+1));

				try {
					informHospital(propsal.getSender(), patientAgent.allocatedAppointment, recivedSwapInfo.currentSlot);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				patientAgent.allocatedAppointment = recivedSwapInfo.currentSlot;
			}
			patientAgent.send(reply);
		}
		else{
			block();
		}	
	}

	private void informHospital(AID swapPatientAgentAID, int currentSlot, int swapSlot) throws IOException{
		String conversationId = "inform-swap-to-hospital";
		ACLMessage request = new ACLMessage(ACLMessage.INFORM);
		request.addReceiver(patientAgent.allocationAgent);
		request.setSender(patientAgent.getAID());
		request.setContentObject(new SwapInfoForHospital(currentSlot, swapSlot, swapPatientAgentAID));
		request.setConversationId(conversationId);
		request.setReplyWith(conversationId + " " + System.currentTimeMillis());
		System.out.println(patientAgent.getAID().getName() + " sending confirmation to hospital");
		patientAgent.send(request);
	}

}
