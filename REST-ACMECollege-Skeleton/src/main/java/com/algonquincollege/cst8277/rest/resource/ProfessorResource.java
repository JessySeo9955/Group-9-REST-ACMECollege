package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.PROGRAM_RESOURCE_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityUser;


@Path(PROFESSOR_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfessorResource {
	
	private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;
    
    @Inject
    protected SecurityContext sc;

    /**
     * Get all professors.
     * Only ADMIN can retrieve the complete list.
     */
    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getProfessors() {
        LOG.debug("retrieving all professors ...");

        List<Professor> professors = service.getAllProfessors();

        return Response.ok(professors).build();
    }

    /**
     * Get a professor by id.
     * ADMIN and USER can access.
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getProfessorById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {

        LOG.debug("retrieving professor {}", id);

        Professor professor = service.getProfessorById(id);

        return Response.status(professor == null ? Status.NOT_FOUND : Status.OK)
                .entity(professor)
                .build();
    }

    /**
     * Create a professor.
     */
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addProfessor(Professor newProfessor) {

        Professor professor = service.persistProfessor(newProfessor);

        return Response.ok(professor).build();
    }

    /**
     * Update a professor.
     */
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateProfessorById(
            @PathParam(RESOURCE_PATH_ID_ELEMENT) int id,
            Professor professorWithUpdates) {

        Professor updatedProfessor =
                service.updateProfessorById(id, professorWithUpdates);

        return Response.ok(updatedProfessor).build();
    }

    /**
     * Delete a professor.
     */
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteProfessorById(
            @PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {

        Professor deletedProfessor =
                service.deleteProfessorById(id);

        return Response.ok(deletedProfessor).build();
    }

}
