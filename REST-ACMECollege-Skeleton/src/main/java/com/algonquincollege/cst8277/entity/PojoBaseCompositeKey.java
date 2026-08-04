/********************************************************************************************************
 * File: PojoBaseCompositeKey.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
@Access(AccessType.FIELD)
@EntityListeners(PojoCompositeListener.class)
public abstract class PojoBaseCompositeKey<ID extends Serializable> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Version
    @Column(name = "version", nullable = false)
    protected int version = 1;

    @Column(name = "created")
    protected LocalDateTime created;

    @Column(name = "updated")
    protected LocalDateTime updated;

    public abstract ID getId();
    public abstract void setId(ID id);
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
        if (obj instanceof PojoBaseCompositeKey otherPojoBaseComposite) {
            return Objects.equals(this.getId(), otherPojoBaseComposite.getId());
        }
        return false;
    }
}
