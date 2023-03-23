package ru.hse.jade.sample.model.visitors_orders_list;

public class OrderInfo {
    public class Status{
        static final String ready= "READY";
        static final String cooking= "COOKING";
        static final String notCooking= "NOT_COOKING";
    }
    String status = Status.notCooking;
    int minutesLeft = 0;
}
