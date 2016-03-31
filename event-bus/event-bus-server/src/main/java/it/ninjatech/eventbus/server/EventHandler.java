package it.ninjatech.eventbus.server;

import java.sql.Timestamp;

import it.ninjatech.eventbus.model.Event;

public class EventHandler {

    public void handle(Event event) {
        event.setReceiveTimestamp(new Timestamp(System.currentTimeMillis()));
    }
    
}
