package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.Course;

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

@Path(COURSE_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed({ADMIN_ROLE})
public class CourseResource {
    @EJB protected ACMECollegeService service;

    @GET public Response getCourses() { return Response.ok(service.getAllCourses()).build(); }

    @GET @Path(RESOURCE_PATH_ID_PATH)
    public Response getCourseById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Course course = service.getCourseById(id);
        return Response.status(course == null ? Status.NOT_FOUND : Status.OK).entity(course).build();
    }

    @POST public Response addCourse(Course course) {
        return Response.status(Status.CREATED).entity(service.persistCourse(course)).build();
    }

    @PUT @Path(RESOURCE_PATH_ID_PATH)
    public Response updateCourse(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Course updates) {
        Course course = service.updateCourseById(id, updates);
        return Response.status(course == null ? Status.NOT_FOUND : Status.OK).entity(course).build();
    }

    @DELETE @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteCourse(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Course course = service.deleteCourseById(id);
        return Response.status(course == null ? Status.NOT_FOUND : Status.OK).entity(course).build();
    }
}
