/********************************************************************************************************
 * File:  StudentClub.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * 
 */
package com.algonquincollege.cst8277.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.DiscriminatorFormula;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.algonquincollege.cst8277.entity.Academic;
import com.algonquincollege.cst8277.entity.NonAcademic;

@SuppressWarnings("unused")

/**
 * The persistent class for the student_club database table.
 */
//TODO SC01 - Add the missing annotations.
//TODO SC02 - StudentClub has subclasses Academic and NonAcademic.  Look at lecture slides for InheritanceType.
//TODO SC03 - Do we need a mapped super class?  If so, which one?
@Entity
@Table(name = "student_club")
@Access(AccessType.FIELD)
@NamedQuery(
    name = StudentClub.ALL_STUDENT_CLUBS_QUERY,
    query = "SELECT DISTINCT sc FROM StudentClub sc LEFT JOIN FETCH sc.studentMembers"
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
		 "CASE WHEN academic = 1 THEN 1 ELSE 0 END"
	)
@AttributeOverride(name = "id", column = @Column(name = "club_id"))
public class StudentClub extends PojoBase implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String ALL_STUDENT_CLUBS_QUERY = "StudentClub.findAll";

	// TODO SC04 - Add the missing annotations.
    @Basic(optional = false)
    @Column(name = "name", nullable = false)
	protected String name;

	// TODO SC05 - Add the missing annotations.
    @Basic(optional = false)
    @Column(name = "description", nullable = false)
	protected String desc;

	// TODO SC06 - Add the missing annotations.
    // The 'academic' column is already mapped by @DiscriminatorColumn above.
    // Mapping it again read-write makes Hibernate fail at boot with
    // "Repeated column in mapping", so this field is read-only: the value is
    // written by the discriminator and only read back into the field.
    @Basic(optional = false)
    @Column(
	    name = "academic", nullable = false)
    protected boolean isAcademic;

	// TODO SC07 - Add the M:N annotation.  What should be the cascade and fetch types?
	// TODO SC08 - Add other missing annotations.
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(
        name = "club_membership",
        joinColumns = @JoinColumn(name = "club_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnore
	protected Set<Student> studentMembers = new HashSet<Student>();
	
	// TODO SC09 - Add the missing annotations.
    @Transient
	protected boolean editable = false;

	public StudentClub() {
		super();
	}

    public StudentClub(boolean isAcademic) {
        this();
        this.isAcademic = isAcademic;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public boolean getAcademic() {
		return this.isAcademic;
	}

	public void setAcademic(boolean isAcademic) {
		this.isAcademic = isAcademic;
	}

	// TODO SC10 - Is an annotation needed here?
	@JsonIgnore
	public Set<Student> getStudentMembers() {
		return studentMembers;
	}

	public void setStudentMembers(Set<Student> studentMembers) {
		this.studentMembers = studentMembers;
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
		builder.append("StudentClub[id = ").append(id).append(", name = ").append(name).append(", desc = ")
				.append(desc).append(", isAcademic = ").append(isAcademic)
				.append(", created = ").append(created).append(", updated = ").append(updated).append(", version = ").append(version).append("]");
		return builder.toString();
	}
	
}
