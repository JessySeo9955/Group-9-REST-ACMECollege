/********************************************************************************************************
 * File: Professor.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity(name = "Professor")
@Table(name = "professor")
@Access(AccessType.FIELD)
@AttributeOverride(name = "id", column = @Column(name = "professor_id"))
@NamedQuery(name = Professor.ALL_PROFESSORS_QUERY, query = "SELECT DISTINCT p FROM Professor p LEFT JOIN FETCH p.courseRegistrations")
@NamedQuery(name = Professor.QUERY_PROFESSOR_BY_ID, query = "SELECT DISTINCT p FROM Professor p LEFT JOIN FETCH p.courseRegistrations WHERE p.id = :param1")
public class Professor extends PojoBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ALL_PROFESSORS_QUERY = "Professor.findAll";
    public static final String QUERY_PROFESSOR_BY_ID = "Professor.findById";

    @Basic(optional = false)
    @Column(name = "first_name", nullable = false, length = 50)
    protected String firstName;

    @Basic(optional = false)
    @Column(name = "last_name", nullable = false, length = 50)
    protected String lastName;

    @Column(name = "degree", length = 45)
    protected String degree;

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "professor")
    @JsonIgnore
    protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

    @Transient
    protected boolean editable = false;

    public Professor() { super(); }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }
    public Set<CourseRegistration> getCourseRegistrations() { return courseRegistrations; }
    public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) { this.courseRegistrations = courseRegistrations; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }

    @Override
    public String toString() {
        return "Professor[id = " + id + ", firstName = " + firstName + ", lastName = " + lastName
            + ", degree = " + degree + "]";
    }
}
