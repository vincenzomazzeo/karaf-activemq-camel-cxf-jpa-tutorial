package it.ninjatech.warehouse;

import it.ninjatech.eventbus.client.EventBusClientService;
import it.ninjatech.warehouse.model.Product;

public class EventHandler {

    private final EventBusClientService eventBusClientService;
    
    public EventHandler(EventBusClientService eventBusClientService) {
        this.eventBusClientService = eventBusClientService;
    }

    public void notifyProductAdded(Product product) {
        this.eventBusClientService.notifyEvent(String.format("Added: [Product] %d - '%s'", product.getId(), product.getName()));
    }
    
    public void notifyProductModified(Product product) {
        this.eventBusClientService.notifyEvent(String.format("Modified: [Product] %d", product.getId()));
    }
    
    public void notifyProductDeleted(Integer productId) {
        this.eventBusClientService.notifyEvent(String.format("Deleted: [Product] %d", productId));
    }
    
}
