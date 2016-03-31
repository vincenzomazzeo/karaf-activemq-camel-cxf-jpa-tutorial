package it.ninjatech.warehouse.rs;

import java.util.List;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;

import it.ninjatech.warehouse.model.Product;

@WebService
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface WarehouseService {

    @POST
    @Path("product")
    public Response<Product> postProduct(Product product);

    @GET
    @Path("product")
    public Response<List<Product>> getProducts();
    
    @PUT
    @Path("product/{productId}")
    public Response<Product> putProduct(@PathParam("productId") Integer productId, Product product);
    
    @DELETE
    @Path("product/{productId}")
    public Response<Integer> deleteProduct(@PathParam("productId") Integer productId);
    
}
