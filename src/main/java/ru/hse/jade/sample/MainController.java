package ru.hse.jade.sample;

import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.reflections.Reflections;
import ru.hse.jade.sample.agents.TestAgent;
import ru.hse.jade.sample.agents.VisitorAgent;
import ru.hse.jade.sample.annotation_setup.SetAnnotationNumber;
import ru.hse.jade.sample.configuration.JadeAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;
import com.google.gson.*;
import ru.hse.jade.sample.model.Error;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrdersList;

import static ru.hse.jade.sample.gson.MyGson.gson;

class MainController {

    private final ContainerController containerController;
    private Exception ex;

    public MainController() {
        final Runtime rt = Runtime.instance();
        final Profile p = new ProfileImpl();

        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "8080");
        p.setParameter(Profile.GUI, "true");
        containerController = rt.createMainContainer(p);
    }
    void configureAgentClasses(){
        new TestAgent().setNumber(2);
    }
    void initAgents() {
        initAgents(MainController.class.getPackageName());
    }

    void initAgents(String basePackage) {
        final Reflections reflections = new Reflections(basePackage);
        configureAgentClasses();

        final Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(JadeAgent.class);
        try {
            for (Class<?> clazz : allClasses) {
                if (Agent.class.isAssignableFrom(clazz)) {
                    configureAgent(clazz);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureAgent(Class<?> clazz) throws StaleProxyException, Error {
        final JadeAgent jadeAgent = clazz.getAnnotation(JadeAgent.class);

        if (jadeAgent.number() <= 0) {
            throw new IllegalStateException(MessageFormat.format(
                    "Number of agent {0} is less then 1. Real number is {1}",
                    clazz.getName(),
                    jadeAgent.number()
            ));
        }
        final String agentName =
                !Objects.equals(jadeAgent.value(), "")
                        ? jadeAgent.value()
                        : clazz.getSimpleName();
        if (clazz.equals(VisitorAgent.class)) {
            createVisitorAgent(clazz,agentName).start();
            return;
        }

        if (jadeAgent.number() == 1) {
            createAgent(clazz, agentName).start();
        } else {
            for (int i = 0; i < jadeAgent.number(); ++i) {
                createAgent(
                        clazz,
                        MessageFormat.format(
                                "{0}{1}",
                                agentName,
                                i
                        )).start();
            }
        }
    }
    private String readFileFromResources(String filename) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(filename);
        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(bytes);
    }
    private AgentController createVisitorAgent(Class<?> clazz, String agentName) throws StaleProxyException, Error {
        String json = "";
        try{
            json = readFileFromResources("visitors_orders.json");
        } catch (Exception ex){
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        VisitorsOrdersList visitorsOrdersList = gson.fromJson(json,VisitorsOrdersList.class);
        if(visitorsOrdersList == null){
            throw new Error("JSON-error", "visitorsOrdersList", "");
        }
        return containerController.createNewAgent(
                agentName,
                clazz.getName(), new VisitorsOrdersList[]{visitorsOrdersList});
    }
    private AgentController createAgent(Class<?> clazz, String agentName) throws StaleProxyException {
        return containerController.createNewAgent(
                agentName,
                clazz.getName(),
                null);
    }

}
