package com.norcode.bukkit.saddlebags;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table(name = "saddlebags_entityinventory")
public class EntityInventory {
    @Id @Column UUID id;
    @Temporal(TemporalType.TIMESTAMP) @Column private Date lastUpdated;
    @Column(nullable=false) private String owner;
    @Column(nullable=false) private Integer capacity;
    @Column(columnDefinition="TEXT") private String inventory;


    public UUID getId() {
        return id;
    }

    public void setId(UUID entityId) {
        this.id = entityId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getInventory() {
        return inventory;
    }

    public void setInventory(String inventory) {
        this.inventory = inventory;
    }

    /* Important guts below here */
    @Transient private volatile Object object;

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date updated) {
        this.lastUpdated = updated;
    }

    public boolean equals(Object obj) {
        final boolean returner;
        if (obj instanceof EntityInventory) {
            return getObject().equals(((EntityInventory) obj).getObject());
        } else {
            returner = false;
        }
        return returner;
    }

    public int hashCode() {
        return getObject().hashCode();
    }

    private Object getObject() {
        if (object != null || object == null && id == null) {
            if (object == null) { // Avoid the performance impact of
                                  // synchronized if we can
                synchronized (this) {
                    if (object == null) {
                        object = new Object();
                    }
                }
            }
            return object;
        }
        return id;
    }
}
