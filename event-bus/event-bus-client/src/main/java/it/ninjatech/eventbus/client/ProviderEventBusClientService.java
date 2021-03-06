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
