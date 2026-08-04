package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEGREE_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Professor;

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

@Path(PROFESSOR_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({ADMIN_ROLE})
public class ProfessorResource {
    @EJB protected ACMECollegeService service;

    @GET public Response getProfessors() { return Response.ok(service.getAllProfessors()).build(); }

    @GET @Path(RESOURCE_PATH_ID_PATH)
    public Response getProfessorById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Professor professor = service.getProfessorById(id);
        return Response.status(professor == null ? Status.NOT_FOUND : Status.OK).entity(professor).build();
    }

    @POST public Response addProfessor(Professor professor) {
        return Response.status(Status.CREATED).entity(service.persistProfessor(professor)).build();
    }

    @PUT @Path(RESOURCE_PATH_ID_PATH)
    public Response updateProfessor(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Professor updates) {
        Professor professor = service.updateProfessorById(id, updates);
        return Response.status(professor == null ? Status.NOT_FOUND : Status.OK).entity(professor).build();
    }

    @DELETE @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteProfessor(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Professor professor = service.deleteProfessorById(id);
        return Response.status(professor == null ? Status.NOT_FOUND : Status.OK).entity(professor).build();
    }

    @GET @Path(DEGREE_RESOURCE_PATH)
    public Response getDegrees() { return Response.ok(service.getAllDegrees()).build(); }
}
