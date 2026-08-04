/********************************************************************************************************
 * File: ACMECollegeService.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.ejb;

import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PU_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algonquincollege.cst8277.entity.Academic;
import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.CourseRegistrationPK;
import com.algonquincollege.cst8277.entity.NonAcademic;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String READ_ALL_PROGRAMS = "SELECT name FROM program";
    private static final String READ_ALL_DEGREES = "SELECT name FROM degree";
    private static final String READ_ALL_SEMESTERS = "SELECT name FROM semester";
    private static final String READ_ALL_LETTER_GRADES = "SELECT grade FROM letter_grade";

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        return em.createNamedQuery(Student.ALL_STUDENTS_QUERY_NAME, Student.class).getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        return newStudent;
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();
        userForNewStudent.setUsername(DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        userForNewStudent.setPwHash(pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray()));
        userForNewStudent.setStudent(newStudent);
        SecurityRole userRole = em.createNamedQuery(SecurityRole.SECURITY_ROLE_BY_NAME, SecurityRole.class)
            .setParameter(PARAM1, USER_ROLE)
            .getSingleResult();
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        em.persist(userForNewStudent);
    }

    @Transactional
    public Student updateStudentById(int id, Student updates) {
        Student student = getStudentById(id);
        if (student != null) {
            student.setFirstName(updates.getFirstName());
            student.setLastName(updates.getLastName());
            student.setEmail(updates.getEmail());
            student.setPhone(updates.getPhone());
            student.setProgram(updates.getProgram());
            em.merge(student);
        }
        return student;
    }

    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            SecurityUser sUser = findSecurityUserByStudentId(id);
            if (sUser != null) {
                for (SecurityRole role : new ArrayList<>(sUser.getRoles())) {
                    role.getUsers().remove(sUser);
                }
                em.remove(sUser);
            }
            for (CourseRegistration registration : new ArrayList<>(student.getCourseRegistrations())) {
                em.remove(em.contains(registration) ? registration : em.merge(registration));
            }
            for (StudentClub club : new ArrayList<>(student.getStudentClubs())) {
                club.getStudentMembers().remove(student);
                student.getStudentClubs().remove(club);
            }
            em.remove(student);
        }
        return student;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllPrograms() { return readStringList(READ_ALL_PROGRAMS); }
    @SuppressWarnings("unchecked")
    public List<String> getAllDegrees() { return readStringList(READ_ALL_DEGREES); }
    @SuppressWarnings("unchecked")
    public List<String> getAllSemesters() { return readStringList(READ_ALL_SEMESTERS); }
    @SuppressWarnings("unchecked")
    public List<String> getAllLetterGrades() { return readStringList(READ_ALL_LETTER_GRADES); }

    @SuppressWarnings("unchecked")
    private List<String> readStringList(String sql) {
        try { return (List<String>) em.createNativeQuery(sql).getResultList(); }
        catch (Exception e) { return new ArrayList<>(); }
    }

    public List<Course> getAllCourses() { return em.createNamedQuery(Course.ALL_COURSES_QUERY, Course.class).getResultList(); }
    public Course getCourseById(int id) { return em.find(Course.class, id); }
    @Transactional public Course persistCourse(Course course) { em.persist(course); return course; }
    @Transactional public Course updateCourseById(int id, Course updates) {
        Course course = getCourseById(id);
        if (course != null) {
            course.setCourseCode(updates.getCourseCode());
            course.setCourseTitle(updates.getCourseTitle());
            course.setCreditUnits(updates.getCreditUnits());
            course.setOnline(updates.getOnline());
            em.merge(course);
        }
        return course;
    }
    @Transactional public Course deleteCourseById(int id) {
        Course course = getCourseById(id);
        if (course != null) {
            for (CourseRegistration registration : new ArrayList<>(course.getCourseRegistrations())) {
                em.remove(em.contains(registration) ? registration : em.merge(registration));
            }
            em.remove(course);
        }
        return course;
    }

    public List<Professor> getAllProfessors() { return em.createNamedQuery(Professor.ALL_PROFESSORS_QUERY, Professor.class).getResultList(); }
    public Professor getProfessorById(int id) { return em.find(Professor.class, id); }
    @Transactional public Professor persistProfessor(Professor professor) { em.persist(professor); return professor; }
    @Transactional public Professor updateProfessorById(int id, Professor updates) {
        Professor professor = getProfessorById(id);
        if (professor != null) {
            professor.setFirstName(updates.getFirstName());
            professor.setLastName(updates.getLastName());
            professor.setDegree(updates.getDegree());
            em.merge(professor);
        }
        return professor;
    }
    @Transactional public Professor deleteProfessorById(int id) {
        Professor professor = getProfessorById(id);
        if (professor != null) {
            for (CourseRegistration registration : new ArrayList<>(professor.getCourseRegistrations())) {
                registration.setProfessor(null);
                em.merge(registration);
            }
            em.remove(professor);
        }
        return professor;
    }

    public List<StudentClub> getAllStudentClubs() { return em.createNamedQuery(StudentClub.ALL_STUDENT_CLUBS_QUERY, StudentClub.class).getResultList(); }
    public StudentClub getStudentClubById(int id) { return em.find(StudentClub.class, id); }
    @Transactional public StudentClub persistStudentClub(StudentClub club) {
        StudentClub entity = club.getAcademic() ? new Academic() : new NonAcademic();
        entity.setName(club.getName());
        entity.setDesc(club.getDesc());
        em.persist(entity);
        return entity;
    }
    @Transactional public StudentClub updateStudentClubById(int id, StudentClub updates) {
        StudentClub club = getStudentClubById(id);
        if (club != null) {
            club.setName(updates.getName());
            club.setDesc(updates.getDesc());
            em.merge(club);
        }
        return club;
    }
    @Transactional public StudentClub deleteStudentClubById(int id) {
        StudentClub club = getStudentClubById(id);
        if (club != null) {
            for (Student student : new ArrayList<>(club.getStudentMembers())) {
                student.getStudentClubs().remove(club);
            }
            club.getStudentMembers().clear();
            em.remove(club);
        }
        return club;
    }
    @Transactional public StudentClub addStudentToClub(int clubId, int studentId) {
        StudentClub club = getStudentClubById(clubId);
        Student student = getStudentById(studentId);
        if (club != null && student != null) {
            club.getStudentMembers().add(student);
            student.getStudentClubs().add(club);
            em.merge(club);
            em.merge(student);
        }
        return club;
    }

    public List<CourseRegistration> getAllCourseRegistrations() {
        return em.createNamedQuery(CourseRegistration.ALL_COURSE_REGISTRATIONS_QUERY_NAME, CourseRegistration.class).getResultList();
    }
    public List<CourseRegistration> getCourseRegistrationsForStudent(int studentId) {
        return em.createNamedQuery(CourseRegistration.QUERY_COURSE_REGISTRATIONS_BY_STUDENT, CourseRegistration.class)
            .setParameter(PARAM1, studentId)
            .getResultList();
    }
    public CourseRegistration getCourseRegistration(int studentId, int courseId) {
        return em.find(CourseRegistration.class, new CourseRegistrationPK(studentId, courseId));
    }
    @Transactional public CourseRegistration createCourseRegistration(int studentId, int courseId, CourseRegistration registration) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        if (student == null || course == null) { return null; }
        CourseRegistration entity = new CourseRegistration();
        entity.setStudent(student);
        entity.setCourse(course);
        entity.setYear(registration.getYear());
        entity.setSemester(registration.getSemester());
        entity.setLetterGrade(registration.getLetterGrade());
        em.persist(entity);
        return entity;
    }
    @Transactional public CourseRegistration updateCourseRegistration(int studentId, int courseId, CourseRegistration updates) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        if (registration != null) {
            registration.setYear(updates.getYear());
            registration.setSemester(updates.getSemester());
            registration.setLetterGrade(updates.getLetterGrade());
            em.merge(registration);
        }
        return registration;
    }
    @Transactional public CourseRegistration assignProfessor(int studentId, int courseId, int professorId) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        Professor professor = getProfessorById(professorId);
        if (registration != null && professor != null) {
            registration.setProfessor(professor);
            em.merge(registration);
        }
        return registration;
    }
    @Transactional public CourseRegistration assignGrade(int studentId, int courseId, String grade) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        if (registration != null) {
            registration.setLetterGrade(grade);
            em.merge(registration);
        }
        return registration;
    }
    @Transactional public CourseRegistration deleteCourseRegistration(int studentId, int courseId) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        if (registration != null) {
            em.remove(registration);
        }
        return registration;
    }

    private SecurityUser findSecurityUserByStudentId(int studentId) {
        try {
            return em.createNamedQuery(SecurityUser.SECURITY_USER_BY_STUDENT_ID, SecurityUser.class)
                .setParameter(PARAM1, studentId)
                .getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }
}

