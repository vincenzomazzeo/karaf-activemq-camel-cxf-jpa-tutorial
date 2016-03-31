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
