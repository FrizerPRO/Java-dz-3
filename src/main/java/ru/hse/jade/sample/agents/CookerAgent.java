package ru.hse.jade.sample.agents;

import com.google.gson.reflect.TypeToken;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.behaviour.ReceiveMessageBehaviour;
import ru.hse.jade.sample.behaviour.SendMessageOnce;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.cookers_list.Cooker;
import ru.hse.jade.sample.model.products_on_stock_list.ProductOnStockList;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.min;
import static ru.hse.jade.sample.gson.MyGson.gson;

@JadeAgent()
public class CookerAgent extends Agent implements SetAnnotationNumber {
    Cooker cooker;
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Cooker) {
                cooker = (Cooker) args[0];
            }
        }

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.cookerAgent);
        sd.setName("JADE-test");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new MakeMeWait(this));
    }
    private static class MakeMeWait extends Behaviour {
        CookerAgent cookerAgent;

        public MakeMeWait(CookerAgent cookerAgent) {
            this.cookerAgent = cookerAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.PROCESS_TO_COOKER)) {
                    String json = msg.getContent();
                    Double wait = gson.fromJson(json,Double.class);
                    cookerAgent.cooker.cook_active = true;
                    myAgent.doWait((int)(wait*100));
                    cookerAgent.cooker.cook_active = false;
                }
            } else {
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
