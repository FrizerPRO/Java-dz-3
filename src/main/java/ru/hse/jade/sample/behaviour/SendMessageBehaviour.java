package ru.hse.jade.sample.behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.model.Person;
import ru.hse.jade.sample.util.JsonMessage;

public class SendMessageBehaviour extends Behaviour {

    private final Person message;
    private final AID[] recipients;

    public SendMessageBehaviour(AID[] recipients, Person message) {
        this.recipients = recipients;
        this.message = message;
    }

    @Override
    public void action() {
        JsonMessage cfp = new JsonMessage(ACLMessage.CFP);
        for (AID recipient : recipients) {
            cfp.addReceiver(recipient);
        }
        cfp.setContent(message);
        myAgent.send(cfp);
    }

    @Override
    public boolean done() {
        return true;
    }
}
