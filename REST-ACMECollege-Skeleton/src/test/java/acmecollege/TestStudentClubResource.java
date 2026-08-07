package acmecollege;

import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_CLUB_RESOURCE_NAME;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.net.URI;
import java.util.List;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.algonquincollege.cst8277.entity.StudentClub;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

public class TestStudentClubResource {

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    static final int CLUB_ID = 1;
    static final int STUDENT_ID = 1;
    static final int OTHER_STUDENT_ID = 2;

    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    protected WebTarget webTarget;

    @BeforeAll
    public static void oneTimeSetUp() {

        uri = UriBuilder
                .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
                .scheme(HTTP_SCHEMA)
                .host(HOST)
                .port(PORT)
                .build();

        adminAuth = HttpAuthenticationFeature.basic(
                DEFAULT_ADMIN_USER,
                DEFAULT_ADMIN_USER_PASSWORD);

        userAuth = HttpAuthenticationFeature.basic(
                DEFAULT_USER,
                DEFAULT_USER_PASSWORD);
    }

    @BeforeEach
    public void setUp() {

        Client client = ClientBuilder.newClient()
                .register(MyObjectMapperProvider.class)
                .register(new LoggingFeature());

        webTarget = client.target(uri);
    }

    @Test
    public void testGetStudentClubs() {

        Response response = webTarget
                .register(adminAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME)
                .request()
                .get();

        assertThat(response.getStatus(), is(200));

        List<StudentClub> clubs =
                response.readEntity(new GenericType<List<StudentClub>>() {});

        assertThat(clubs, is(not(empty())));
    }

    @Test
    public void testGetStudentClubById() {

        Response response = webTarget
                .register(adminAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME + "/" + CLUB_ID)
                .request()
                .get();

        assertThat(response.getStatus(), is(200));

        StudentClub club = response.readEntity(StudentClub.class);

        assertThat(club, is(notNullValue()));
        assertThat(club.getId(), is(CLUB_ID));
    }

    @Test
    public void testGetStudentClubsByStudent() {

        Response response = webTarget
                .register(adminAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME + "/student/" + STUDENT_ID)
                .request()
                .get();

        assertThat(response.getStatus(), is(200));

        List<StudentClub> clubs =
                response.readEntity(new GenericType<List<StudentClub>>() {});

        assertThat(clubs, is(notNullValue()));
    }

    @Test
    public void testGetUnregisteredStudentClubs() {

        Response response = webTarget
                .register(adminAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME + "/student/" + STUDENT_ID + "/unregistered")
                .request()
                .get();

        assertThat(response.getStatus(), is(200));

        List<StudentClub> clubs =
                response.readEntity(new GenericType<List<StudentClub>>() {});

        assertThat(clubs, is(notNullValue()));
    }

    @Test
    public void testAddClubMembership() {

        Response response = webTarget
                .register(adminAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME + "/" + CLUB_ID + "/student/" + OTHER_STUDENT_ID)
                .request()
                .post(Entity.json(""));

        assertThat(response.getStatus(), is(200));

        StudentClub club = response.readEntity(StudentClub.class);

        assertThat(club, is(notNullValue()));
    }

    @Test
    public void testDeleteClubMembership() {

        Response response = webTarget
                .register(adminAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME + "/" + CLUB_ID + "/student/" + OTHER_STUDENT_ID)
                .request()
                .delete();

        assertThat(response.getStatus(), is(200));

        StudentClub club = response.readEntity(StudentClub.class);

        assertThat(club, is(notNullValue()));
    }

    @Test
    public void testUserCannotAddClubMembership() {

        Response response = webTarget
                .register(userAuth)
                .path(STUDENT_CLUB_RESOURCE_NAME + "/" + CLUB_ID + "/student/" + OTHER_STUDENT_ID)
                .request()
                .post(Entity.json(""));

        assertThat(response.getStatus(), is(403));
    }
}