package jadeCW;

import java.io.IOException;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ProposeSwap extends Behaviour {

	PatientAgent patientAgent;
	private int step;
	MessageTemplate reqTemplate;
	//private int swapSlot;
	
	public ProposeSwap(PatientAgent patientAgent) {
		super(patientAgent);
		this.patientAgent = patientAgent;
		step = 0;
		//this.swapSlot = -1;
	}
	
	@Override
	public void action() {
		switch (step) {
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
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public void recieveSwapAppointmentReply() throws UnreadableException, IOException{
		String conversationId = "request-swap";
		
		ACLMessage reply = patientAgent.blockingReceive(reqTemplate);

		if (reply != null) {
			
			SwapInfo recievedSwapInfo = (SwapInfo) reply.getContentObject();
			if (reply.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
				System.out.println(patientAgent.getName() + " proposal was accepted");				
				informHospital(reply.getSender(), patientAgent.allocatedAppointment, recievedSwapInfo.currentSlot);
				
				System.out.println(patientAgent.getName() + " was allocated this before: " + patientAgent.allocatedAppointment + " and now has " + recievedSwapInfo.getCurrentSlot());
				
				patientAgent.allocatedAppointment = recievedSwapInfo.currentSlot;//Integer.parseInt(reply.getContent());
				patientAgent.highPriorityAppointmentOwner = null;
				
				step = 2;
			}
			else {
				System.out.println(patientAgent.getName() + " proposal was rejected");				
				patientAgent.excluded.add(recievedSwapInfo.currentSlot);
				patientAgent.highPriorityAppointmentOwner = null;
				step = 2;
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
		
		//request.setContentObject(swapPatientAgentAID);
		request.setContentObject(new SwapInfoForHospital(currentSlot, swapSlot, swapPatientAgentAID));
		request.setConversationId(conversationId);
		request.setReplyWith(conversationId + " " + System.currentTimeMillis());
		patientAgent.send(request);
	}
	
	public void requestSwapAppointments(){
		String conversationId = "request-swap";

		if (patientAgent.highPriorityAppointmentOwner != null){
			
			System.out.println(patientAgent.getName() + " proposing to swap slot" 
			+ patientAgent.allocatedAppointment + " with " + patientAgent.swapSlot);
			ACLMessage request = new ACLMessage(ACLMessage.PROPOSE);
			request.addReceiver(patientAgent.highPriorityAppointmentOwner);
			//request.setContent(Integer.toString(patientAgent.allocatedAppointment));
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
			step = 1;
			
		}
	}

}
