package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.time.Year;
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
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;
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
    protected CourseRegistration newRegistration = freshRegistration();

    /*
     * The course-registration screens (Course Registration / Assign Professor / Assign Grade) keep
     * their own selection state. They used to share 'selectedStudentId' with the Club Membership
     * screen, which resets it to -1 - that silently turned every later registration call into an
     * HTTP 400/404 against student -1.
     */
    protected int regStudentId;
    protected int regCourseId;
    protected int regProfessorId;
    protected String selectedGrade = "A";

    // Club Membership screen only.
    protected int selectedStudentId = 1;
    protected int selectedClubId = 1;

    @PostConstruct
    public void initialize() {
        URI uri = UriBuilder.fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA).host(HOST).port(PORT).build();
        client = ClientBuilder.newClient(new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);
    }

    protected WebTarget target() { return webTarget.register(HttpAuthenticationFeature.basic(loginBean.getUsername(), loginBean.getPassword())); }

    protected static CourseRegistration freshRegistration() {
        CourseRegistration registration = new CourseRegistration();
        registration.setYear(Year.now().getValue());
        return registration;
    }

    protected void addMessage(FacesMessage.Severity severity, String text) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(severity, text, null));
        }
    }

    /**
     * Reports a failed REST call to the user. Without this every non-2xx response was discarded,
     * so a rejected request looked exactly like a button that does nothing.
     */
    protected boolean succeeded(Response response, String what) {
        if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
            return true;
        }
        String detail = "";
        try {
            if (response.hasEntity()) {
                detail = response.readEntity(String.class);
            }
        }
        catch (RuntimeException ignored) {
            // body already consumed or not readable as text - the status code is enough
        }
        addMessage(FacesMessage.SEVERITY_ERROR, what + " failed (HTTP " + response.getStatus() + "). " + detail);
        return false;
    }

    /**
     * GET a list without letting a non-2xx response escape as a WebApplicationException - one of
     * those used to propagate out of MainController.submitForm() and break the whole page.
     */
    protected <T> List<T> readList(String path, GenericType<List<T>> type, String what) {
        Response response = target().path(path).request().get();
        try {
            if (response.getStatusInfo().getFamily() != Family.SUCCESSFUL) {
                addMessage(FacesMessage.SEVERITY_ERROR, what + " could not be loaded (HTTP " + response.getStatus() + ").");
                return new ArrayList<>();
            }
            return response.readEntity(type);
        }
        finally {
            response.close();
        }
    }

    public void loadCourses() { courses = readList(COURSE_RESOURCE_NAME, new GenericType<List<Course>>(){}, "Courses"); }
    public void loadProfessors() { professors = readList(PROFESSOR_RESOURCE_NAME, new GenericType<List<Professor>>(){}, "Professors"); }
    public void loadStudentClubs() { studentClubs = readList(STUDENT_CLUB_RESOURCE_NAME, new GenericType<List<StudentClub>>(){}, "Student clubs"); }
    public void loadCourseRegistrations() { courseRegistrations = readList(COURSE_REGISTRATION_RESOURCE_NAME, new GenericType<List<CourseRegistration>>(){}, "Course registrations"); }
    public void loadDegrees() { degrees = readList(PROFESSOR_RESOURCE_NAME + DEGREE_RESOURCE_PATH, new GenericType<List<String>>(){}, "Degrees"); }
    public void loadSemesters() { semesters = readList(COURSE_REGISTRATION_RESOURCE_NAME + SEMESTER_RESOURCE_PATH, new GenericType<List<String>>(){}, "Semesters"); }
    public void loadLetterGrades() { letterGrades = readList(COURSE_REGISTRATION_RESOURCE_NAME + LETTER_GRADE_RESOURCE_PATH, new GenericType<List<String>>(){}, "Letter grades"); }

    public void addCourse() { target().path(COURSE_RESOURCE_NAME).request().post(Entity.json(newCourse)); newCourse = new Course(); loadCourses(); }
    public void updateCourse(Course course) { target().path(COURSE_RESOURCE_NAME + "/" + course.getId()).request().put(Entity.json(course)); course.setEditable(false); loadCourses(); }
    public void deleteCourse(int id) { target().path(COURSE_RESOURCE_NAME + "/" + id).request().delete(); loadCourses(); }

    public void addProfessor() { target().path(PROFESSOR_RESOURCE_NAME).request().post(Entity.json(newProfessor)); newProfessor = new Professor(); loadProfessors(); }
    public void updateProfessor(Professor professor) { target().path(PROFESSOR_RESOURCE_NAME + "/" + professor.getId()).request().put(Entity.json(professor)); professor.setEditable(false); loadProfessors(); }
    public void deleteProfessor(int id) { target().path(PROFESSOR_RESOURCE_NAME + "/" + id).request().delete(); loadProfessors(); }

    public void addStudentClub() { target().path(STUDENT_CLUB_RESOURCE_NAME).request().post(Entity.json(newStudentClub)); newStudentClub = new StudentClub(false); loadStudentClubs(); }
    public void updateStudentClub(StudentClub club) { target().path(STUDENT_CLUB_RESOURCE_NAME + "/" + club.getId()).request().put(Entity.json(club)); club.setEditable(false); loadStudentClubs(); }
    public void deleteStudentClub(int id) { target().path(STUDENT_CLUB_RESOURCE_NAME + "/" + id).request().delete(); loadStudentClubs(); }

    public void addCourseRegistration() {
        Response response = target()
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + regStudentId + "/course/" + regCourseId)
            .request()
            .post(Entity.json(newRegistration));
        if (succeeded(response, "Course registration")) {
            addMessage(FacesMessage.SEVERITY_INFO, "Registered student " + regStudentId + " in course " + regCourseId + ".");
            newRegistration = freshRegistration();
        }
        loadCourseRegistrations();
    }

    /**
     * Saves an in-place edit of one row of the registrations table. Year/semester/grade go to the
     * base update endpoint; the professor has its own endpoint, so it needs a second call.
     */
    public void updateCourseRegistration(CourseRegistration registration) {
        String basePath = COURSE_REGISTRATION_RESOURCE_NAME
            + "/student/" + registration.getStudentId()
            + "/course/" + registration.getCourseId();

        Response response = target().path(basePath).request().put(Entity.json(registration));
        if (!succeeded(response, "Update registration")) {
            loadCourseRegistrations();
            return;
        }

        Integer professorId = registration.getProfessorId();
        if (professorId != null) {
            Response professorResponse = target()
                .path(basePath + "/professor/" + professorId)
                .request()
                .put(Entity.json(new CourseRegistration()));
            if (!succeeded(professorResponse, "Assign professor")) {
                loadCourseRegistrations();
                return;
            }
        }

        registration.setEditable(false);
        addMessage(FacesMessage.SEVERITY_INFO, "Saved registration for student "
            + registration.getStudentId() + " in course " + registration.getCourseId() + ".");
        loadCourseRegistrations();
    }

    public void deleteCourseRegistration(CourseRegistration registration) {
        Response response = target()
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + registration.getStudentId() + "/course/" + registration.getCourseId())
            .request()
            .delete();
        succeeded(response, "Delete registration");
        loadCourseRegistrations();
    }

    public void assignProfessor() {
        Response response = target()
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + regStudentId + "/course/" + regCourseId + "/professor/" + regProfessorId)
            .request()
            .put(Entity.json(new CourseRegistration()));
        if (succeeded(response, "Assign professor")) {
            addMessage(FacesMessage.SEVERITY_INFO, "Assigned professor " + regProfessorId + " to student " + regStudentId + " in course " + regCourseId + ".");
        }
        loadCourseRegistrations();
    }

    public void assignGrade() {
        Response response = target()
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/" + regStudentId + "/course/" + regCourseId + "/grade/" + selectedGrade)
            .request()
            .put(Entity.json(new CourseRegistration()));
        if (succeeded(response, "Assign grade")) {
            addMessage(FacesMessage.SEVERITY_INFO, "Assigned grade " + selectedGrade + " to student " + regStudentId + " in course " + regCourseId + ".");
        }
        loadCourseRegistrations();
    }

    public void addClubMembership() {
        if (!studentSelected()) {
            return;
        }
        Response response = target()
            .path(STUDENT_CLUB_RESOURCE_NAME + "/" + selectedClubId + "/student/" + selectedStudentId)
            .request()
            .post(Entity.json(""));
        succeeded(response, "Add club membership");
        loadStudentClubs();
    }

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
    public int getRegStudentId() { return regStudentId; }
    public void setRegStudentId(int regStudentId) { this.regStudentId = regStudentId; }
    public int getRegCourseId() { return regCourseId; }
    public void setRegCourseId(int regCourseId) { this.regCourseId = regCourseId; }
    public int getRegProfessorId() { return regProfessorId; }
    public void setRegProfessorId(int regProfessorId) { this.regProfessorId = regProfessorId; }
    public int getSelectedStudentId() { return selectedStudentId; }
    public void setSelectedStudentId(int selectedStudentId) { this.selectedStudentId = selectedStudentId; }
    public int getSelectedClubId() { return selectedClubId; }
    public void setSelectedClubId(int selectedClubId) { this.selectedClubId = selectedClubId; }
    public String getSelectedGrade() { return selectedGrade; }
    public void setSelectedGrade(String selectedGrade) { this.selectedGrade = selectedGrade; }

    public void setRegisteredClubs(List<StudentClub> registeredClubs) {
        this.registeredClubs = registeredClubs;
    }
    public void setUnRegisteredClubs(List<StudentClub> unRegisteredClubs) {
        this.unRegisteredClubs = unRegisteredClubs;
    }


    /**
     * The Club Membership screen opens with no student chosen (MainController resets the selection
     * to -1), so guard the calls rather than firing a request for a student id that cannot exist.
     */
    protected boolean studentSelected() {
        if (selectedStudentId > 0) {
            return true;
        }
        addMessage(FacesMessage.SEVERITY_WARN, "Select a student first.");
        return false;
    }

    public List<StudentClub> loadStudentMemberships() {
        if (!studentSelected()) {
            registeredClubs = new ArrayList<>();
            unRegisteredClubs = new ArrayList<>();
            return null;
        }
        registeredClubs = readList(STUDENT_CLUB_RESOURCE_NAME + "/student/" + selectedStudentId,
            new GenericType<List<StudentClub>>(){}, "Registered clubs");
        unRegisteredClubs = readList(STUDENT_CLUB_RESOURCE_NAME + "/student/" + selectedStudentId + "/unregistered",
            new GenericType<List<StudentClub>>(){}, "Unregistered clubs");
        return null;
    }

    public void deleteStudentFromClub(int clubId) {
        if (!studentSelected()) {
            return;
        }
        Response response = target()
            .path(STUDENT_CLUB_RESOURCE_NAME + "/" + clubId + "/student/" + selectedStudentId)
            .request()
            .delete();
        if (succeeded(response, "Withdraw from club")) {
            addMessage(FacesMessage.SEVERITY_INFO, "Withdrew student " + selectedStudentId + " from club " + clubId + ".");
        }
        loadStudentMemberships();
    }

    public void addStudentToClub(int clubId) {
        if (!studentSelected()) {
            return;
        }
        Response response = target()
            .path(STUDENT_CLUB_RESOURCE_NAME + "/" + clubId + "/student/" + selectedStudentId)
            .request()
            .post(Entity.json("{}"));
        if (succeeded(response, "Register in club")) {
            addMessage(FacesMessage.SEVERITY_INFO, "Registered student " + selectedStudentId + " in club " + clubId + ".");
        }
        loadStudentMemberships();
    }
}
