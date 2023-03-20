package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import ru.hse.jade.sample.behaviour.ReceiveMessageBehaviour;
import ru.hse.jade.sample.configuration.JadeAgent;

@JadeAgent(number = 5)
public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("test-squad");
        sd.setName("JADE-test");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new ReceiveMessageBehaviour());
    }

    @Override
    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Print out a dismissal message
        System.out.println("testAgent " + getAID().getName() + " terminating");
    }

}
