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
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PU_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String READ_ALL_PROGRAMS = "SELECT name FROM program";
    //ACMECS01 - Named-query name constants live on the entity they belong to
    //           (Course.ALL_COURSES_QUERY, Course.COURSE_BY_ID_QUERY, ...).
    //           Only raw native-SQL strings that no entity owns are declared here.
    
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

    /**
     * To update a student
     * 
     * @param id - id of entity to update
     * @param studentWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
    	Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            em.merge(studentWithUpdates);
            em.flush();
        }
        return studentWithUpdates;
    }

    /**
     * To delete a student by id
     * 
     * @param id - student id to delete
     */
    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            /* ACMEMS02 - Use the NamedQuery on SecurityUser to find the SecurityUser
               related to this Student, so that when we remove it, the relationship
               from the SECURITY_USER table is not left dangling. */
            SecurityUser sUser = em.createNamedQuery(SecurityUser.SECURITY_USER_BY_STUDENT_ID,SecurityUser.class)
            		.setParameter(PARAM1, id)
            		.getSingleResult();
            em.remove(sUser);
            em.remove(student);
        }
        return student;
    }
    
	@SuppressWarnings("unchecked")
    public List<String> getAllPrograms() {
		List<String> programs = new ArrayList<>();
		try {
			programs = (List<String>) em.createNativeQuery(READ_ALL_PROGRAMS).getResultList();
		}
		catch (Exception e) {
		}
		return programs;
    }

	//ACMECS02 - Add the rest of your CRUD methods here.

	// ------------------------------------------------------------------
	// --- Simon - Course ---
	// ------------------------------------------------------------------

	/**
	 * Read all courses. Uses the named query so the (LAZY) courseRegistrations
	 * collection is initialised before the entity leaves the transaction.
	 *
	 * @return every Course in the database
	 */
	public List<Course> getAllCourses() {
		return em.createNamedQuery(Course.ALL_COURSES_QUERY, Course.class).getResultList();
	}

	/**
	 * Read a single course by primary key.
	 *
	 * @param id - course_id to look for
	 * @return the Course, or null when no such row exists
	 */
	public Course getCourseById(int id) {
		return em.find(Course.class, id);
	}

	/**
	 * Read a single course by its course code (e.g. "CST8277").
	 *
	 * @param courseCode - the course code to look for
	 * @return the Course, or null when no such row exists
	 */
	public Course getCourseByCourseCode(String courseCode) {
		try {
			return em.createNamedQuery(Course.COURSE_BY_CODE_QUERY, Course.class)
					.setParameter(PARAM1, courseCode)
					.getSingleResult();
		}
		catch (NoResultException e) {
			LOG.debug("no Course found with courseCode = {}", courseCode);
			return null;
		}
	}

	/**
	 * Create a new course.
	 *
	 * @param newCourse - the transient Course to insert
	 * @return the managed Course, now carrying its generated id and timestamps
	 */
	@Transactional
	public Course persistCourse(Course newCourse) {
		em.persist(newCourse);
		return newCourse;
	}

	/**
	 * Update an existing course.
	 * <p>
	 * State is copied onto the managed instance rather than merging the detached
	 * object straight from the request body: the incoming JSON has no @Version
	 * value, so merging it directly risks a spurious OptimisticLockException.
	 *
	 * @param id - course_id of the row to update
	 * @param courseWithUpdates - entity carrying the new values
	 * @return the updated Course, or null when no such row exists
	 */
	@Transactional
	public Course updateCourseById(int id, Course courseWithUpdates) {
		Course courseToBeUpdated = getCourseById(id);
		if (courseToBeUpdated == null) {
			return null;
		}
		em.refresh(courseToBeUpdated);
		courseToBeUpdated.setCourseCode(courseWithUpdates.getCourseCode());
		courseToBeUpdated.setCourseTitle(courseWithUpdates.getCourseTitle());
		courseToBeUpdated.setCreditUnits(courseWithUpdates.getCreditUnits());
		courseToBeUpdated.setOnline(courseWithUpdates.getOnline());
		em.merge(courseToBeUpdated);
		em.flush();
		return courseToBeUpdated;
	}

	/**
	 * Delete a course by id.
	 * <p>
	 * course_registration holds a FK to course with ON DELETE NO ACTION, so the
	 * dependent registrations are removed first to avoid a constraint violation.
	 *
	 * @param id - course_id of the row to delete
	 * @return the deleted Course, or null when no such row exists
	 */
	@Transactional
	public Course deleteCourseById(int id) {
		Course course = getCourseById(id);
		if (course == null) {
			return null;
		}
		em.refresh(course);
		for (CourseRegistration cr : new ArrayList<>(course.getCourseRegistrations())) {
			em.remove(cr);
		}
		course.getCourseRegistrations().clear();
		em.remove(course);
		em.flush();
		return course;
	}

	// ------------------------------------------------------------------
	// --- Jessy - Professor / StudentClub ---
	// ------------------------------------------------------------------

	// ------------------------------------------------------------------
	// --- Hadi - CourseRegistration ---
	// ------------------------------------------------------------------

}