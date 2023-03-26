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
import ru.hse.jade.sample.behaviour.SendMessageOnce;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.gson.MyGson;
import ru.hse.jade.sample.model.products_on_stock_list.ProductOnStockList;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.min;

@JadeAgent()
public class StockAgent extends Agent implements SetAnnotationNumber {
    ProductOnStockList productOnStockList;

    @Override
    protected void setup() {
        System.out.println("Hello from " + getAID().getName());
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof ProductOnStockList) {
                productOnStockList = (ProductOnStockList) args[0];
            }
        }
        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(AgentTypes.stockAgent);
        sd.setName("JADE-test");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }


        addBehaviour(new ResendProductListToOrder(this));
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

    private static class ResendProductListToOrder extends Behaviour {
        StockAgent stockAgent;

        public ResendProductListToOrder(StockAgent stockAgent) {
            this.stockAgent = stockAgent;
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                if (Objects.equals(msg.getOntology(), Ontologies.ORDER_TO_STOCK)) {
                    String json = msg.getContent();
                    Type type = new TypeToken<HashMap<Integer, Double>>() {
                    }.getType();
                    Map<Integer, Double> askedProducts = MyGson.gson.fromJson(json, type);
                    myAgent.addBehaviour(new SendMessageOnce(
                            MyGson.gson.toJson(getNeededProducts(askedProducts)),
                            Ontologies.STOCK_TO_ORDER, msg.getSender()));
                }
            } else {
                block();
            }
        }

        private Map<Integer, Double> getNeededProducts(Map<Integer, Double> askedProducts) {

            Map<Integer, Double> res = new HashMap<>();
            Map<Integer, Double> existingProducts = new HashMap<>();

            for (var i : stockAgent.productOnStockList.products) {
                existingProducts.put(i.prod_item_type, i.prod_item_quantity);
            }
            for (var i : askedProducts.keySet()) {
                if (existingProducts.containsKey(i)) {
                    res.put(i, min(askedProducts.get(i), existingProducts.get(i)));
                } else {
                    res.put(i, 0.0);
                    continue;
                }
                for (var j : stockAgent.productOnStockList.products) {
                    if (j.prod_item_type == i) {
                        j.prod_item_quantity -= res.get(i);
                        break;
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
}
