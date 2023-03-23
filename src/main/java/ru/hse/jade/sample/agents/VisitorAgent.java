package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrdersList;

import static ru.hse.jade.sample.gson.MyGson.gson;

@JadeAgent()
public class VisitorAgent extends Agent implements SetAnnotationNumber {
    VisitorsOrdersList visitorsOrder;
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof VisitorsOrdersList) {
                visitorsOrder = (VisitorsOrdersList) args[0];
            }
        }
        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.visitorAgent);
        sd.setName(AgentTypes.visitorAgent);

        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(
                new ru.hse.jade.sample.behaviour.SendMessageOnce(
                        gson.toJson(visitorsOrder),
                        Ontologies.VISITOR_TO_MAIN,
                        AgentTypes.mainAgent,0));
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
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
}
