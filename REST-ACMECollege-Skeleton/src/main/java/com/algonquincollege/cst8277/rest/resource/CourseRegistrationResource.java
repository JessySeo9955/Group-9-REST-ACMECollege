/********************************************************************************************************
 * File: CourseRegistrationResource.java
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_GRADE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_PROFESSOR_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.GRADE_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.LETTER_GRADE_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.SEMESTER_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;

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

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getCourseRegistrations() {
        LOG.debug("retrieving course registrations");
        List<CourseRegistration> registrations;
        if (sc.isCallerInRole(ADMIN_ROLE)) {
            registrations = service.getAllCourseRegistrations();
        }
        else {
            registrations = service.getCourseRegistrationsForStudent(getCallerStudentId());
        }
        return Response.ok(registrations).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(COURSE_REGISTRATION_RESOURCE_PATH)
    public Response getCourseRegistrationByIds(
        @PathParam(STUDENT_ID_ELEMENT) int studentId,
        @PathParam(COURSE_ID_ELEMENT) int courseId) {
        ensureAdminOrOwner(studentId);
        CourseRegistration registration = service.getCourseRegistration(studentId, courseId);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_RESOURCE_PATH)
    public Response addCourseRegistration(
        @PathParam(STUDENT_ID_ELEMENT) int studentId,
        @PathParam(COURSE_ID_ELEMENT) int courseId,
        CourseRegistration newRegistration) {
        CourseRegistration registration = service.persistCourseRegistration(studentId, courseId, newRegistration);
        return Response.status(registration == null ? Status.BAD_REQUEST : Status.OK).entity(registration).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_RESOURCE_PATH)
    public Response updateCourseRegistration(
        @PathParam(STUDENT_ID_ELEMENT) int studentId,
        @PathParam(COURSE_ID_ELEMENT) int courseId,
        CourseRegistration registrationWithUpdates) {
        CourseRegistration registration = service.updateCourseRegistration(studentId, courseId, registrationWithUpdates);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_PROFESSOR_PATH)
    public Response assignProfessor(
        @PathParam(STUDENT_ID_ELEMENT) int studentId,
        @PathParam(COURSE_ID_ELEMENT) int courseId,
        @PathParam(PROFESSOR_ID_ELEMENT) int professorId) {
        CourseRegistration registration = service.assignProfessorToCourseRegistration(studentId, courseId, professorId);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_GRADE_PATH)
    public Response assignGrade(
        @PathParam(STUDENT_ID_ELEMENT) int studentId,
        @PathParam(COURSE_ID_ELEMENT) int courseId,
        @PathParam(GRADE_ELEMENT) String grade) {
        CourseRegistration registration = service.assignGradeToCourseRegistration(studentId, courseId, grade);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(COURSE_REGISTRATION_RESOURCE_PATH)
    public Response deleteCourseRegistration(
        @PathParam(STUDENT_ID_ELEMENT) int studentId,
        @PathParam(COURSE_ID_ELEMENT) int courseId) {
        CourseRegistration registration = service.deleteCourseRegistration(studentId, courseId);
        return Response.status(registration == null ? Status.NOT_FOUND : Status.OK).entity(registration).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(SEMESTER_RESOURCE_PATH)
    public Response getSemesters() {
        return Response.ok(service.getAllSemesters()).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(LETTER_GRADE_RESOURCE_PATH)
    public Response getLetterGrades() {
        return Response.ok(service.getAllLetterGrades()).build();
    }

    private void ensureAdminOrOwner(int studentId) {
        if (sc.isCallerInRole(ADMIN_ROLE)) {
            return;
        }
        if (sc.isCallerInRole(USER_ROLE) && getCallerStudentId() == studentId) {
            return;
        }
        throw new ForbiddenException("User trying to access a registration it does not own");
    }

    private int getCallerStudentId() {
        if (!(sc.getCallerPrincipal() instanceof WrappingCallerPrincipal wrappingPrincipal)) {
            throw new ForbiddenException("Caller is not linked to a SecurityUser");
        }
        SecurityUser securityUser = (SecurityUser) wrappingPrincipal.getWrapped();
        Student student = securityUser.getStudent();
        if (student == null) {
            throw new ForbiddenException("Caller is not linked to a student");
        }
        return student.getId();
    }
}