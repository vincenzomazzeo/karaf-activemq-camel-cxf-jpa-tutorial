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
