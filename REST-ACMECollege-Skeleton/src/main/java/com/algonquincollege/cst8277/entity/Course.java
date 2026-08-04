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

import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("unused")

/**
 * The persistent class for the course database table.
 */
// C01 - @Entity marks the class as managed by JPA; @Table binds it to the `course` table.
@Entity(name = "Course")
@Table(name = "course")
// C01 - all JPA annotations in this hierarchy are on fields (PojoBase places @Id on a field).
@Access(AccessType.FIELD)
// C01/C02 - PojoBase maps the PK as column "id", but the course table calls it "course_id".
//           @AttributeOverride re-points the inherited mapping without editing PojoBase.
@AttributeOverride(name = "id", column = @Column(name = "course_id"))
// C01 - DISTINCT is required: LEFT JOIN FETCH across a collection multiplies the root rows.
@NamedQuery(name = Course.ALL_COURSES_QUERY,
	query = "SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseRegistrations")
@NamedQuery(name = Course.COURSE_BY_ID_QUERY,
	query = "SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.courseRegistrations WHERE c.id = :" + PARAM1)
@NamedQuery(name = Course.COURSE_BY_CODE_QUERY,
	query = "SELECT DISTINCT c FROM Course c WHERE c.courseCode = :" + PARAM1)
// C02 - yes: PojoBase, the @MappedSuperclass holding id / version / created / updated.
public class Course extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ALL_COURSES_QUERY = "Course.findAll";
	public static final String COURSE_BY_ID_QUERY = "Course.findById";
	public static final String COURSE_BY_CODE_QUERY = "Course.findByCourseCode";

	// C03 - `course_code` VARCHAR(7) NOT NULL
	@Basic(optional = false)
	@Column(name = "course_code", nullable = false, length = 7)
	protected String courseCode;

	// C04 - `course_title` VARCHAR(100) NOT NULL
	@Basic(optional = false)
	@Column(name = "course_title", nullable = false, length = 100)
	protected String courseTitle;

	// C05 - `credit_units` INT NOT NULL
	@Basic(optional = false)
	@Column(name = "credit_units", nullable = false)
	protected Integer creditUnits;

	// C06 - `online` BIT(1) NOT NULL - mapped as Short (0 = classroom, 1 = online)
	@Basic(optional = false)
	@Column(name = "online", nullable = false)
	protected Short online;

	// C07 - 1:M inverse side. CourseRegistration.course owns the FK, hence mappedBy.
	//       LAZY so a course list does not drag in every registration.
	//       MERGE only - cascading REMOVE would delete registrations owned by another slice.
	@OneToMany(mappedBy = "course", cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
	// C08 - keep registrations out of the JSON payload (avoids the Course <-> CourseRegistration cycle).
	@JsonIgnore
	protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

	// C09 - UI-only flag, no such column on the DB.
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

	// C08 - also on the getter: Jackson resolves properties, not fields.
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

	//Inherited hashCode/equals is sufficient for this Entity class

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Course[id = ").append(id).append(", courseCode = ").append(courseCode).append(", courseTitle = ")
				.append(courseTitle).append(", creditUnits = ").append(creditUnits).append(", online = ").append(online)
				.append(", created = ").append(created).append(", updated = ").append(updated).append(", version = ").append(version).append("]");
		return builder.toString();
	}
	
}
