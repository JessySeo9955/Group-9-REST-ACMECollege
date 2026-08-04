/********************************************************************************************************
 * File: CourseRegistration.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_registration")
@Access(AccessType.FIELD)
@NamedQuery(name = CourseRegistration.ALL_COURSE_REGISTRATIONS_QUERY_NAME, query = "SELECT cr FROM CourseRegistration cr")
@NamedQuery(name = CourseRegistration.QUERY_SPECIFIC_COURSE_REGISTRATION, query = "SELECT cr FROM CourseRegistration cr WHERE cr.student.id = :param1 AND cr.course.id = :param2")
@NamedQuery(name = CourseRegistration.QUERY_COURSE_REGISTRATIONS_BY_STUDENT, query = "SELECT cr FROM CourseRegistration cr WHERE cr.student.id = :param1")
public class CourseRegistration extends PojoBaseCompositeKey<CourseRegistrationPK> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ALL_COURSE_REGISTRATIONS_QUERY_NAME = "CourseRegistration.findAll";
    public static final String QUERY_SPECIFIC_COURSE_REGISTRATION = "CourseRegistration.findSpecificCourseRegistration";
    public static final String QUERY_COURSE_REGISTRATIONS_BY_STUDENT = "CourseRegistration.findByStudent";

    @EmbeddedId
    protected CourseRegistrationPK id = new CourseRegistrationPK();

    @MapsId("studentId")
    @ManyToOne(cascade = CascadeType.MERGE, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    protected Student student;

    @MapsId("courseId")
    @ManyToOne(cascade = CascadeType.MERGE, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", referencedColumnName = "course_id", nullable = false)
    @JsonIgnore
    protected Course course;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", referencedColumnName = "professor_id")
    @JsonIgnore
    protected Professor professor;

    @Basic(optional = false)
    @Column(name = "year", nullable = false)
    protected int year;

    @Basic(optional = false)
    @Column(name = "semester", nullable = false, length = 6)
    protected String semester;

    @Column(name = "letter_grade", length = 3)
    protected String letterGrade;

    public CourseRegistration() {
        id = new CourseRegistrationPK();
    }

    @Override
    public CourseRegistrationPK getId() { return id; }
    @Override
    public void setId(CourseRegistrationPK id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) {
        if (student != null) { id.setStudentId(student.getId()); }
        this.student = student;
    }
    public Course getCourse() { return course; }
    public void setCourse(Course course) {
        if (course != null) { id.setCourseId(course.getId()); }
        this.course = course;
    }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    @JsonProperty("studentId")
    public int getStudentId() { return id == null ? 0 : id.getStudentId(); }

    @JsonProperty("courseId")
    public int getCourseId() { return id == null ? 0 : id.getCourseId(); }

    @JsonProperty("professorId")
    public Integer getProfessorId() { return professor == null ? null : professor.getId(); }
}
