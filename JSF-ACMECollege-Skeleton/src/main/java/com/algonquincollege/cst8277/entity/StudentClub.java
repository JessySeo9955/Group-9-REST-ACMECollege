/********************************************************************************************************
 * File: StudentClub.java Course Materials CST 8277
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
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity(name = "StudentClub")
@Table(name = "student_club")
@Access(AccessType.FIELD)
@AttributeOverride(name = "id", column = @Column(name = "club_id"))
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "academic", discriminatorType = DiscriminatorType.INTEGER)
@NamedQuery(name = StudentClub.ALL_STUDENT_CLUBS_QUERY, query = "SELECT DISTINCT sc FROM StudentClub sc LEFT JOIN FETCH sc.studentMembers")
@NamedQuery(name = StudentClub.QUERY_STUDENT_CLUB_BY_ID, query = "SELECT DISTINCT sc FROM StudentClub sc LEFT JOIN FETCH sc.studentMembers WHERE sc.id = :param1")
public class StudentClub extends PojoBase implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ALL_STUDENT_CLUBS_QUERY = "StudentClub.findAll";
    public static final String QUERY_STUDENT_CLUB_BY_ID = "StudentClub.findById";

    @Basic(optional = false)
    @Column(name = "name", nullable = false, unique = true, length = 100)
    protected String name;

    @Basic(optional = false)
    @Column(name = "description", nullable = false, length = 100)
    protected String desc;

    @Column(name = "academic", nullable = false, insertable = false, updatable = false)
    protected boolean isAcademic;

    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "studentClubs")
    @JsonIgnore
    protected Set<Student> studentMembers = new HashSet<>();

    @Transient
    protected boolean editable = false;

    public StudentClub() { super(); }
    public StudentClub(boolean isAcademic) { this(); this.isAcademic = isAcademic; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public boolean getAcademic() { return isAcademic; }
    public void setAcademic(boolean isAcademic) { this.isAcademic = isAcademic; }
    public Set<Student> getStudentMembers() { return studentMembers; }
    public void setStudentMembers(Set<Student> studentMembers) { this.studentMembers = studentMembers; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }

    @Override
    public String toString() {
        return "StudentClub[id = " + id + ", name = " + name + ", desc = " + desc + ", isAcademic = " + isAcademic + "]";
    }
}
