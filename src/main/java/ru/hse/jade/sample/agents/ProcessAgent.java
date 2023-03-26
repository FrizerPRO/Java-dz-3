package ru.hse.jade.sample.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.MainController;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.behaviour.SendMessageOnce;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.model.cookers_list.CookersList;
import ru.hse.jade.sample.model.kitchen_equipment_list.KitchenEquipmentList;
import ru.hse.jade.sample.model.techno_card.DishCard;

import static ru.hse.jade.sample.gson.MyGson.gson;

@JadeAgent()
public class ProcessAgent extends Agent implements SetAnnotationNumber {
    CookersList cookersList;
    KitchenEquipmentList kitchenEquipmentList;
    DishCard dishCard;
    AID orderAID;

    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof DishCard) {
                dishCard = (DishCard) args[0];
            }
            if (args[1] instanceof AID) {
                orderAID = (AID) args[1];
            }
        }

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.processAgent);
        sd.setName("JADE-test");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        findFreeCookerAndEquipment();
        //addBehaviour(new RentCookerAndEquipment(this));
    }

    private void findFreeCookerAndEquipment() {
        boolean isFound = false;
        double waitTime = 0;
        for (var i : dishCard.operations) {
            waitTime += i.oper_time;
        }
        while (!(isFound)) {
            Integer cookerCounter = 0;
            for (var cooker : MainController.cookersList.cookers) {
                if (!cooker.cook_active) {
                    Integer equipCounter = 0;
                    for (var equip : MainController.kitchenEquipmentList.equipment) {
                        if (!equip.equip_active && equip.equip_type == dishCard.equip_type) {
                            addBehaviour(new SendMessageOnce(gson.toJson(dishCard), Ontologies.PROCESS_TO_COOKER,
                                    AgentTypes.cookerAgent, cookerCounter));
                            addBehaviour(new SendMessageOnce(gson.toJson(dishCard), Ontologies.PROCESS_TO_EQUIP,
                                    AgentTypes.equipmentAgent, equipCounter));
                            isFound = true;
                        }
                        equipCounter += 1;
                        addBehaviour(new SendMessageOnce(gson.toJson((int) (waitTime * 100)), Ontologies.PROCESS_TO_ORDER, orderAID));
                    }
                }
                cookerCounter += 1;
            }
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
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }

    private static class RentCookerAndEquipment extends Behaviour {
        ProcessAgent processAgent;

        public RentCookerAndEquipment(ProcessAgent processAgent) {
            this.processAgent = processAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {

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
