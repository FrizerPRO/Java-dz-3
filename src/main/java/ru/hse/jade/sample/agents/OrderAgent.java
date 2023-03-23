package ru.hse.jade.sample.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.behaviour.ReceiveMessageBehaviour;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.Error;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrdersList;

import java.util.Objects;

@JadeAgent()
public class OrderAgent extends Agent implements SetAnnotationNumber {
    VisitorsOrdersList visitorsOrder;
    AID visitorAID;

    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof VisitorsOrdersList) {
                visitorsOrder = (VisitorsOrdersList) args[0];
            }
            if (args[1] instanceof AID) {
                visitorAID = (AID) args[1];
            }
        }

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.orderAgent);
        sd.setName(AgentTypes.orderAgent);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new ReceiveMessageBehaviour());
    }
    private static class CreateOrderAgent extends Behaviour {
        public static int counter = 0;
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if(Objects.equals(msg.getOntology(),Ontologies.COOKING_TO_ORDER)){
                    String json = msg.getContent();
                    //Создать мапу
                    VisitorsOrdersList list = MyGson.gson.fromJson(json,VisitorsOrdersList.class);
                    ContainerController cnc = myAgent.getContainerController();
                    try {
                        var t = cnc.createNewAgent(AgentTypes.orderAgent + counter,OrderAgent.class.getName(),
                                new Object[]{list,msg.getSender()});
                        orderAgents.add(t);
                        t.start();
                    } catch (StaleProxyException e) {
                        new Error("Cannot create order agent",e.getMessage(),
                                e.getLocalizedMessage());
                    }
                    counter += 1;
                }
            }else {
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
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
    @Override
    public void setNumber(int number){
        SetAnnotationNumber.super.setNumber(number);
    }
}
