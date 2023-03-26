package ru.hse.jade.sample;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import ru.hse.jade.sample.agents.*;
import ru.hse.jade.sample.model.Error;
import ru.hse.jade.sample.model.cookers_list.Cooker;
import ru.hse.jade.sample.model.cookers_list.CookersList;
import ru.hse.jade.sample.model.kitchen_equipment_list.KitchenEquipment;
import ru.hse.jade.sample.model.kitchen_equipment_list.KitchenEquipmentList;
import ru.hse.jade.sample.model.menu.Menu;
import ru.hse.jade.sample.model.products_on_stock_list.ProductOnStockList;
import ru.hse.jade.sample.model.techno_card.ArrayOfDishCards;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrder;
import ru.hse.jade.sample.model.visitors_orders_list.VisitorsOrdersList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ru.hse.jade.sample.gson.MyGson.gson;

public class MainController {
    public static CookersList cookersList;
    public static KitchenEquipmentList kitchenEquipmentList;

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

    void initAgents() {
        try {
            createAgent(MainAgent.class, "MainAgent").start();
            createVisitorAgent();
            createCookerAgent();
            createMenuAgent();
            createStockAgent();
            createEquipmentAgent();
        } catch (Exception e) {
            new Error("Cant create agents", e.getMessage(), e.getLocalizedMessage());
        }
    }


    private String readFileFromResources(String filename) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(filename);
        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(bytes);
    }

    private void createVisitorAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("visitors_orders.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        VisitorsOrdersList visitorsOrdersList = gson.fromJson(json, VisitorsOrdersList.class);
        if (visitorsOrdersList == null) {
            throw new Error("JSON-error", "visitorsOrdersList", "");
        }
        int counter = 0;
        for (var i : visitorsOrdersList.visitors_orders) {
            containerController.createNewAgent(
                    "VisitorAgent" + counter,
                    VisitorAgent.class.getName(), new VisitorsOrder[]{i}).start();
            counter += 1;
        }
    }

    private void createStockAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("products.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        ProductOnStockList productOnStockList = gson.fromJson(json, ProductOnStockList.class);
        if (productOnStockList == null) {
            throw new Error("JSON-error", "stockList", "");
        }
        containerController.createNewAgent(
                "StockAgent",
                StockAgent.class.getName(), new ProductOnStockList[]{productOnStockList}).start();
    }

    private void createMenuAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("menu_dishes.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        Menu menu = gson.fromJson(json, Menu.class);
        if (menu == null) {
            throw new Error("JSON-error", "menuList", "");
        }
        try {
            json = readFileFromResources("dish_cards.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        ArrayOfDishCards dishCards = gson.fromJson(json, ArrayOfDishCards.class);
        if (dishCards == null) {
            throw new Error("JSON-error", "dishCards", "");
        }
        containerController.createNewAgent(
                "MenuAgent",
                MenuAgent.class.getName(), new Object[]{menu, dishCards}).start();
    }

    private void createCookerAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("cookers.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        cookersList = gson.fromJson(json, CookersList.class);
        if (cookersList == null) {
            throw new Error("JSON-error", "cookersList", "");
        }

        int counter = 0;
        for (var i : cookersList.cookers) {
            var t = containerController.createNewAgent(
                    "CookerAgent" + counter,
                    CookerAgent.class.getName(), new Cooker[]{i});
            t.start();
            counter += 1;
        }
    }

    private void createEquipmentAgent() throws StaleProxyException, Error {
        String json = "";
        try {
            json = readFileFromResources("equipment.json");
        } catch (Exception ex) {
            this.ex = ex;
            throw new Error("File-error", ex.getMessage(), ex.getLocalizedMessage());
        }
        kitchenEquipmentList = gson.fromJson(json, KitchenEquipmentList.class);
        if (kitchenEquipmentList == null) {
            throw new Error("JSON-error", "EquipmentAgent", "");
        }
        int counter = 0;
        for (var i : kitchenEquipmentList.equipment) {
            var t = containerController.createNewAgent(
                    "EquipmentAgent" + counter,
                    EquipmentAgent.class.getName(), new KitchenEquipment[]{i});
            t.start();
            counter += 1;
        }
    }

    private AgentController createAgent(Class<?> clazz, String agentName) throws StaleProxyException {
        return containerController.createNewAgent(
                agentName,
                clazz.getName(),
                null);
    }
}
