package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.CLUB_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.CLUB_MEMBERSHIP_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_CLUB_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_CLUB_MEMBERSHIP_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_UNREGISTERED_CLUB_MEMBERSHIP_PATH;
import java.util.List;


import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path(STUDENT_CLUB_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentClubResource {
    @EJB protected ACMECollegeService service;

    @GET @PermitAll
    public Response getStudentClubs() { return Response.ok(service.getAllStudentClubs()).build(); }

    @GET @PermitAll @Path(RESOURCE_PATH_ID_PATH)
    public Response getStudentClubById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        StudentClub club = service.getStudentClubById(id);
        return Response.status(club == null ? Status.NOT_FOUND : Status.OK).entity(club).build();
    }
    
    @GET 
    // @RolesAllowed({ADMIN_ROLE}) 
    @Path(STUDENT_CLUB_MEMBERSHIP_PATH)
    public Response getStudentClubsByStudent(
            @PathParam(STUDENT_ID_ELEMENT) int studentId) {
        List<StudentClub> clubs =
                service.getStudentClubsByStudent(studentId);
        return Response.ok(clubs).build();
    }

    @GET
    //@RolesAllowed({ADMIN_ROLE})
    @Path(STUDENT_UNREGISTERED_CLUB_MEMBERSHIP_PATH)
    public Response getUnregisteredStudentClubsByStudent(
            @PathParam(STUDENT_ID_ELEMENT) int studentId) {

        List<StudentClub> clubs =
                service.getUnregisteredStudentClubs(studentId);

        return Response.ok(clubs).build();
    }
    
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addStudentClub(StudentClub club) {
        return Response.status(Status.CREATED).entity(service.persistStudentClub(club)).build();
    }

    @PUT 
    @RolesAllowed({ADMIN_ROLE}) 
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateStudentClub(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, StudentClub updates) {
        StudentClub club = service.updateStudentClubById(id, updates);
        return Response.status(club == null ? Status.NOT_FOUND : Status.OK).entity(club).build();
    }

    
    @DELETE 
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteStudentClub(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        StudentClub club = service.deleteStudentClubById(id);
        return Response.status(club == null ? Status.NOT_FOUND : Status.OK).entity(club).build();
    }

    @POST 
    @RolesAllowed({ADMIN_ROLE}) 
    @Path(CLUB_MEMBERSHIP_PATH)
    public Response addClubMembership(
    		@PathParam(CLUB_ID_ELEMENT) int clubId, 
    		@PathParam(STUDENT_ID_ELEMENT) int studentId) {
    	StudentClub club = service.addStudentToClub(clubId, studentId);
        return Response.status(club == null ? Status.NOT_FOUND : Status.OK).entity(club).build();
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(CLUB_MEMBERSHIP_PATH)
    public Response deleteClubMembership(
            @PathParam(CLUB_ID_ELEMENT) int clubId,
            @PathParam(STUDENT_ID_ELEMENT) int studentId) {

        StudentClub club = service.removeStudentFromClub(clubId, studentId);

        return Response.status(club == null ? Status.NOT_FOUND : Status.OK)
                       .entity(club)
                       .build();
    }
}
