/********************************************************************************************************
 * File:  CourseResource.java Course Materials CST 8277
 *
 * @author Simon
 *
 * Course vertical slice - REST layer.
 *
 * Security rules for this resource:
 *   - Reading courses  : any authenticated user (ADMIN_ROLE or USER_ROLE)
 *   - Creating/updating/deleting a course : ADMIN_ROLE only
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Course;

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @Inject
    protected SecurityContext sc;

    /**
     * GET /course - list every course.
     * Any authenticated user may browse the course catalogue.
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getCourses() {
        LOG.debug("retrieving all courses ...");
        List<Course> courses = service.getAllCourses();
        return Response.ok(courses).build();
    }

    /**
     * GET /course/{id} - read one course.
     * Any authenticated user may read a course; unlike Student there is nothing
     * user-specific about a course, so no ownership check is required.
     */
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getCourseById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific course {}", id);
        Course course = service.getCourseById(id);
        return Response.status(course == null ? Status.NOT_FOUND : Status.OK).entity(course).build();
    }

    /**
     * POST /course - create a course. ADMIN_ROLE only.
     */
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourse(Course newCourse) {
        LOG.debug("adding a new course {}", newCourse);
        Course newCourseWithIdTimestamps = service.persistCourse(newCourse);
        return Response.ok(newCourseWithIdTimestamps).build();
    }

    /**
     * PUT /course/{id} - update a course. ADMIN_ROLE only.
     */
    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateCourseById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Course courseWithUpdates) {
        LOG.debug("updating specific course {}", id);
        Course updatedCourse = service.updateCourseById(id, courseWithUpdates);
        return Response.status(updatedCourse == null ? Status.NOT_FOUND : Status.OK).entity(updatedCourse).build();
    }

    /**
     * DELETE /course/{id} - delete a course. ADMIN_ROLE only.
     */
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteCourseById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("deleting specific course {}", id);
        Course deletedCourse = service.deleteCourseById(id);
        return Response.status(deletedCourse == null ? Status.NOT_FOUND : Status.OK).entity(deletedCourse).build();
    }

}
