package it.ninjatech.eventbus.client.route;

import org.apache.camel.builder.RouteBuilder;

public class EventBusClientRouteBuilder extends RouteBuilder {

    private final String jmsComponentId;
    private final String jmsQueueId;

    public EventBusClientRouteBuilder(String jmsComponentId, String jmsQueueId) {
        this.jmsComponentId = jmsComponentId;
        this.jmsQueueId = jmsQueueId;
    }
    
    @Override
    public void configure() throws Exception {
        from("direct:event-bus")
        .routeId("event-bus-client-main-route")
        .to(String.format("%s:queue:%s", this.jmsComponentId, this.jmsQueueId));
    }

}
