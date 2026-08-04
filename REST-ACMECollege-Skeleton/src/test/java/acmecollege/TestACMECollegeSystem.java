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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.Student;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
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
            //.register(userAuth)
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(2));
    }
    
    // TACMECS 01 - Add your meaningful JUnit tests here.

    // ==================================================================
    // --- Simon - Course ---
    //
    // Security contract under test:
    //   GET    /course       ADMIN_ROLE or USER_ROLE
    //   GET    /course/{id}  ADMIN_ROLE or USER_ROLE
    //   POST   /course       ADMIN_ROLE only
    //   PUT    /course/{id}  ADMIN_ROLE only
    //   DELETE /course/{id}  ADMIN_ROLE only
    // ==================================================================

    /**
     * READ (positive, admin): the seed data contains exactly two courses.
     */
    @Test
    public void test02_get_all_courses_with_adminrole() throws JsonMappingException, JsonProcessingException {
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
     * READ (positive, non-admin): a plain USER_ROLE caller may browse the catalogue.
     * This is what makes Course different from Student, where listing is admin-only.
     */
    @Test
    public void test03_get_all_courses_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Course> courses = response.readEntity(new GenericType<List<Course>>(){});
        assertThat(courses, is(not(empty())));
    }

    /**
     * NEGATIVE (authentication): no Authorization header at all must be rejected
     * by CustomAuthenticationMechanism before any resource method runs.
     */
    @Test
    public void test04_get_all_courses_with_no_auth_is_unauthorized() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(401));
    }

    /**
     * READ by id (positive): course 1 is CST8116 in acmecollege-data.sql.
     */
    @Test
    public void test05_get_course_by_id_with_userrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Course course = response.readEntity(Course.class);
        assertThat(course, is(notNullValue()));
        assertThat(course.getCourseCode(), is("CST8116"));
    }

    /**
     * NEGATIVE (not found): an id that does not exist yields 404, not 500.
     */
    @Test
    public void test06_get_course_by_id_not_found() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/999999")
            .request()
            .get();
        assertThat(response.getStatus(), is(404));
    }

    /**
     * NEGATIVE (authorization): a USER_ROLE caller is authenticated but not
     * authorized to create a course - @RolesAllowed({ADMIN_ROLE}) gives 403.
     */
    @Test
    public void test07_add_course_with_userrole_is_forbidden() throws JsonMappingException, JsonProcessingException {
        Course newCourse = new Course();
        newCourse.setCourseCode("CST8999");
        newCourse.setCourseTitle("Should Not Be Created");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 0);

        Response response = webTarget
            .register(userAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newCourse, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(403));
    }

    /**
     * FULL CRUD cycle (admin): create, read back, update, delete, confirm gone.
     * Runs last (highest method name) and cleans up after itself so the suite
     * is repeatable without re-seeding the database.
     */
    @Test
    public void test08_crud_course_with_adminrole() throws JsonMappingException, JsonProcessingException {
        // CREATE
        Course newCourse = new Course();
        newCourse.setCourseCode("CST8888");
        newCourse.setCourseTitle("Integration Test Course");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 1);

        Response postResponse = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(Entity.entity(newCourse, MediaType.APPLICATION_JSON));
        assertThat(postResponse.getStatus(), is(200));
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
            .put(Entity.entity(created, MediaType.APPLICATION_JSON));
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