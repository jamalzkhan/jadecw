package jadeCW;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

public class FindAppointmentOwner extends Behaviour {

	public int step = 0;
	private PatientAgent patientAgent;

	public FindAppointmentOwner(PatientAgent patientAgent){
		super(patientAgent);
		this.patientAgent = patientAgent;
	}

	@Override
	public void action() {

		requestPreferredAppointment();

	}

	public void requestPreferredAppointment(){
		// Check if there is an appointment or if the appointment is not preferred
		if (!this.patientAgent.hasAppointment || !this.patientAgent.hasPreferedAppointment()){
			// Query the hospital agent about the getting an appointment with a preference
			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
			request.addReceiver(patientAgent.allocationAgent);
			
		}
	}
	
	public void getPreferredAppointmentRequest(){
		
		
	}



	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
