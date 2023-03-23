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
import ru.hse.jade.sample.behaviour.SendMessageOnce;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.Error;
import ru.hse.jade.sample.model.menu.Dish;
import ru.hse.jade.sample.model.techno_card.DishCard;
import ru.hse.jade.sample.model.visitors_orders_list.OrderInfo;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@JadeAgent()
public class OrderAgent extends Agent implements SetAnnotationNumber {
    VisitorsOrder visitorsOrder;
    int counter = 0;
    AID visitorAID;
    Map<AID, Integer> mapAgentTime = new HashMap<>();
    ArrayList<DishCard> neededDishes = new ArrayList<>();
    Map<Integer,Double> existingResources = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof VisitorsOrder) {
                visitorsOrder = (VisitorsOrder) args[0];
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
        createProcessAgents();
        addBehaviour(new SendStatusToVisitor(this));
        addBehaviour(new SendMessageOnce(MyGson.gson.toJson(visitorsOrder.vis_ord_dishes.stream().
                map(e -> e.menu_dish)),
                Ontologies.ORDER_TO_MENU,
                AgentTypes.menuAgent,0));
    }

    private void createProcessAgents() {

        for (var i : neededDishes) {
            var tmpExistingProducts = Map<in>//copy of existing prods
            boolean isEnought = true;
            for(var j: i.operations){
                for (var k: j.oper_products) {
                    existingResources.put(k.prod_type,existingResources.get(k.prod_type) - k.prod_quantity);
                    if(existingResources.get(k.prod_type) < 0){
                        isEnought = false;
                        break;
                    }
                }
                if(!isEnought){
                    //Work with copy


                    break;
                }
            }
            if(!isEnought){
                continue;
            }
            ContainerController cnc = this.getContainerController();
            try {
                var t = cnc.createNewAgent(AgentTypes.cookerAgent + counter, CookerAgent.class.getName(),
                        new Object[]{i});
                t.start();
            } catch (StaleProxyException e) {
                new Error("Cannot create order agent", e.getMessage(),
                        e.getLocalizedMessage());
            }
        }
    }

    private static class SendStatusToVisitor extends Behaviour {
        OrderAgent orderAgent;

        public SendStatusToVisitor(OrderAgent orderAgent) {
            this.orderAgent = orderAgent;
        }

        @Override
        public void action() {

            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.COOKING_TO_ORDER)) {
                    String json = msg.getContent();
                    Integer timeToCook = MyGson.gson.fromJson(json, Integer.class);
                    orderAgent.mapAgentTime.put(msg.getSender(), timeToCook);
                    if(orderAgent.mapAgentTime.size() == orderAgent.visitorsOrder.vis_ord_dishes.size()){
                        Integer currentTotalTime = 0;
                        for(var i: orderAgent.mapAgentTime.values()){
                            currentTotalTime += i;
                        }
                        String state = OrderInfo.Status.notCooking;
                        if(currentTotalTime > 0){
                            state = OrderInfo.Status.cooking;
                        }
                        OrderInfo orderInfo = new OrderInfo(state,currentTotalTime);
                        myAgent.addBehaviour(new SendMessageOnce(
                                MyGson.gson.toJson(orderInfo),Ontologies.ORDER_TO_VISITOR,
                                orderAgent.visitorAID));
                    }
                } else if(Objects.equals(msg.getOntology(), Ontologies.MENU_TO_ORDER)){
                    String json = msg.getContent();
                    orderAgent.neededDishes = MyGson.gson.fromJson(json, (ArrayList.class));
                    myAgent.addBehaviour(new SendMessageOnce(MyGson.gson.toJson(getNeededProducts()),
                            Ontologies.ORDER_TO_STOCK,
                            AgentTypes.stockAgent,0));
                } else if(Objects.equals(msg.getOntology(), Ontologies.STOCK_TO_ORDER)){
                    String json = msg.getContent();
                    orderAgent.existingResources = MyGson.gson.fromJson(json, (Map.class));
                    orderAgent.createProcessAgents();
                }
            } else {
                block();
            }
        }
        private Map<Integer,Double> getNeededProducts(){
            Map<Integer,Double> res = new HashMap<>();
            for(var i: orderAgent.neededDishes){
                for(var j: i.operations){
                    for (var k: j.oper_products) {
                        if(!res.containsKey(k.prod_type)){
                            res.put(k.prod_type, k.prod_quantity);
                        }else {
                            res.put(k.prod_type, res.get(k.prod_type) + k.prod_quantity);
                        }}
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
    public void setNumber(int number) {
        SetAnnotationNumber.super.setNumber(number);
    }
}
