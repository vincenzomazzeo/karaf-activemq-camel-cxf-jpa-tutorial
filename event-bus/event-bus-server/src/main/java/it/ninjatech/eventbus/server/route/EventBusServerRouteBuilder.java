package it.ninjatech.eventbus.server.route;

import org.apache.camel.builder.RouteBuilder;

import it.ninjatech.eventbus.server.EventHandler;

public class EventBusServerRouteBuilder extends RouteBuilder {

    private final String jmsComponentId;
    private final String jmsQueueId;
    private final String jpaComponentId;
    private final String persistenceUnitId;
    
    public EventBusServerRouteBuilder(String jmsComponentId, String jmsQueueId, String jpaComponentId, String persistenceUnitId) {
        this.jmsComponentId = jmsComponentId;
        this.jmsQueueId = jmsQueueId;
        this.jpaComponentId = jpaComponentId;
        this.persistenceUnitId = persistenceUnitId;
    }
    
    @Override
    public void configure() throws Exception {
        from(String.format("%s:queue:%s", this.jmsComponentId, this.jmsQueueId))
        .routeId("event-bus-server-main-route")
        .bean(EventHandler.class, "handle(${body})")
        .to(String.format("%s:?persistenceUnit=%s", this.jpaComponentId, this.persistenceUnitId));
    }

}
