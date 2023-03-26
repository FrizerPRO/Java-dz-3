package ru.hse.jade.sample.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.LifeCycle;
import jade.core.MainContainer;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.State;
import ru.hse.jade.sample.Main;
import ru.hse.jade.sample.MainController;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.behaviour.ReceiveMessageBehaviour;
import ru.hse.jade.sample.behaviour.SendMessageOnce;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.Error;
import ru.hse.jade.sample.model.cookers_list.CookersList;
import ru.hse.jade.sample.model.kitchen_equipment_list.KitchenEquipmentList;
import ru.hse.jade.sample.model.techno_card.DishCard;
import ru.hse.jade.sample.model.visitors_orders_list.OrderInfo;
import ru.hse.jade.sample.util.JsonMessage;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static jade.core.AID.ISLOCALNAME;
import static java.lang.Math.min;
import static ru.hse.jade.sample.gson.MyGson.gson;

@JadeAgent()
public class ProcessAgent extends Agent implements SetAnnotationNumber {
    CookersList cookersList;
    KitchenEquipmentList kitchenEquipmentList;
    DishCard  dishCard;
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof DishCard) {
                dishCard = (DishCard) args[0];
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
    private void findFreeCookerAndEquipment(){
        var sum_time = 0.0;
        for(var i: (dishCard.operations)){
            sum_time += i.oper_time;
        }
        boolean isFound = false;
        while(!(isFound)){
            for(var cooker: MainController.cookersMap.keySet()){
                if(!cooker.cook_active){
                    for(var equip: MainController.equipmentStringHashMap.keySet()){
                        if(!equip.equip_active){
                            addBehaviour(new SendMessageOnce(gson.toJson(sum_time), Ontologies.PROCESS_TO_COOKER,
                                    MainController.cookersMap.get(cooker),0));
                            addBehaviour(new SendMessageOnce(gson.toJson(sum_time), Ontologies.PROCESS_TO_COOKER,
                                    new AID(MainController.equipmentStringHashMap.get(equip),ISLOCALNAME)));
                        isFound= true;
                        }
                    }
                }
            }
        }
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
