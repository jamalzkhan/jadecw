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
			System.out.println("Recieved proposal properly");

			SwapInfo recivedSwapInfo = null;
			try {
				recivedSwapInfo = (SwapInfo) propsal.getContentObject();
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			
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
				reply.setContent(Integer.toString(recivedSwapInfo.currentSlot));;;;;;;;;;;
			}
			else {
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				System.out.println(patientAgent.getName() + " woz allocated this before: " + patientAgent.allocatedAppointment);

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
		patientAgent.send(request);
	}

}
