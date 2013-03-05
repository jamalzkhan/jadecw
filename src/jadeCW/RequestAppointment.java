package jadeCW;

import java.io.IOException;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestAppointment extends Behaviour {
	
	private int step = 0;
	private PatientAgent patientAgent;
	private MessageTemplate reqTemplate;
	
	public RequestAppointment(PatientAgent agent) {
		super(agent);
		this.patientAgent = agent;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		switch (step) {
			case 0:
				sendRequest();
				break;
			case 1:
				receiveReply();
				break;
		}
		step++;
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

	private void sendRequest() {
		if (patientAgent.allocationAgent != null) {
			System.out.println("performing first action on agent " + patientAgent.getName());
			if (!patientAgent.hasAppointment) {
				ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
				request.addReceiver(patientAgent.allocationAgent);
				try {
					request.setContentObject(patientAgent.preferences);
				} catch (IOException e) {
					e.printStackTrace();
				}
				request.setConversationId("allocate-appointments");
				request.setReplyWith("allocate-appointments" + " " + System.currentTimeMillis());
				patientAgent.send(request);
				reqTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("allocate-appointments"),
						MessageTemplate.MatchInReplyTo(request.getReplyWith())); 
				step = 1;
			}
		}
	}
	
	private void receiveReply() {
		ACLMessage reply = patientAgent.receive();
		
		if (reply != null) {
			if (!reqTemplate.match(reply)) {
				System.out.println("Message template doesn't match!");
			} else if (reply.getPerformative() == ACLMessage.AGREE) {
				patientAgent.allocatedAppointment = Integer.parseInt(reply.getContent());
				step = 0;
			} else if (reply.getPerformative() == ACLMessage.REFUSE) {
				patientAgent.allocatedAppointment = -1;	
			}
		} else {
			block();
		}
		
	}
	
}
