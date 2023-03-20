package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ru.hse.jade.sample.configuration.JadeAgent;

import java.util.HashMap;
import java.util.Map;

@JadeAgent
public class BookSellerAgent extends Agent {

    private Map<String, Integer> catalogue = new HashMap<>();

    public static final String AGENT_TYPE = "book-selling";

    public BookSellerAgent() {
        catalogue.put("Тест", 5);
    }

    @Override
    protected void setup() {
        catalogue = new HashMap<>();

        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(getAID());

        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(AGENT_TYPE);
        serviceDescription.setName("JADE-book-trading");

        agentDescription.addServices(serviceDescription);

        try {
            DFService.register(this, agentDescription);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Printout a dismissal message
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    private class OfferRequestsServer extends CyclicBehaviour {

        @Override
        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(messageTemplate);

            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = catalogue.get(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent("not-available");
                }

                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {

        public void action() {
            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(messageTemplate);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = catalogue.remove(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title + " sold to agent " + msg.getSender().getName());
                } else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}
