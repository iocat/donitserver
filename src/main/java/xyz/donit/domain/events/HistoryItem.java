package xyz.donit.domain.events;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Timestamp;

/**
 * Created by felix on 1/13/17.
 */
public class HistoryItem {
    private HistoryEnum type;
    private Timestamp at;
    // The goal id of this event;
    private long goalId;
    // The task or habit id associated with this event
    private long torhId;


    private static final String QUERY_STORE = "";
    public void store(BasicDataSource ds, long userId){

    }




}

