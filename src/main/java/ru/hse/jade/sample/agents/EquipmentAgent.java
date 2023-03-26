package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.model.cookers_list.Cooker;
import ru.hse.jade.sample.model.kitchen_equipment_list.KitchenEquipment;
import ru.hse.jade.sample.model.techno_card.DishCard;

import java.util.Date;
import java.util.Objects;

import static ru.hse.jade.sample.gson.MyGson.gson;

@JadeAgent()
public class EquipmentAgent extends Agent implements SetAnnotationNumber {
    KitchenEquipment kitchenEquipment;
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof KitchenEquipment) {
                kitchenEquipment = (KitchenEquipment) args[0];
            }
        }

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.equipmentAgent);
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
        EquipmentAgent equipmentAgent;

        public MakeMeWait(EquipmentAgent cookerAgent) {
            this.equipmentAgent = cookerAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.PROCESS_TO_EQUIP)) {
                    String json = msg.getContent();
                    DishCard dishCard = gson.fromJson(json, DishCard.class);
                    double wait = 0.0;
                    for(var i: (dishCard.operations)){
                        wait += i.oper_time;
                    }
                    equipmentAgent.kitchenEquipment.equip_active = true;
                    myAgent.doWait((int)(wait*100000));
                    equipmentAgent.kitchenEquipment.equip_active = false;
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
