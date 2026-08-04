/********************************************************************************************************
 * File: SecurityRole.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity(name = "SecurityRole")
@Table(name = "security_role")
@NamedQuery(name = SecurityRole.SECURITY_ROLE_BY_NAME, query = "SELECT DISTINCT sr FROM SecurityRole sr LEFT JOIN FETCH sr.users WHERE sr.roleName = :param1")
public class SecurityRole implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String SECURITY_ROLE_BY_NAME = "SecurityRole.RoleByName";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    protected int id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false, unique = true, length = 45)
    protected String roleName;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    protected Set<SecurityUser> users = new HashSet<>();

    public SecurityRole() { super(); }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Set<SecurityUser> getUsers() { return users; }
    public void setUsers(Set<SecurityUser> users) { this.users = users; }
    public void addUserToRole(SecurityUser user) { getUsers().add(user); }

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
        if (obj instanceof SecurityRole otherSecurityRole) {
            return Objects.equals(this.getId(), otherSecurityRole.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "SecurityRole [id = " + id + ", roleName = " + roleName + "]";
    }
}
