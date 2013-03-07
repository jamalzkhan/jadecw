package jadeCW;

import java.io.IOException;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestAppointment extends Behaviour {
	
	private int state = 0;
	private PatientAgent patientAgent;
	private MessageTemplate reqTemplate;
	
	/*
	 * The behaviour has different states:
	 *  - State 0: Sending the request for the appointment
	 *  - State 1: Waiting for the reply from the appointment owner
	 *  - State 2: Behaviour is finished
	 *  
	 */
	
	public RequestAppointment(PatientAgent agent) {
		super(agent);
		this.patientAgent = agent;
	}

	@Override
	public void action() {
		switch (state) {
			case 0:
				sendRequest();
				break;
			case 1:
				receiveReply();
				break;
		}
	}

	@Override
	public boolean done() {
		return false;
	}

	private void sendRequest() {
		if (patientAgent.allocationAgent != null) {
			if (!patientAgent.hasAppointment) {
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.addReceiver(patientAgent.allocationAgent);
				try {
					request.setContentObject(patientAgent.preferences);
				} catch (IOException e) {
					e.printStackTrace();
				}
				request.setSender(patientAgent.getAID());
				request.setConversationId("allocate-appointments");
				request.setReplyWith("allocate-appointments" + " " + System.currentTimeMillis());
				patientAgent.send(request);
				reqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("allocate-appointments"),
						MessageTemplate.MatchInReplyTo(request.getReplyWith())); 
				state = 1;
			}
		}
	}
	
	private void receiveReply() {
		ACLMessage reply = patientAgent.receive(reqTemplate);
		
		if (reply != null) {
			if (!reqTemplate.match(reply)) {
				System.err.println("Message template doesn't match!");
			} else if (reply.getPerformative() == ACLMessage.AGREE) {
				patientAgent.allocatedAppointment = Integer.parseInt(reply.getContent());
				patientAgent.hasAppointment = true;
				patientAgent.addBehaviour(new FindAppointmentOwner(patientAgent));
				state = 2;
			} else if (reply.getPerformative() == ACLMessage.REFUSE) {
				patientAgent.allocatedAppointment = -1;	
				state = 0;
			}
			
		} else {
			block();
		}
		
	}
	
}
