/**
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the 'License'); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an 'AS IS' BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    @author vincenzo.mazzeo
*/
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
