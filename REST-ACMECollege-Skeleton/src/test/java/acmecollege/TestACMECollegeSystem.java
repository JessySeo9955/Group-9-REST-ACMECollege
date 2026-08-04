/********************************************************************************************************
 * File:  TestACMECollegeSystem.java
 * Course Materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 */
package acmecollege;

import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Student;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@SuppressWarnings("unused")
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;
    static final int STUDENT_ID = 1;
    static final int OTHER_STUDENT_ID = 2;
    static final int COURSE_ID = 1;
    static final int PROFESSOR_ID = 1;

    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;

    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_get_all_students_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(2));
    }

    @Test
    public void test02_admin_create_course_registration() {
        CourseRegistration registration = new CourseRegistration();
        registration.setYear(2026);
        registration.setSemester("FALL");

        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + STUDENT_ID + "/course/" + COURSE_ID)
            .request()
            .post(Entity.json(registration));

        assertThat(response.getStatus(), is(200));
        CourseRegistration created = response.readEntity(CourseRegistration.class);
        assertThat(created.getStudentId(), is(STUDENT_ID));
        assertThat(created.getCourseId(), is(COURSE_ID));
        assertThat(created.getSemester(), is("FALL"));
    }

    @Test
    public void test03_admin_get_course_registration() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + STUDENT_ID + "/course/" + COURSE_ID)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        CourseRegistration registration = response.readEntity(CourseRegistration.class);
        assertThat(registration.getStudentId(), is(STUDENT_ID));
        assertThat(registration.getCourseId(), is(COURSE_ID));
    }

    @Test
    public void test04_admin_assign_professor_to_registration() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + STUDENT_ID + "/course/" + COURSE_ID + "/professor/" + PROFESSOR_ID)
            .request()
            .put(Entity.json(new CourseRegistration()));

        assertThat(response.getStatus(), is(200));
        CourseRegistration registration = response.readEntity(CourseRegistration.class);
        assertThat(registration.getProfessorId(), is(PROFESSOR_ID));
    }

    @Test
    public void test05_admin_assign_grade_to_registration() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + STUDENT_ID + "/course/" + COURSE_ID + "/grade/A")
            .request()
            .put(Entity.json(new CourseRegistration()));

        assertThat(response.getStatus(), is(200));
        CourseRegistration registration = response.readEntity(CourseRegistration.class);
        assertThat(registration.getLetterGrade(), is("A"));
    }

    @Test
    public void test06_user_can_read_own_course_registrations() {
        Response response = webTarget
            .register(userAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        List<CourseRegistration> registrations = response.readEntity(new GenericType<List<CourseRegistration>>(){});
        assertThat(registrations, is(not(empty())));
    }

    @Test
    public void test07_user_cannot_create_course_registration() {
        CourseRegistration registration = new CourseRegistration();
        registration.setYear(2026);
        registration.setSemester("WINTER");

        Response response = webTarget
            .register(userAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + OTHER_STUDENT_ID + "/course/2")
            .request()
            .post(Entity.json(registration));

        assertThat(response.getStatus(), is(403));
    }

    @Test
    public void test08_admin_delete_course_registration() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + STUDENT_ID + "/course/" + COURSE_ID)
            .request()
            .delete();

        assertThat(response.getStatus(), is(200));
        CourseRegistration deleted = response.readEntity(CourseRegistration.class);
        assertThat(deleted.getStudentId(), is(STUDENT_ID));
        assertThat(deleted.getCourseId(), is(COURSE_ID));
    }

    // ==================================================================
    // --- Simon - Course endpoint tests ---
    //
    // CourseResource declares @RolesAllowed({ADMIN_ROLE}) at class level,
    // so every /course endpoint is admin-only, and POST answers 201 CREATED.
    // ==================================================================

    /**
     * READ (positive): acmecollege-data.sql seeds exactly two courses.
     */
    @Test
    public void test09_admin_get_all_courses() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        List<Course> courses = response.readEntity(new GenericType<List<Course>>(){});
        assertThat(courses, is(not(empty())));
        assertThat(courses, hasSize(2));
    }

    /**
     * NEGATIVE (authorization): a USER_ROLE caller is authenticated but the
     * class-level @RolesAllowed({ADMIN_ROLE}) denies even read access.
     */
    @Test
    public void test10_user_cannot_get_all_courses() {
        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(403));
    }

    /**
     * NEGATIVE (authentication): no Authorization header is rejected by
     * CustomAuthenticationMechanism before any resource method runs.
     */
    @Test
    public void test11_anonymous_cannot_get_all_courses() {
        Response response = webTarget
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();

        assertThat(response.getStatus(), is(401));
    }

    /**
     * READ by id (positive): course 1 is CST8116 in the seed data.
     */
    @Test
    public void test12_admin_get_course_by_id() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/" + COURSE_ID)
            .request()
            .get();

        assertThat(response.getStatus(), is(200));
        Course course = response.readEntity(Course.class);
        assertThat(course, is(notNullValue()));
        assertThat(course.getCourseCode(), is("CST8116"));
    }

    /**
     * NEGATIVE (not found): an id with no matching row yields 404, not 500.
     */
    @Test
    public void test13_admin_get_course_by_id_not_found() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/999999")
            .request()
            .get();

        assertThat(response.getStatus(), is(404));
    }

    /**
     * NEGATIVE (authorization): a USER_ROLE caller may not create a course.
     */
    @Test
    public void test14_user_cannot_create_course() {
        Course newCourse = new Course();
        newCourse.setCourseCode("CST8999");
        newCourse.setCourseTitle("Should Not Be Created");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 0);

        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newCourse));

        assertThat(response.getStatus(), is(403));
    }

    /**
     * FULL CRUD cycle (admin): create, update, delete, confirm gone.
     * Runs last and cleans up after itself so the suite stays repeatable
     * without re-seeding the database.
     */
    @Test
    public void test15_admin_crud_course() throws JsonMappingException, JsonProcessingException {
        // CREATE - CourseResource answers 201 CREATED
        Course newCourse = new Course();
        newCourse.setCourseCode("CST8888");
        newCourse.setCourseTitle("Integration Test Course");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 1);

        Response postResponse = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(Entity.json(newCourse));

        assertThat(postResponse.getStatus(), is(201));
        Course created = postResponse.readEntity(Course.class);
        assertThat(created, is(notNullValue()));
        assertThat(created.getId(), is(not(0)));
        assertThat(created.getCourseCode(), is("CST8888"));
        int newId = created.getId();

        // UPDATE
        created.setCourseTitle("Integration Test Course - Updated");
        Response putResponse = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/" + newId)
            .request()
            .put(Entity.json(created));

        assertThat(putResponse.getStatus(), is(200));
        Course updated = putResponse.readEntity(Course.class);
        assertThat(updated.getCourseTitle(), is("Integration Test Course - Updated"));

        // DELETE
        Response deleteResponse = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/" + newId)
            .request()
            .delete();

        assertThat(deleteResponse.getStatus(), is(200));

        // CONFIRM GONE
        Response getResponse = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/" + newId)
            .request()
            .get();

        assertThat(getResponse.getStatus(), is(404));
    }
}
