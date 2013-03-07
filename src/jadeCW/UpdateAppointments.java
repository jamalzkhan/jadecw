package jadeCW;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class UpdateAppointments extends CyclicBehaviour {

	public HospitalAgent hospitalAgent;
	HashMap<AID, LinkedList<SwapInfoForHospital>> swapStatus;

	public UpdateAppointments(HospitalAgent hospitalAgent) {
		super(hospitalAgent);
		this.hospitalAgent = hospitalAgent;
		swapStatus = new HashMap<AID, LinkedList<SwapInfoForHospital>>();
	}

	@Override
	public void action() {

		String conversationId = "inform-swap-to-hospital";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage propsal = hospitalAgent.receive(reqTemplate);

		if (propsal != null){
			
			AID sender = propsal.getSender();

			SwapInfoForHospital recievedSwapInfoForHospital = null;
			try {
				recievedSwapInfoForHospital = (SwapInfoForHospital) propsal.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}

			if (swapStatus.containsKey(recievedSwapInfoForHospital.swapWith)){
				for (SwapInfoForHospital s : swapStatus.get(recievedSwapInfoForHospital.swapWith)){
					if (s.currentSlot == recievedSwapInfoForHospital.getSwapSlot() &&
							s.swapSlot == recievedSwapInfoForHospital.getCurrentSlot()) {
						
						hospitalAgent.appointments[recievedSwapInfoForHospital.getSwapSlot()] = propsal.getSender();
						hospitalAgent.appointments[recievedSwapInfoForHospital.getCurrentSlot()] = recievedSwapInfoForHospital.getSwapWith();
						
						//swapStatus.remove(recievedSwapInfoForHospital.getSwapWith());
						swapStatus.get(recievedSwapInfoForHospital.swapWith).remove(s);
						System.out.println("Hospital has verified the swap and updated itself.");
						return;
					} 
				}
				
				if (! swapStatus.containsKey(propsal.getSender()))
					swapStatus.put(propsal.getSender(), new LinkedList<SwapInfoForHospital>());
				swapStatus.get(propsal.getSender()).add(recievedSwapInfoForHospital);
								
			}
			else {
				swapStatus.put(propsal.getSender(), new LinkedList<SwapInfoForHospital>());
				swapStatus.get(propsal.getSender()).add(recievedSwapInfoForHospital);
			}
		}
	}
}