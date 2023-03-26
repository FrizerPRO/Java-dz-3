package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.ProcessLogger;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.model.agent_of_cookicng_process.CookingProcessLog;
import ru.hse.jade.sample.model.agent_of_cookicng_process.CookingProcessOperation;
import ru.hse.jade.sample.model.cookers_list.Cooker;
import ru.hse.jade.sample.model.techno_card.DishCard;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
        sd.setName(AgentTypes.cookerAgent);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new MakeMeWait(this));
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

    private static class MakeMeWait extends Behaviour {
        public static AtomicInteger counter = new AtomicInteger(0);

        CookerAgent cookerAgent;

        public MakeMeWait(CookerAgent cookerAgent) {
            this.cookerAgent = cookerAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.PROCESS_TO_COOKER)) {
                    CookingProcessLog log = new CookingProcessLog();
                    log.proc_id = counter.get();
                    counter.addAndGet(1);

                    String json = msg.getContent();
                    DishCard dishCard = gson.fromJson(json, DishCard.class);
                    log.proc_active = false;
                    log.proc_started = new Date();
                    double wait = 0.0;
                    for (var i : (dishCard.operations)) {
                        wait += i.oper_time;
                    }
                    cookerAgent.cooker.cook_active = true;
                    myAgent.doWait((int) (wait * 100000));
                    log.proc_ended = new Date();
                    log.ord_dish = dishCard.card_id;
                    log.proc_operations = new ArrayList<>();
                    for (var i : dishCard.operations) {
                        var cookingOper = new CookingProcessOperation();
                        cookingOper.proc_oper = i.oper_type;
                        log.proc_operations.add(cookingOper);
                    }
                    ProcessLogger.logger.fine(gson.toJson(log));
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
}
