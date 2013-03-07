package jadeCW;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ProposeSwap extends Behaviour {

	PatientAgent patientAgent;
	private int state;
	MessageTemplate reqTemplate;

	/*
	 * Use to send swap request and recieve the result, using states again:
	 *  - State 0: Request a swap from a higher preference slot owner
	 *  - State 1: Receives a reply for the request and updates the agent state
	 *  	Informs the hospitalAgent that a swap was made
	 * 	- State 2: Stop the behaviour 
	 */

	public ProposeSwap(PatientAgent patientAgent) {
		super(patientAgent);
		this.patientAgent = patientAgent;
		state = 0;
	}

	@Override
	public void action() {
		switch (state) {
		case 0:
			requestSwapAppointments();
			break;
		case 1:
			try {
				recieveSwapAppointmentReply();
			} catch (UnreadableException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	@Override
	public boolean done() {
		return false;
	}


	public void recieveSwapAppointmentReply() throws UnreadableException, IOException{

		ACLMessage reply = patientAgent.blockingReceive(reqTemplate);

		if (reply != null) {

			SwapInfo recievedSwapInfo = (SwapInfo) reply.getContentObject();
			if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
				System.out.println(patientAgent.getName() + " proposal was accepted, now swapping...");	
				System.out.println(patientAgent.getName() + " was allocated this before: " 
						+ (patientAgent.allocatedAppointment+1) 
						+ " and now has " + (recievedSwapInfo.getCurrentSlot()+1));


				informHospital(reply.getSender(), patientAgent.allocatedAppointment, recievedSwapInfo.currentSlot);

				patientAgent.allocatedAppointment = recievedSwapInfo.currentSlot;//Integer.parseInt(reply.getContent());
				patientAgent.highPriorityAppointmentOwner = null;

				state = 2;
			}
			else {
				System.out.println(patientAgent.getName() + "'s proposal was rejected");				
				patientAgent.excluded.add(recievedSwapInfo.currentSlot);
				patientAgent.highPriorityAppointmentOwner = null;
				patientAgent.swapSlot = -1;
				state = 2;
			}
			patientAgent.swapSlot = -1;
			patientAgent.addBehaviour(new FindAppointmentOwner(patientAgent));
		} else {
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

	public void requestSwapAppointments(){
		String conversationId = "request-swap";

		if (patientAgent.highPriorityAppointmentOwner != null){

			System.out.println(patientAgent.getName() + " proposing to swap slot " 
					+ (patientAgent.allocatedAppointment+1) + " with " + (patientAgent.swapSlot+1));
			ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
			request.addReceiver(patientAgent.highPriorityAppointmentOwner);
			try {
				request.setContentObject(new SwapInfo(patientAgent.allocatedAppointment, patientAgent.swapSlot));
			} catch (IOException e) {
				e.printStackTrace();
			}
			request.setSender(patientAgent.getAID());
			request.setConversationId(conversationId);
			request.setReplyWith(conversationId + " " + System.currentTimeMillis());
			patientAgent.send(request);
			reqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationId),
					MessageTemplate.MatchInReplyTo(request.getReplyWith())); 
			state = 1;

		}
	}

}
