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
