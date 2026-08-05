package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.StudentClub;
import com.algonquincollege.cst8277.rest.resource.MyObjectMapperProvider;
import com.algonquincollege.cst8277.utility.MyConstants;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

@Named("collegeController")
@SessionScoped
public class CollegeController implements Serializable, MyConstants {
    private static final long serialVersionUID = 1L;

    @Inject protected LoginBean loginBean;

    protected Client client;
    protected WebTarget webTarget;
    protected List<Course> courses;
    protected List<Professor> professors;
    protected List<StudentClub> studentClubs;
    protected List<StudentClub> registeredClubs;
    protected List<StudentClub> unRegisteredClubs;
    protected List<CourseRegistration> courseRegistrations;
    protected List<String> degrees;
    protected List<String> semesters;
    protected List<String> letterGrades;

    protected Course newCourse = new Course();
    protected Professor newProfessor = new Professor();
    protected StudentClub newStudentClub = new StudentClub(false);
    protected CourseRegistration newRegistration = new CourseRegistration();
    protected int selectedStudentId = 1;
    protected int selectedCourseId = 1;
    protected int selectedProfessorId = 1;
    protected int selectedClubId = 1;
    protected String selectedGrade = "A";

    @PostConstruct
    public void initialize() {
        URI uri = UriBuilder.fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA).host(HOST).port(PORT).build();
        client = ClientBuilder.newClient(new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    protected WebTarget target() { return webTarget.register(HttpAuthenticationFeature.basic(loginBean.getUsername(), loginBean.getPassword())); }

    public void loadCourses() { courses = target().path(COURSE_RESOURCE_NAME).request().get(new GenericType<List<Course>>(){}); }
    public void loadProfessors() { professors = target().path(PROFESSOR_RESOURCE_NAME).request().get(new GenericType<List<Professor>>(){}); }
    public void loadStudentClubs() { 
    	studentClubs = target().path(STUDENT_CLUB_RESOURCE_NAME).request().get(new GenericType<List<StudentClub>>(){});
    }
    public void loadCourseRegistrations() { courseRegistrations = target().path(COURSE_REGISTRATION_RESOURCE_NAME).request().get(new GenericType<List<CourseRegistration>>(){}); }
    public void loadDegrees() { degrees = target().path(PROFESSOR_RESOURCE_NAME + DEGREE_RESOURCE_PATH).request().get(new GenericType<List<String>>(){}); }
    public void loadSemesters() { semesters = target().path(COURSE_REGISTRATION_RESOURCE_NAME + SEMESTER_RESOURCE_PATH).request().get(new GenericType<List<String>>(){}); }
    public void loadLetterGrades() { letterGrades = target().path(COURSE_REGISTRATION_RESOURCE_NAME + LETTER_GRADE_RESOURCE_PATH).request().get(new GenericType<List<String>>(){}); }

    public void addCourse() { target().path(COURSE_RESOURCE_NAME).request().post(Entity.json(newCourse)); newCourse = new Course(); loadCourses(); }
    public void updateCourse(Course course) { target().path(COURSE_RESOURCE_NAME + "/" + course.getId()).request().put(Entity.json(course)); course.setEditable(false); loadCourses(); }
    public void deleteCourse(int id) { target().path(COURSE_RESOURCE_NAME + "/" + id).request().delete(); loadCourses(); }

    public void addProfessor() { target().path(PROFESSOR_RESOURCE_NAME).request().post(Entity.json(newProfessor)); newProfessor = new Professor(); loadProfessors(); }
    public void updateProfessor(Professor professor) { target().path(PROFESSOR_RESOURCE_NAME + "/" + professor.getId()).request().put(Entity.json(professor)); professor.setEditable(false); loadProfessors(); }
    public void deleteProfessor(int id) { target().path(PROFESSOR_RESOURCE_NAME + "/" + id).request().delete(); loadProfessors(); }

    public void addStudentClub() { target().path(STUDENT_CLUB_RESOURCE_NAME).request().post(Entity.json(newStudentClub)); newStudentClub = new StudentClub(false); loadStudentClubs(); }
    public void updateStudentClub(StudentClub club) { target().path(STUDENT_CLUB_RESOURCE_NAME + "/" + club.getId()).request().put(Entity.json(club)); club.setEditable(false); loadStudentClubs(); }
    public void deleteStudentClub(int id) { target().path(STUDENT_CLUB_RESOURCE_NAME + "/" + id).request().delete(); loadStudentClubs(); }

    public void addCourseRegistration() { target().path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + selectedStudentId + "/course/" + selectedCourseId).request().post(Entity.json(newRegistration)); newRegistration = new CourseRegistration(); loadCourseRegistrations(); }
    public void deleteCourseRegistration(CourseRegistration registration) { target().path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + registration.getStudentId() + "/course/" + registration.getCourseId()).request().delete(); loadCourseRegistrations(); }
    public void assignProfessor() { target().path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + selectedStudentId + "/course/" + selectedCourseId + "/professor/" + selectedProfessorId).request().put(Entity.json("")); loadCourseRegistrations(); }
    public void assignGrade() { target().path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + selectedStudentId + "/course/" + selectedCourseId + "/grade/" + selectedGrade).request().put(Entity.json("")); loadCourseRegistrations(); }
    public void addClubMembership() { 
    	target().path(STUDENT_CLUB_RESOURCE_NAME + "/" + selectedClubId + "/student/" + selectedStudentId).request().post(Entity.json("")); loadStudentClubs(); }

    public List<Course> getCourses() { if (courses == null) loadCourses(); return courses; }
    public List<Professor> getProfessors() { if (professors == null) loadProfessors(); return professors; }
    public List<StudentClub> getStudentClubs() { if (studentClubs == null) loadStudentClubs(); return studentClubs; }
    public List<StudentClub> getRegisteredClubs() { if (registeredClubs == null) return new ArrayList<>(); return registeredClubs; }
    public List<StudentClub> getUnRegisteredClubs() { if (unRegisteredClubs == null) return new ArrayList<>(); return unRegisteredClubs; }
    public List<CourseRegistration> getCourseRegistrations() { if (courseRegistrations == null) loadCourseRegistrations(); return courseRegistrations; }
    public List<String> getDegrees() { if (degrees == null) loadDegrees(); return degrees; }
    public List<String> getSemesters() { if (semesters == null) loadSemesters(); return semesters; }
    public List<String> getLetterGrades() { if (letterGrades == null) loadLetterGrades(); return letterGrades; }
    
    public Course getNewCourse() { return newCourse; }
    public Professor getNewProfessor() { return newProfessor; }
    public StudentClub getNewStudentClub() { return newStudentClub; }
    public CourseRegistration getNewRegistration() { return newRegistration; }
    public int getSelectedStudentId() { return selectedStudentId; }
    public void setSelectedStudentId(int selectedStudentId) { this.selectedStudentId = selectedStudentId; }
    public int getSelectedCourseId() { return selectedCourseId; }
    public void setSelectedCourseId(int selectedCourseId) { this.selectedCourseId = selectedCourseId; }
    public int getSelectedProfessorId() { return selectedProfessorId; }
    public void setSelectedProfessorId(int selectedProfessorId) { this.selectedProfessorId = selectedProfessorId; }
    public int getSelectedClubId() { return selectedClubId; }
    public void setSelectedClubId(int selectedClubId) { this.selectedClubId = selectedClubId; }
    public String getSelectedGrade() { return selectedGrade; }
    public void setSelectedGrade(String selectedGrade) { this.selectedGrade = selectedGrade; }
    
    public List<StudentClub> loadStudentMemberships() {
    	registeredClubs = target().path(STUDENT_CLUB_RESOURCE_NAME+"/student/" + this.selectedStudentId).request().get(new GenericType<List<StudentClub>>(){});
    	unRegisteredClubs= target().path(STUDENT_CLUB_RESOURCE_NAME+"/student/" + this.selectedStudentId + "/unregistered").request().get(new GenericType<List<StudentClub>>(){});
    	return null;  
    }
    
    public void deleteStudentFromClub(int clubId) {
	   target()
       .path(STUDENT_CLUB_RESOURCE_NAME + "/" + clubId + "/student/" + selectedStudentId)
       .request()
       .delete();

       loadStudentMemberships();
    }
    
    public void addStudentToClub(int clubId) {
        Response response = target()
                .path(STUDENT_CLUB_RESOURCE_NAME + "/" + clubId + "/student/" + selectedStudentId)
                .request()
                .post(Entity.json("{}"));

        if (response.hasEntity()) {
            System.out.println("Response Body: " + response.readEntity(String.class));
        }

        response.close();

        loadStudentMemberships();
    }
}
