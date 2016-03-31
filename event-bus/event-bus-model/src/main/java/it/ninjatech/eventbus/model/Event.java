package it.ninjatech.eventbus.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "event_bus_journal")
public class Event implements Serializable {

    private static final long serialVersionUID = -2915870084418741288L;

    @Id
    @Column(name = "id", nullable = true)
    private Integer id;
    @Column(name = "sendTimestamp", nullable = false)
    private Timestamp sendTimestamp;
    @Column(name = "receiveTimestamp", nullable = false)
    private Timestamp receiveTimestamp;
    @Column(name = "description", nullable = false)
    private String description;

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getSendTimestamp() {
        return this.sendTimestamp;
    }

    public void setSendTimestamp(Timestamp sendTimestamp) {
        this.sendTimestamp = sendTimestamp;
    }

    public Timestamp getReceiveTimestamp() {
        return this.receiveTimestamp;
    }

    public void setReceiveTimestamp(Timestamp receiveTimestamp) {
        this.receiveTimestamp = receiveTimestamp;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
