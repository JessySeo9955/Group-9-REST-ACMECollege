/********************************************************************************************************
 * File:  MainController.java
 * Course Materials CST 8277
 * 
 * @author Teddy Yap
 *
 */
package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.algonquincollege.cst8277.utility.MyConstants;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("mainController")
@SessionScoped
public class MainController implements Serializable, MyConstants {
    private static final long serialVersionUID = 1L;

    public static final String NONE = "None";
    public static final String STUDENT_MANAGEMENT = "Student Management";
    public static final String COURSE_MANAGEMENT = "Course Management";
    public static final String PROFESSOR_MANAGEMENT = "Professor Management";
    public static final String STUDENT_CLUB_MANAGEMENT = "Student Club Management";
    public static final String COURSE_REGISTRATION = "Course Registration";
    public static final String ASSIGN_PROFESSOR = "Assign Professor";
    public static final String ASSIGN_GRADE = "Assign Grade";
    public static final String CLUB_MEMBERSHIP_REGISTRATION = "Club Membership Registration";

    @Inject
    protected StudentController studentController;

    @Inject
    protected CollegeController collegeController;

    protected String optionChosen = "None";
    protected List<String> options = Stream.of(NONE, STUDENT_MANAGEMENT, COURSE_MANAGEMENT, PROFESSOR_MANAGEMENT,
        STUDENT_CLUB_MANAGEMENT, COURSE_REGISTRATION, ASSIGN_PROFESSOR, ASSIGN_GRADE, CLUB_MEMBERSHIP_REGISTRATION)
        .collect(Collectors.toList());

    public MainController() {
        super();
    }

    public String getOptionChosen() {
        return optionChosen;
    }

    public void setOptionChosen(String option) {
        optionChosen = option;
    }

    public List<String> getOptions() {
        return options;
    }

    public String submitForm() {
        loadData();
        return null;
    }

    public void loadData() {
        switch (optionChosen) {
            case STUDENT_MANAGEMENT:
                studentController.loadStudents();
                break;
            case COURSE_MANAGEMENT:
                collegeController.loadCourses();
                break;
            case PROFESSOR_MANAGEMENT:
                collegeController.loadProfessors();
                collegeController.loadDegrees();
                break;
            case STUDENT_CLUB_MANAGEMENT:
                collegeController.loadStudentClubs();
                break;
            // All three course-registration screens pick students/courses/professors/semesters/
            // grades from drop-downs - both in the entry form and in the editable table - so every
            // one of those lists has to be loaded alongside the registrations.
            case COURSE_REGISTRATION:
            case ASSIGN_PROFESSOR:
            case ASSIGN_GRADE:
                loadCourseRegistrationLookups();
                break;
            case CLUB_MEMBERSHIP_REGISTRATION:
            	collegeController.setRegisteredClubs(new ArrayList<>());
            	collegeController.setUnRegisteredClubs(new ArrayList<>());
            	collegeController.setSelectedStudentId(-1);
            	collegeController.loadStudentClubs();
            	studentController.loadStudents();
                break;
            default:
                break;
        }
    }

    private void loadCourseRegistrationLookups() {
        studentController.loadStudents();
        collegeController.loadCourses();
        collegeController.loadProfessors();
        collegeController.loadSemesters();
        collegeController.loadLetterGrades();
        collegeController.loadCourseRegistrations();
    }
}