package ru.hse.jade.sample.behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import ru.hse.jade.sample.model.Error;
import ru.hse.jade.sample.util.JsonMessage;

public class SendMessageOnce extends Behaviour {
    String message;
    String ontology;
    String agentType;
    int index;
    AID name = null;
    boolean isSend = false;

    public SendMessageOnce(String message, String ontology, String agentType, int index) {
        this.message = message;
        this.ontology = ontology;
        this.agentType = agentType;
        this.index = index;
    }

    public SendMessageOnce(String message, String ontology, AID name) {
        this.message = message;
        this.ontology = ontology;
        this.name = name;
    }

    @Override
    public void action() {

        JsonMessage cfp = new JsonMessage(ACLMessage.CFP);
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(agentType);
        template.addServices(sd);
        try {
            if (name == null) {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                name = result[index].getName();
            }
            cfp.addReceiver(name);
            cfp.setOntology(ontology);
            cfp.setContent(message);
            myAgent.send(cfp);
            isSend = true;
        } catch (IndexOutOfBoundsException ex) {
            //ex.printStackTrace();
        } catch (Exception ex) {
            try {
                throw new Error("Cant find agent", ex.getMessage(), ex.getLocalizedMessage());
            } catch (Error ignored) {

            }
        }

    }

    @Override
    public boolean done() {
        return isSend;
    }
}
