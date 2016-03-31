package it.ninjatech.warehouse.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "product")
public class Product {

    @JsonProperty
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ProductSequence")
    @SequenceGenerator(name = "ProductSequence", sequenceName = "product_sequence", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;
    @JsonProperty
    @Column(name = "name", nullable = false)
    private String name;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
