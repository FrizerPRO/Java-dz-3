package ru.hse.jade.sample.model.visitors_orders_list;

import java.util.ArrayList;
import java.util.Date;

public class VisitorsOrder {
    public String vis_name;
    public Date vis_ord_started;
    public Date vis_ord_ended;
    public int vis_ord_total;
    public ArrayList<VisitorOrderDish> vis_ord_dishes;
}
