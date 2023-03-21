package ru.hse.jade.sample.model.agent_of_cookicng_process;

import java.util.ArrayList;
import java.util.Date;

public class CookingProcessLog {
    public int proc_id;
    public int ord_dish;
    public Date proc_started;
    public Date proc_ended;
    public boolean proc_active;
    public ArrayList<CookingProcessOperation> proc_operations;
}
