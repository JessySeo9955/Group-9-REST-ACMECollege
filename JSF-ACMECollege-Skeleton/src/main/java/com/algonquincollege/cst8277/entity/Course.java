/********************************************************************************************************
 * File: Course.java Course Materials CST 8277
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

@Entity(name = "Course")
@Table(name = "course")
@Access(AccessType.FIELD)
@AttributeOverride(name = "id", column = @Column(name = "course_id"))
@NamedQuery(name = Course.ALL_COURSES_QUERY, query = "SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseRegistrations")
@NamedQuery(name = Course.QUERY_COURSE_BY_ID, query = "SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseRegistrations WHERE c.id = :param1")
public class Course extends PojoBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ALL_COURSES_QUERY = "Course.findAll";
    public static final String QUERY_COURSE_BY_ID = "Course.findById";

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

    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "course")
    @JsonIgnore
    protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

    @Transient
    protected boolean editable = false;

    public Course() { super(); }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public Integer getCreditUnits() { return creditUnits; }
    public void setCreditUnits(Integer creditUnits) { this.creditUnits = creditUnits; }
    public Short getOnline() { return online; }
    public void setOnline(Short online) { this.online = online; }
    public Set<CourseRegistration> getCourseRegistrations() { return courseRegistrations; }
    public void setCourseRegistrations(Set<CourseRegistration> courseRegistrations) { this.courseRegistrations = courseRegistrations; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }

    @Override
    public String toString() {
        return "Course[id = " + id + ", courseCode = " + courseCode + ", courseTitle = " + courseTitle
            + ", creditUnits = " + creditUnits + ", online = " + online + "]";
    }
}
