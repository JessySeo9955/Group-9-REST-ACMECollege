/********************************************************************************************************
 * File:  ACMECollegeService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package com.algonquincollege.cst8277.ejb;

import static com.algonquincollege.cst8277.entity.Student.ALL_STUDENTS_QUERY_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utility.MyConstants.PARAM2;
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

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.CourseRegistrationPK;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger();

    private static final String READ_ALL_PROGRAMS = "SELECT name FROM program";
    private static final String READ_ALL_SEMESTERS = "SELECT name FROM semester";
    private static final String READ_ALL_LETTER_GRADES = "SELECT grade FROM letter_grade";
    private static final String READ_ALL_DEGREES = "SELECT name FROM degree";

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Student> cq = cb.createQuery(Student.class);
        cq.select(cq.from(Student.class));
        return em.createQuery(cq).getResultList();
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
        userForNewStudent.setUsername(
            DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);
        SecurityRole userRole = em.createNamedQuery(SecurityRole.SECURITY_ROLE_BY_NAME, SecurityRole.class)
            .setParameter(PARAM1, USER_ROLE)
            .getSingleResult();
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        em.persist(userForNewStudent);
    }

    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
        Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            studentWithUpdates.setId(id);
            em.merge(studentWithUpdates);
            em.flush();
        }
        return studentWithUpdates;
    }

    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            try {
                TypedQuery<SecurityUser> findUser = em.createNamedQuery(SecurityUser.SECURITY_USER_BY_STUDENT_ID, SecurityUser.class)
                    .setParameter(PARAM1, id);
                SecurityUser sUser = findUser.getSingleResult();
                em.remove(sUser);
            }
            catch (NoResultException e) {
                LOG.debug("no SecurityUser linked to student {}", id);
            }
            em.remove(student);
        }
        return student;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllPrograms() {
        List<String> programs = new ArrayList<>();
        try {
            programs = em.createNativeQuery(READ_ALL_PROGRAMS).getResultList();
        }
        catch (Exception e) {
            LOG.debug("could not load programs", e);
        }
        return programs;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllDegrees() {
        List<String> degrees = new ArrayList<>();
        try {
            degrees = em.createNativeQuery(READ_ALL_DEGREES).getResultList();
        }
        catch (Exception e) {
            LOG.debug("could not load degrees", e);
        }
        return degrees;
    }

    public List<Professor> getAllProfessors() {
        return em.createNamedQuery(Professor.ALL_PROFESSORS_QUERY, Professor.class).getResultList();
    }

    public Professor getProfessorById(int id) {
        return em.find(Professor.class, id);
    }

    @Transactional
    public Professor persistProfessor(Professor professor) {
        em.persist(professor);
        return professor;
    }

    @Transactional
    public Professor updateProfessorById(int id, Professor professorWithUpdates) {
        Professor professor = getProfessorById(id);
        if (professor != null) {
            em.refresh(professor);
            professorWithUpdates.setId(id);
            em.merge(professorWithUpdates);
            em.flush();
        }
        return professorWithUpdates;
    }

    @Transactional
    public Professor deleteProfessorById(int id) {
        Professor professor = getProfessorById(id);
        if (professor != null) {
            em.refresh(professor);
            em.remove(professor);
        }
        return professor;
    }


    public List<Course> getAllCourses() {
        return em.createNamedQuery(Course.ALL_COURSES_QUERY, Course.class).getResultList();
    }

    @Transactional
    public Course persistCourse(Course course) {
        em.persist(course);
        return course;
    }

    @Transactional
    public Course updateCourseById(int id, Course updates) {
        Course course = getCourseById(id);
        if (course == null) {
            return null;
        }
        course.setCourseCode(updates.getCourseCode());
        course.setCourseTitle(updates.getCourseTitle());
        course.setCreditUnits(updates.getCreditUnits());
        course.setOnline(updates.getOnline());
        em.merge(course);
        em.flush();
        return course;
    }

    @Transactional
    public Course deleteCourseById(int id) {
        Course course = getCourseById(id);
        if (course != null) {
            em.remove(course);
        }
        return course;
    }

    public List<StudentClub> getAllStudentClubs() {
        return em.createNamedQuery(StudentClub.ALL_STUDENT_CLUBS_QUERY, StudentClub.class).getResultList();
    }

    public StudentClub getStudentClubById(int id) {
        return em.find(StudentClub.class, id);
    }
    

    @Transactional
    public StudentClub persistStudentClub(StudentClub club) {
        em.persist(club);
        return club;
    }

    @Transactional
    public StudentClub updateStudentClubById(int id, StudentClub updates) {
        StudentClub club = getStudentClubById(id);
        if (club == null) {
            return null;
        }
        club.setName(updates.getName());
        club.setDesc(updates.getDesc());
        club.setAcademic(updates.getAcademic());
        em.merge(club);
        em.flush();
        return club;
    }

    @Transactional
    public StudentClub deleteStudentClubById(int id) {
        StudentClub club = getStudentClubById(id);
        if (club != null) {
            em.remove(club);
        }
        return club;
    }

    @Transactional
    public StudentClub addStudentToClub(int clubId, int studentId) {
    	
    	System.out.println("========================== addStudentToClub ===========");
 	   System.out.println("========================== addStudentToClub =========== clubId: "+ clubId);
 	   System.out.println("========================== addStudentToClub =========== selectedStudentId: " + studentId);
    	
        StudentClub club = getStudentClubById(clubId);
        Student student = getStudentById(studentId);
        
        System.out.println("========================== addStudentToClub ===========" + club);
        System.out.println("========================== addStudentToClub ===========" + student);
        if (club == null || student == null) {
            return null;
        }
        student.getStudentClubs().add(club);          // Owning side
        club.getStudentMembers().add(student); // Keep both sides in sync

        em.merge(student); // Merge the owning side
        return club;
    }
    public Course getCourseById(int id) {
        return em.find(Course.class, id);
    }

    public List<CourseRegistration> getAllCourseRegistrations() {
        return em.createNamedQuery(CourseRegistration.ALL_COURSE_REGISTRATIONS_QUERY_NAME, CourseRegistration.class)
            .getResultList();
    }

    public List<CourseRegistration> getCourseRegistrationsForStudent(int studentId) {
        return em.createNamedQuery(CourseRegistration.QUERY_COURSE_REGISTRATIONS_BY_STUDENT, CourseRegistration.class)
            .setParameter(PARAM1, studentId)
            .getResultList();
    }

    public CourseRegistration getCourseRegistration(int studentId, int courseId) {
        try {
            return em.createNamedQuery(CourseRegistration.QUERY_SPECIFIC_COURSE_REGISTRATION, CourseRegistration.class)
                .setParameter(PARAM1, studentId)
                .setParameter(PARAM2, courseId)
                .getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public CourseRegistration persistCourseRegistration(int studentId, int courseId, CourseRegistration registration) {
        Student student = getStudentById(studentId);
        Course course = getCourseById(courseId);
        if (student == null || course == null || getCourseRegistration(studentId, courseId) != null) {
            return null;
        }
        registration.setStudent(student);
        registration.setCourse(course);
        if (registration.getYear() == 0) {
            registration.setYear(java.time.Year.now().getValue());
        }
        if (registration.getSemester() == null || registration.getSemester().isBlank()) {
            registration.setSemester("FALL");
        }
        em.persist(registration);
        return registration;
    }

    @Transactional
    public CourseRegistration updateCourseRegistration(int studentId, int courseId, CourseRegistration registrationWithUpdates) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        if (registration == null) {
            return null;
        }
        registration.setYear(registrationWithUpdates.getYear());
        registration.setSemester(registrationWithUpdates.getSemester());
        registration.setLetterGrade(registrationWithUpdates.getLetterGrade());
        em.merge(registration);
        em.flush();
        return registration;
    }

    @Transactional
    public CourseRegistration assignProfessorToCourseRegistration(int studentId, int courseId, int professorId) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        Professor professor = getProfessorById(professorId);
        if (registration == null || professor == null) {
            return null;
        }
        registration.setProfessor(professor);
        em.merge(registration);
        em.flush();
        return registration;
    }

    @Transactional
    public CourseRegistration assignGradeToCourseRegistration(int studentId, int courseId, String grade) {
        CourseRegistration registration = getCourseRegistration(studentId, courseId);
        if (registration == null || grade == null || grade.isBlank()) {
            return null;
        }
        registration.setLetterGrade(grade);
        em.merge(registration);
        em.flush();
        return registration;
    }

    @Transactional
    public CourseRegistration deleteCourseRegistration(int studentId, int courseId) {
        CourseRegistration registration = em.find(CourseRegistration.class, new CourseRegistrationPK(studentId, courseId));
        if (registration != null) {
            em.remove(registration);
        }
        return registration;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllSemesters() {
        return em.createNativeQuery(READ_ALL_SEMESTERS).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<String> getAllLetterGrades() {
        return em.createNativeQuery(READ_ALL_LETTER_GRADES).getResultList();
    }

	public List<StudentClub> getStudentClubsByStudent(int studentId) {
		return em.createNamedQuery(
	            Student.STUDENT_CLUBS_QUERY,
	            StudentClub.class)
	        .setParameter(PARAM1, studentId)
	        .getResultList();
	}
	
	public List<StudentClub> getUnregisteredStudentClubs(int studentId) {

	    return em.createNamedQuery(
	            Student.UNREGISTERED_STUDENT_CLUBS_QUERY,
	            StudentClub.class)
	        .setParameter(PARAM1, studentId)
	        .getResultList();
	}

	@Transactional
	public StudentClub removeStudentFromClub(int clubId, int studentId) {

	    StudentClub club = getStudentClubById(clubId);
	    Student student = getStudentById(studentId);

	    if (club == null || student == null) {
	        return null;
	    }

	 // Remove from the owning side
	    student.getStudentClubs().remove(club);

	    // Keep the inverse side in sync
	    club.getStudentMembers().remove(student);

	    em.merge(club);
	    em.flush();

	    return club;
	}
	
}