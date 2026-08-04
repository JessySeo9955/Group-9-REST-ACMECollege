/********************************************************************************************************
 * File: CourseRegistrationController.java
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.rest.resource.MyObjectMapperProvider;
import com.algonquincollege.cst8277.utility.MyConstants;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Named("courseRegistrationController")
@SessionScoped
public class CourseRegistrationController implements Serializable, MyConstants {
    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;

    @Inject
    protected ExternalContext externalContext;

    @Inject
    protected ServletContext sc;

    @Inject
    protected LoginBean loginBean;

    protected List<CourseRegistration> listOfCourseRegistrations;
    protected List<String> listOfSemesters;
    protected List<String> listOfLetterGrades;

    protected int studentId;
    protected int courseId;
    protected int professorId;
    protected int year = java.time.Year.now().getValue();
    protected String semester = "FALL";
    protected String letterGrade = "A";

    static URI uri;
    static HttpAuthenticationFeature auth;
    protected Client client;
    protected WebTarget webTarget;

    @PostConstruct
    public void initialize() {
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();

        auth = HttpAuthenticationFeature.basic(loginBean.getUsername(), loginBean.getPassword());
        client = ClientBuilder.newClient(new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
        loadCourseRegistrations();
    }

    public void loadCourseRegistrations() {
        Response response = webTarget
            .register(auth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME)
            .request()
            .get();
        listOfCourseRegistrations = response.readEntity(new GenericType<List<CourseRegistration>>(){});
    }

    public List<CourseRegistration> getCourseRegistrations() {
        return listOfCourseRegistrations;
    }

    public String createRegistration() {
        CourseRegistration registration = new CourseRegistration();
        registration.setYear(year);
        registration.setSemester(semester);
        Response response = webTarget
            .register(auth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + studentId + "/course/" + courseId)
            .request()
            .post(Entity.json(registration));
        if (response.getStatus() == 200) {
            loadCourseRegistrations();
        }
        return null;
    }

    public String assignProfessor() {
        Response response = webTarget
            .register(auth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + studentId + "/course/" + courseId + "/professor/" + professorId)
            .request()
            .put(Entity.json(new CourseRegistration()));
        if (response.getStatus() == 200) {
            loadCourseRegistrations();
        }
        return null;
    }

    public String assignGrade() {
        Response response = webTarget
            .register(auth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + studentId + "/course/" + courseId + "/grade/" + letterGrade)
            .request()
            .put(Entity.json(new CourseRegistration()));
        if (response.getStatus() == 200) {
            loadCourseRegistrations();
        }
        return null;
    }

    public String refreshCourseRegistrationForm() {
        Iterator<FacesMessage> iterator = facesContext.getMessages();
        while (iterator.hasNext()) {
            iterator.remove();
        }
        loadCourseRegistrations();
        return MAIN_PAGE_REDIRECT;
    }

    public List<String> getSemesters() {
        Response response = webTarget
            .register(auth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + SEMESTER_RESOURCE_PATH)
            .request()
            .get();
        listOfSemesters = response.readEntity(new GenericType<List<String>>(){});
        return listOfSemesters;
    }

    public List<String> getLetterGrades() {
        Response response = webTarget
            .register(auth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + LETTER_GRADE_RESOURCE_PATH)
            .request()
            .get();
        listOfLetterGrades = response.readEntity(new GenericType<List<String>>(){});
        return listOfLetterGrades;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getProfessorId() {
        return professorId;
    }

    public void setProfessorId(int professorId) {
        this.professorId = professorId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getLetterGrade() {
        return letterGrade;
    }

    public void setLetterGrade(String letterGrade) {
        this.letterGrade = letterGrade;
    }
}