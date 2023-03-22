package ru.hse.jade.sample.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import ru.hse.jade.sample.behaviour.SendMessageBehaviour;
import ru.hse.jade.sample.configuration.JadeAgent;
import ru.hse.jade.sample.model.Person;

@JadeAgent("HelloAgent")
public class HelloWorldAgent extends Agent {

    private AID[] testAgents;

    @Override
    protected void setup() {
        System.out.println("Hello world! I'm an agent!");
        System.out.println("My local name is " + getAID().getLocalName());
        System.out.println("My GUID is " + getAID().getName());
        System.out.println("My addresses are " + String.join(",", getAID().getAddressesArray()));


        findTestAgents();
    }

    private void findTestAgents() {
        // Add a TickerBehaviour that schedules a request to seller agents every minute
        addBehaviour(new TickerBehaviour(this, 10000) {
            protected void onTick() {
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("test-squad");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    testAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        testAgents[i] = result[i].getName();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
                // Perform the request
                myAgent.addBehaviour(new SendMessageBehaviour(testAgents,
                        new Person(
                                "Anton",
                               "Kalinin"
                        )));
            }
        });
    }
}
