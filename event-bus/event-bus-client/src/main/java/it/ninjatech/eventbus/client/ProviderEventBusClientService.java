package it.ninjatech.eventbus.client;

import java.sql.Timestamp;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import it.ninjatech.eventbus.model.Event;

public class ProviderEventBusClientService implements EventBusClientService {

    private final ProducerTemplate producerTemplate;
    
    public ProviderEventBusClientService(CamelContext camelContext, String producerTemplateId) {
        this.producerTemplate = (ProducerTemplate)camelContext.getRegistry().lookupByName(producerTemplateId);
    }
    
    @Override
    public void notifyEvent(String description) {
        Event event = new Event();
        event.setSendTimestamp(new Timestamp(System.currentTimeMillis()));
        event.setDescription(description);
        
        producerTemplate.sendBody("direct:event-bus", event);
    }

}
