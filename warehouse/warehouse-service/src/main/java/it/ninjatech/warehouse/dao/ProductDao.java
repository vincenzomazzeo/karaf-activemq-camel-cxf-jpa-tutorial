package it.ninjatech.warehouse.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import it.ninjatech.warehouse.model.Product;

public class ProductDao {

    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void createProduct(Product product) {
        this.entityManager.persist(product);
        this.entityManager.flush();
    }

    @SuppressWarnings("unchecked")
    public List<Product> readProducts() {
        List<Product> result = null;
        
        Query query = this.entityManager.createQuery("SELECT p FROM Product p");
        result = query.getResultList();
        
        return result;
    }

    public Product updateProduct(Integer id, Product product) {
        Product result = null;
        
        result = this.entityManager.find(Product.class, id);
        result.setName(product.getName());
        this.entityManager.persist(result);
        this.entityManager.flush();
        
        return result;
    }

    public Integer deleteProduct(Integer id) {
        Integer result = id;
        
        Product product = this.entityManager.find(Product.class, id);
        this.entityManager.remove(product);
        this.entityManager.flush();
        
        return result;
    }

}
