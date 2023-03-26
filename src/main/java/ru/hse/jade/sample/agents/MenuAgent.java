package ru.hse.jade.sample.agents;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.behaviour.SendMessageOnce;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.menu.Menu;
import ru.hse.jade.sample.model.techno_card.ArrayOfDishCards;
import ru.hse.jade.sample.model.techno_card.DishCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JadeAgent()
public class MenuAgent extends Agent implements SetAnnotationNumber {
    Menu menu;
    ArrayOfDishCards arrayOfDishCards;
    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Menu) {
                menu = (Menu) args[0];
            }
            if (args[1] instanceof ArrayOfDishCards) {
                arrayOfDishCards = (ArrayOfDishCards) args[1];
            }
        }

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.menuAgent);
        sd.setName("JADE-test");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new resendDishesToOrderAgent(this));
    }
    private static class resendDishesToOrderAgent extends Behaviour {
        MenuAgent menuAgent;

        public resendDishesToOrderAgent(MenuAgent menuAgent) {
            this.menuAgent = menuAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.ORDER_TO_MENU)) {
                    String json = msg.getContent();
                    int[] menuList = MyGson.gson.fromJson(json, int[].class);
                    myAgent.addBehaviour(new SendMessageOnce(
                            MyGson.gson.toJson(getNeededDishes(menuList)),
                            Ontologies.MENU_TO_ORDER, msg.getSender()));
                }
                } else {
                    block();
                }
        }

        private ArrayList<DishCard> getNeededDishes(int[] menuList) {
            ArrayList<DishCard> res = new ArrayList<>();
            for(var i: menuList){
                Integer indexInDishesCard = -1;
                for (var j: menuAgent.menu.menu_dishes){
                    if(j.menu_dish_id == i){
                        indexInDishesCard = j.menu_dish_card;
                    }
                }
                for (var j: menuAgent.arrayOfDishCards.dish_cards){
                    if(j.card_id == indexInDishesCard){
                        res.add(j);
                    }
                }
            }
            return res;
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
