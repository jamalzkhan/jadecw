package jadeCW;

import java.io.IOException;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class RespondToProposal2 extends CyclicBehaviour {
	
	public HospitalAgent hospitalAgent;
	
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
			System.out.println("Hospital recieved proposal properly for " + propsal.getContent());

			SwapInfo recivedSwapInfo = null;
			try {
				recivedSwapInfo = (SwapInfo) propsal.getContentObject();
			} catch (UnreadableException e1) {
				e1.printStackTrace();
			}
			
			ACLMessage reply = propsal.createReply();
			try {
				reply.setContentObject(new SwapInfo(recivedSwapInfo.swapSlot, recivedSwapInfo.currentSlot));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (this.hospitalAgent.appointments[recivedSwapInfo.swapSlot] == null){
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				hospitalAgent.appointments[recivedSwapInfo.swapSlot] = propsal.getSender();
				hospitalAgent.appointments[recivedSwapInfo.currentSlot] = null;
				System.out.println("Hospital agreed to swap");
			}
			else {
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				System.out.println("Hospital rejected swap");
			}

			hospitalAgent.send(reply);
		}
		else{
			block();
		}	
		
	}

}
