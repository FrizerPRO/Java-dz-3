package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrder;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrdersList;
import ru.hse.jade.sample.model.Error;
import java.util.ArrayList;
import java.util.Objects;

@JadeAgent("MainAgent")
public class MainAgent extends Agent implements SetAnnotationNumber {
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setName(AgentTypes.mainAgent);
        sd.setType(AgentTypes.mainAgent);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new CreateOrderAgent());
    }


    private static class CreateOrderAgent extends Behaviour {
        public static int counter = 0;
        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if(Objects.equals(msg.getOntology(),Ontologies.VISITOR_TO_MAIN)){
                    String json = msg.getContent();
                    VisitorsOrder list = MyGson.gson.fromJson(json, VisitorsOrder.class);
                    ContainerController cnc = myAgent.getContainerController();
                    try {
                        var t = cnc.createNewAgent(AgentTypes.orderAgent + counter,OrderAgent.class.getName(),
                                new Object[]{list,msg.getSender()});
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
