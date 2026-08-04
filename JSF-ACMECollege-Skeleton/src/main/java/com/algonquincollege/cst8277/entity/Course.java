/********************************************************************************************************
 * File:  Course.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
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

/**
 * The persistent class for the course database table.
 */
@Entity
@Table(name = "course")
@Access(AccessType.FIELD)
@NamedQuery(name = Course.ALL_COURSES_QUERY, query = "SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseRegistrations")
@AttributeOverride(name = "id", column = @Column(name = "course_id"))
public class Course extends PojoBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ALL_COURSES_QUERY = "Course.findAll";

    @Basic(optional = false)
    @Column(name = "course_code", nullable = false, length = 7)
    protected String courseCode;

    @Basic(optional = false)
    @Column(name = "course_title", nullable = false, length = 100)
    protected String courseTitle;

    @Basic(optional = false)
    @Column(name = "credit_units", nullable = false)
    protected Integer creditUnits;

    @Basic(optional = false)
    @Column(name = "online", nullable = false)
    protected Short online;

    @OneToMany(mappedBy = "course", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JsonIgnore
    protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

    @Transient
    protected boolean editable = false;

    public Course() {
        super();
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public Integer getCreditUnits() {
        return creditUnits;
    }

    public void setCreditUnits(Integer creditUnits) {
        this.creditUnits = creditUnits;
    }

    public Short getOnline() {
        return online;
    }

    public void setOnline(Short online) {
        this.online = online;
    }

    @JsonIgnore
    public Set<CourseRegistration> getCourseRegistrations() {
        return courseRegistrations;
    }

    public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) {
        this.courseRegistrations = courseRegistrations;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Course[id = ").append(id).append(", courseCode = ").append(courseCode).append(", courseTitle = ")
            .append(courseTitle).append(", creditUnits = ").append(creditUnits).append(", online = ").append(online)
            .append(", created = ").append(created).append(", updated = ").append(updated).append(", version = ").append(version).append("]");
        return builder.toString();
    }
}