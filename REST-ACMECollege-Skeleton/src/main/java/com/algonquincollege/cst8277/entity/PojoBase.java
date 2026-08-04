/********************************************************************************************************
 * File: PojoBase.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
@Access(AccessType.FIELD)
@EntityListeners(PojoListener.class)
public abstract class PojoBase implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;

    @Version
    @Column(name = "version", nullable = false)
    protected int version = 1;

    @Column(name = "created")
    protected LocalDateTime created;

    @Column(name = "updated")
    protected LocalDateTime updated;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }
    public LocalDateTime getUpdated() { return updated; }
    public void setUpdated(LocalDateTime updated) { this.updated = updated; }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (obj instanceof PojoBase otherPojoBase) {
            return Objects.equals(this.getId(), otherPojoBase.getId());
        }
        return false;
    }
}
