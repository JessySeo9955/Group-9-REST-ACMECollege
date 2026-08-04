/********************************************************************************************************
 * File: TestACMECollegeSystem.java Course Materials CST 8277
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
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_CLUB_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;
import com.algonquincollege.cst8277.rest.resource.MyObjectMapperProvider;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;
    static int createdCourseId;
    static int createdProfessorId;
    static int createdClubId;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        MethodHandles.lookup().lookupClass();
        uri = UriBuilder.fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA).host(HOST).port(PORT).build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;

    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test public void test01_get_all_students_with_admin_role() {
        Response response = webTarget.register(adminAuth).path(STUDENT_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        assertThat(students, hasSize(2));
    }

    @Test public void test02_user_role_cannot_get_all_students() {
        Response response = webTarget.register(userAuth).path(STUDENT_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(403));
    }

    @Test public void test03_user_role_can_get_own_student() {
        Response response = webTarget.register(userAuth).path(STUDENT_RESOURCE_NAME + "/1").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(Student.class).getId(), is(1));
    }

    @Test public void test04_user_role_cannot_get_other_student() {
        Response response = webTarget.register(userAuth).path(STUDENT_RESOURCE_NAME + "/2").request().get();
        assertThat(response.getStatus(), is(403));
    }

    @Test public void test05_admin_can_get_courses() {
        Response response = webTarget.register(adminAuth).path(COURSE_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(new GenericType<List<Course>>(){}), is(not(empty())));
    }

    @Test public void test06_user_role_cannot_get_courses() {
        Response response = webTarget.register(userAuth).path(COURSE_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(403));
    }

    @Test public void test07_admin_can_create_course() {
        Course course = new Course();
        course.setCourseCode("TST1001");
        course.setCourseTitle("Integration Testing");
        course.setCreditUnits(3);
        course.setOnline((short) 1);
        Response response = webTarget.register(adminAuth).path(COURSE_RESOURCE_NAME).request().post(Entity.json(course));
        assertThat(response.getStatus(), is(201));
        createdCourseId = response.readEntity(Course.class).getId();
        assertThat(createdCourseId > 0, is(true));
    }

    @Test public void test08_admin_can_update_course() {
        Course course = new Course();
        course.setCourseCode("TST1002");
        course.setCourseTitle("Updated Integration Testing");
        course.setCreditUnits(4);
        course.setOnline((short) 0);
        Response response = webTarget.register(adminAuth).path(COURSE_RESOURCE_NAME + "/" + createdCourseId).request().put(Entity.json(course));
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(Course.class).getCourseTitle(), is("Updated Integration Testing"));
    }

    @Test public void test09_admin_can_get_professors() {
        Response response = webTarget.register(adminAuth).path(PROFESSOR_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(new GenericType<List<Professor>>(){}), is(not(empty())));
    }

    @Test public void test10_admin_can_create_professor() {
        Professor professor = new Professor();
        professor.setFirstName("Ada");
        professor.setLastName("Lovelace");
        professor.setDegree("Doctor of Philosophy");
        Response response = webTarget.register(adminAuth).path(PROFESSOR_RESOURCE_NAME).request().post(Entity.json(professor));
        assertThat(response.getStatus(), is(201));
        createdProfessorId = response.readEntity(Professor.class).getId();
        assertThat(createdProfessorId > 0, is(true));
    }

    @Test public void test11_admin_can_update_professor() {
        Professor professor = new Professor();
        professor.setFirstName("Ada");
        professor.setLastName("Byron");
        professor.setDegree("Doctor of Science");
        Response response = webTarget.register(adminAuth).path(PROFESSOR_RESOURCE_NAME + "/" + createdProfessorId).request().put(Entity.json(professor));
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(Professor.class).getLastName(), is("Byron"));
    }

    @Test public void test12_admin_can_get_degree_options() {
        Response response = webTarget.register(adminAuth).path(PROFESSOR_RESOURCE_NAME + "/degree").request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(new GenericType<List<String>>(){}), is(not(empty())));
    }

    @Test public void test13_anyone_can_get_student_clubs() {
        Response response = webTarget.path(STUDENT_CLUB_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(new GenericType<List<StudentClub>>(){}), is(not(empty())));
    }

    @Test public void test14_admin_can_create_student_club() {
        StudentClub club = new StudentClub(false);
        club.setName("Testing Club");
        club.setDesc("Students who test REST APIs.");
        Response response = webTarget.register(adminAuth).path(STUDENT_CLUB_RESOURCE_NAME).request().post(Entity.json(club));
        assertThat(response.getStatus(), is(201));
        createdClubId = response.readEntity(StudentClub.class).getId();
        assertThat(createdClubId > 0, is(true));
    }

    @Test public void test15_admin_can_update_student_club() {
        StudentClub club = new StudentClub(false);
        club.setName("REST Testing Club");
        club.setDesc("Updated club description.");
        Response response = webTarget.register(adminAuth).path(STUDENT_CLUB_RESOURCE_NAME + "/" + createdClubId).request().put(Entity.json(club));
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(StudentClub.class).getName(), is("REST Testing Club"));
    }

    @Test public void test16_admin_can_create_club_membership() {
        Response response = webTarget.register(adminAuth).path(STUDENT_CLUB_RESOURCE_NAME + "/" + createdClubId + "/student/1").request().post(Entity.json(""));
        assertThat(response.getStatus(), is(200));
    }

    @Test public void test17_admin_can_create_course_registration() {
        CourseRegistration registration = new CourseRegistration();
        registration.setYear(2026);
        registration.setSemester("FALL");
        Response response = webTarget.register(adminAuth).path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/2").request().post(Entity.json(registration));
        assertThat(response.getStatus(), is(201));
        assertThat(response.readEntity(CourseRegistration.class).getStudentId(), is(1));
    }

    @Test public void test18_admin_can_assign_professor_to_registration() {
        Response response = webTarget.register(adminAuth).path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/2/professor/1").request().put(Entity.json(""));
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(CourseRegistration.class).getProfessorId(), is(1));
    }

    @Test public void test19_admin_can_assign_grade_to_registration() {
        Response response = webTarget.register(adminAuth).path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/2/grade/A").request().put(Entity.json(""));
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(CourseRegistration.class).getLetterGrade(), is("A"));
    }

    @Test public void test20_user_can_read_own_course_registrations() {
        Response response = webTarget.register(userAuth).path(COURSE_REGISTRATION_RESOURCE_NAME).request().get();
        assertThat(response.getStatus(), is(200));
        assertThat(response.readEntity(new GenericType<List<CourseRegistration>>(){}), is(not(empty())));
    }

    @Test public void test21_user_can_read_own_specific_course_registration() {
        Response response = webTarget.register(userAuth).path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/2").request().get();
        assertThat(response.getStatus(), is(200));
    }

    @Test public void test22_user_cannot_create_course_registration() {
        CourseRegistration registration = new CourseRegistration();
        registration.setYear(2026);
        registration.setSemester("FALL");
        Response response = webTarget.register(userAuth).path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/1").request().post(Entity.json(registration));
        assertThat(response.getStatus(), is(403));
    }

    @Test public void test23_admin_can_delete_course_registration() {
        Response response = webTarget.register(adminAuth).path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/2").request().delete();
        assertThat(response.getStatus(), is(200));
    }

    @Test public void test24_admin_can_delete_student_club() {
        Response response = webTarget.register(adminAuth).path(STUDENT_CLUB_RESOURCE_NAME + "/" + createdClubId).request().delete();
        assertThat(response.getStatus(), is(200));
    }

    @Test public void test25_admin_can_delete_professor_and_course() {
        Response professorResponse = webTarget.register(adminAuth).path(PROFESSOR_RESOURCE_NAME + "/" + createdProfessorId).request().delete();
        assertThat(professorResponse.getStatus(), is(200));
        Response courseResponse = webTarget.register(adminAuth).path(COURSE_RESOURCE_NAME + "/" + createdCourseId).request().delete();
        assertThat(courseResponse.getStatus(), is(200));
    }
}
