/********************************************************************************************************
 * File: StudentResource.java Course Materials CST 8277
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROGRAM_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
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

@Path(STUDENT_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentResource {
    @EJB protected ACMECollegeService service;
    @Inject protected SecurityContext sc;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getStudents() {
        return Response.ok(service.getAllStudents()).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getStudentById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Student student;
        if (sc.isCallerInRole(ADMIN_ROLE)) {
            student = service.getStudentById(id);
            return Response.status(student == null ? Status.NOT_FOUND : Status.OK).entity(student).build();
        }
        if (sc.isCallerInRole(USER_ROLE)) {
            WrappingCallerPrincipal principal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser user = (SecurityUser) principal.getWrapped();
            student = user.getStudent();
            if (student != null && student.getId() == id) {
                return Response.ok(student).build();
            }
            throw new ForbiddenException("User trying to access resource it does not own");
        }
        return Response.status(Status.BAD_REQUEST).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addStudent(Student newStudent) {
        Student persisted = service.persistStudent(newStudent);
        service.buildUserForNewStudent(persisted);
        return Response.status(Status.CREATED).entity(persisted).build();
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateStudentById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Student studentWithUpdates) {
        Student updated = service.updateStudentById(id, studentWithUpdates);
        return Response.status(updated == null ? Status.NOT_FOUND : Status.OK).entity(updated).build();
    }

    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteStudentById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Student deleted = service.deleteStudentById(id);
        return Response.status(deleted == null ? Status.NOT_FOUND : Status.OK).entity(deleted).build();
    }

    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(PROGRAM_RESOURCE_PATH)
    public Response getPrograms() {
        List<String> programs = service.getAllPrograms();
        return Response.ok(programs).build();
    }
}
