package jadeCW;

import java.io.IOException;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class RespondToProposal2 extends CyclicBehaviour {
	
	public HospitalAgent hospitalAgent;
	
	/*
	 * Checks if the slot recieved is non existant in the appointment structure,
	 * swaps if it is, otherwise sends a rejection message
	 * 
	 */
	
	public RespondToProposal2(HospitalAgent hospitalAgent) {
		super();
		this.hospitalAgent = hospitalAgent;
	}

	@Override
	public void action() {
		String conversationId = "request-swap";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage propsal = hospitalAgent.receive(reqTemplate);
		
		if (propsal != null){
			

			SwapInfo recivedSwapInfo = null;
			try {
				recivedSwapInfo = (SwapInfo) propsal.getContentObject();
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			System.out.println(hospitalAgent.getName() + " recieved proposal for " + recivedSwapInfo.swapSlot);
			
			ACLMessage reply = propsal.createReply();
			try {
				reply.setContentObject(new SwapInfo(recivedSwapInfo.swapSlot, recivedSwapInfo.currentSlot));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (this.hospitalAgent.getAppointments()[recivedSwapInfo.swapSlot] == null){
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				
				int agentSlot = hospitalAgent.getSlotForAID(propsal.getSender());
				
				hospitalAgent.getAppointments()[recivedSwapInfo.swapSlot] = propsal.getSender();
				
				// Check if there is another assignment going on and remove it to this new one
				hospitalAgent.getAppointments()[recivedSwapInfo.currentSlot] = null;
				
				System.out.println(hospitalAgent.getName() + " agreed to swap and updated");
			}
			else {
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				System.out.println("Hospital rejected swap");
			}
			reply.setSender(hospitalAgent.getAID());
			hospitalAgent.send(reply);
		}
		else{
			block();
		}	
		
	}

}
