package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.ASSIGN_GRADE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.ASSIGN_PROFESSOR_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_BY_IDS_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.GRADE_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.LETTER_GRADE_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.SEMESTER_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.SecurityUser;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path(COURSE_REGISTRATION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseRegistrationResource {
    @EJB protected ACMECollegeService service;
    @Inject protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getCourseRegistrations() {
        if (sc.isCallerInRole(ADMIN_ROLE)) {
            return Response.ok(service.getAllCourseRegistrations()).build();
        }
        SecurityUser user = currentUser();
        if (user.getStudent() == null) {
            return Response.ok(List.of()).build();
        }
        return Response.ok(service.getCourseRegistrationsForStudent(user.getStudent().getId())).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(COURSE_REGISTRATION_BY_IDS_PATH)
    public Response getCourseRegistration(@PathParam(STUDENT_ID_ELEMENT) int studentId, @PathParam(COURSE_ID_ELEMENT) int courseId) {
        verifyOwnStudentOrAdmin(studentId);
        CourseRegistration registration = service.getCourseRegistration(studentId, courseId);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_BY_IDS_PATH)
    public Response createCourseRegistration(@PathParam(STUDENT_ID_ELEMENT) int studentId, @PathParam(COURSE_ID_ELEMENT) int courseId, CourseRegistration body) {
        CourseRegistration registration = service.createCourseRegistration(studentId, courseId, body);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.CREATED).entity(registration).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_BY_IDS_PATH)
    public Response updateCourseRegistration(@PathParam(STUDENT_ID_ELEMENT) int studentId, @PathParam(COURSE_ID_ELEMENT) int courseId, CourseRegistration body) {
        CourseRegistration registration = service.updateCourseRegistration(studentId, courseId, body);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(ASSIGN_PROFESSOR_PATH)
    public Response assignProfessor(@PathParam(STUDENT_ID_ELEMENT) int studentId, @PathParam(COURSE_ID_ELEMENT) int courseId, @PathParam(PROFESSOR_ID_ELEMENT) int professorId) {
        CourseRegistration registration = service.assignProfessor(studentId, courseId, professorId);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(ASSIGN_GRADE_PATH)
    public Response assignGrade(@PathParam(STUDENT_ID_ELEMENT) int studentId, @PathParam(COURSE_ID_ELEMENT) int courseId, @PathParam(GRADE_ELEMENT) String grade) {
        CourseRegistration registration = service.assignGrade(studentId, courseId, grade);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_BY_IDS_PATH)
    public Response deleteCourseRegistration(@PathParam(STUDENT_ID_ELEMENT) int studentId, @PathParam(COURSE_ID_ELEMENT) int courseId) {
        CourseRegistration registration = service.deleteCourseRegistration(studentId, courseId);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @GET @RolesAllowed({ADMIN_ROLE}) @Path(SEMESTER_RESOURCE_PATH)
    public Response getSemesters() { return Response.ok(service.getAllSemesters()).build(); }

    @GET @RolesAllowed({ADMIN_ROLE}) @Path(LETTER_GRADE_RESOURCE_PATH)
    public Response getLetterGrades() { return Response.ok(service.getAllLetterGrades()).build(); }

    private SecurityUser currentUser() {
        WrappingCallerPrincipal principal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
        return (SecurityUser) principal.getWrapped();
    }

    private void verifyOwnStudentOrAdmin(int studentId) {
        if (sc.isCallerInRole(ADMIN_ROLE)) { return; }
        SecurityUser user = currentUser();
        if (user.getStudent() == null || user.getStudent().getId() != studentId) {
            throw new ForbiddenException("User trying to access course registration it does not own");
        }
    }
}
