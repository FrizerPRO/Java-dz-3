package ru.hse.jade.sample.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ru.hse.jade.sample.configuration.JadeAgent;

import java.util.List;

@JadeAgent
public class BookBuyerAgent extends Agent {
    // The title of the book to buy
    private String targetBookTitle;
    // The list of known seller agents
    private List<AID> sellerAgents;

    @Override
    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");
        // Get the title of the book to buy as a start-up argument
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Trying to buy" + targetBookTitle);

            addBehaviour(new TickerBehaviour(this, 60000) {
                @Override
                protected void onTick() {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription serviceDescription = new ServiceDescription();

                    serviceDescription.setType(BookSellerAgent.AGENT_TYPE);
                    template.addServices(serviceDescription);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);

                        for (DFAgentDescription agentDescription: result) {
                            sellerAgents.add(agentDescription.getName());
                        }
                    } catch (FIPAException ex) {
                        ex.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        } else {
            // Make the agent terminate immediately
            System.out.println("No book title specified");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        // Printout a dismissal message
        System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
    }

    private class RequestPerformer extends Behaviour {
        private AID bestSeller;
        private int bestPrice;
        private int repliesCount = 0;
        private MessageTemplate messageTemplate;
        private int step = 0;

        private static final String CONVERSATION_ID = "book-trade";

        @Override
        public void action() {
            switch (step) {
                case 0:
                    ACLMessage cfpMessage = new ACLMessage(ACLMessage.CFP);
                    for (AID agent : sellerAgents) {
                        cfpMessage.addReceiver(agent);
                    }

                    cfpMessage.setContent(targetBookTitle);
                    cfpMessage.setConversationId(CONVERSATION_ID);
                    cfpMessage.setReplyWith("cfp" + System.currentTimeMillis());

                    messageTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(cfpMessage.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(messageTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }

                        ++repliesCount;

                        if (repliesCount >= sellerAgents.size()) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);

                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId(CONVERSATION_ID);
                    order.setReplyWith("order" + System.currentTimeMillis());

                    myAgent.send(order);

                    messageTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith())
                    );

                    step = 3;
                    break;
                case 3:
                    reply = myAgent.receive(messageTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(targetBookTitle + " successfully purchased");
                            System.out.println("Price = " + bestPrice);
                            myAgent.doDelete();
                        }

                        step = 4;
                    } else {
                        block();
                    }

                    break;
            }
        }

        @Override
        public boolean done() {
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}
