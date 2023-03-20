package ru.hse.jade.sample.behaviour;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.model.Person;
import ru.hse.jade.sample.util.ACLMessageUtil;

public class ReceiveMessageBehaviour extends Behaviour {
    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            System.out.println("Received: " + ACLMessageUtil.getContent(msg, Person.class));
        }
        else {
            block();
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
