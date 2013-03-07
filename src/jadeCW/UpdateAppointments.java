package jadeCW;

import java.util.HashMap;

import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class UpdateAppointments extends CyclicBehaviour {

	public HospitalAgent hospitalAgent;
	HashMap<AID, SwapInfoForHospital> swapStatus;

	public UpdateAppointments(HospitalAgent hospitalAgent) {
		super(hospitalAgent);
		this.hospitalAgent = hospitalAgent;
		swapStatus = new HashMap<AID, SwapInfoForHospital>();
	}

	@Override
	public void action() {

		String conversationId = "inform-swap-to-hospital";
		MessageTemplate reqTemplate = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchConversationId(conversationId));

		ACLMessage propsal = hospitalAgent.receive(reqTemplate);

		if (propsal != null){

			SwapInfoForHospital recievedSwapInfoForHospital = null;
			try {
				recievedSwapInfoForHospital = (SwapInfoForHospital) propsal.getContentObject();
			} catch (UnreadableException e) {

				e.printStackTrace();
			}


			if (swapStatus.containsKey(recievedSwapInfoForHospital.swapWith)){
				if (swapStatus.get(recievedSwapInfoForHospital.swapWith).getCurrentSlot() == recievedSwapInfoForHospital.getSwapSlot() &&
						swapStatus.get(recievedSwapInfoForHospital.swapWith).swapSlot == recievedSwapInfoForHospital.getCurrentSlot()) {

					hospitalAgent.appointments[recievedSwapInfoForHospital.getSwapSlot()] = propsal.getSender();
					hospitalAgent.appointments[recievedSwapInfoForHospital.getCurrentSlot()] = recievedSwapInfoForHospital.getSwapWith();
					swapStatus.remove(recievedSwapInfoForHospital.getSwapWith());
					System.out.println("Hospital has verified the swap and updated itself.");
				} 
				else {
					System.out.println("Something went wrong with hospital processing!");
				}
			}
			else {
				swapStatus.put(propsal.getSender(), recievedSwapInfoForHospital);
			}
		}
	}
}