package it.ninjatech.warehouse.route;

import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;

public class WarehouseRouteBuilder extends RouteBuilder {

    private final String rsComponentId;
    private final String productDaoId;
    private final String eventHandlerId;
    
    public WarehouseRouteBuilder(String rsComponentId, String productDaoId, String eventHandlerId) {
        this.rsComponentId = rsComponentId;
        this.productDaoId = productDaoId;
        this.eventHandlerId = eventHandlerId;
    }
    
    @Override
    public void configure() throws Exception {
        from(String.format("cxfrs://bean://%s", this.rsComponentId))
        .routeId("warehouse-rs-main-route")
        .choice()
            .when(PredicateBuilder.and(header("CamelHttpMethod").isEqualTo("POST"), header("CamelHttpUri").isEqualTo("/warehouse/product")))
                .beanRef(this.productDaoId, "createProduct(${body})")
                .beanRef(this.eventHandlerId, "notifyProductAdded(${body})")
            .when(PredicateBuilder.and(header("CamelHttpMethod").isEqualTo("GET"), header("CamelHttpUri").isEqualTo("/warehouse/product")))
                .beanRef(this.productDaoId, "readProducts()")
            .when(PredicateBuilder.and(header("CamelHttpMethod").isEqualTo("PUT"), header("CamelHttpUri").regex("/warehouse/product/[0-9]+")))
                .beanRef(this.productDaoId, "updateProduct(${body[0]}, ${body[1]})")
                .beanRef(this.eventHandlerId, "notifyProductModified(${body})")
             .when(PredicateBuilder.and(header("CamelHttpMethod").isEqualTo("DELETE"), header("CamelHttpUri").regex("/warehouse/product/[0-9]+")))
                .beanRef(this.productDaoId, "deleteProduct(${body})")
                .beanRef(this.eventHandlerId, "notifyProductDeleted(${body})")
        .end();
    }

}
