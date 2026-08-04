/********************************************************************************************************
 * File:  CourseRegistrationPK.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The primary key class for the course_registration database table.
 */
@Embeddable
@Access(AccessType.FIELD)
public class CourseRegistrationPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @Column(name = "student_id", nullable = false)
    protected int studentId;

    @Basic(optional = false)
    @Column(name = "course_id", nullable = false)
    protected int courseId;

    public CourseRegistrationPK() {
    }

    public CourseRegistrationPK(int studentId, int courseId) {
        setStudentId(studentId);
        setCourseId(courseId);
    }

    public int getStudentId() {
        return this.studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return this.courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + Objects.hash(getStudentId(), getCourseId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof CourseRegistrationPK otherCourseRegistrationPK) {
            return Objects.equals(this.getStudentId(), otherCourseRegistrationPK.getStudentId()) &&
                Objects.equals(this.getCourseId(),  otherCourseRegistrationPK.getCourseId());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CourseRegistrationPK [studentId = ");
        builder.append(studentId);
        builder.append(", courseId = ");
        builder.append(courseId);
        builder.append("]");
        return builder.toString();
    }
}