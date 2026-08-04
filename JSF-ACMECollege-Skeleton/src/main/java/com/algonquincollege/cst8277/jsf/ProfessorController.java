package com.algonquincollege.cst8277.jsf;

import java.io.Serializable;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.rest.resource.MyObjectMapperProvider;
import com.algonquincollege.cst8277.utility.MyConstants;

@Named("professorController")
@SessionScoped
public class ProfessorController implements Serializable, MyConstants {

    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext facesContext;

    @Inject
    protected ExternalContext externalContext;

    @Inject
    protected ServletContext sc;

    @Inject
    protected LoginBean loginBean;

    protected List<Professor> listOfProfessors;

    protected boolean adding;

    static URI uri;
    static HttpAuthenticationFeature auth;

    protected Client client;
    protected WebTarget webTarget;

    public ProfessorController() {
        super();
    }

    @PostConstruct
    public void initialize() {

        uri = UriBuilder
                .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
                .scheme(HTTP_SCHEMA)
                .host(HOST)
                .port(PORT)
                .build();

        auth = HttpAuthenticationFeature.basic(
                loginBean.getUsername(),
                loginBean.getPassword());

        client = ClientBuilder.newClient(
                new ClientConfig()
                        .register(MyObjectMapperProvider.class)
                        .register(new LoggingFeature()));

        webTarget = client.target(uri);

        loadProfessors();
    }

    public List<Professor> getProfessors() {
        return listOfProfessors;
    }

    public void setProfessors(List<Professor> listOfProfessors) {
        this.listOfProfessors = listOfProfessors;
    }

    public void loadProfessors() {

        Response response = webTarget
                .register(auth)
                .path(PROFESSOR_RESOURCE_NAME)
                .request()
                .get();

        listOfProfessors =
                response.readEntity(new GenericType<List<Professor>>() {});
    }

    public boolean isAdding() {
        return adding;
    }

    public void setAdding(boolean adding) {
        this.adding = adding;
    }

    public void toggleAdding() {
        adding = !adding;
    }

    public String editProfessor(Professor professor) {

        professor.setEditable(true);

        return null;
    }

    public String updateProfessor(Professor professor) {

        Response response = webTarget
                .register(auth)
                .path(PROFESSOR_RESOURCE_NAME + "/" + professor.getId())
                .request()
                .put(Entity.json(professor));

        Professor updatedProfessor =
                response.readEntity(Professor.class);

        updatedProfessor.setEditable(false);

        int idx = listOfProfessors.indexOf(professor);

        listOfProfessors.remove(idx);
        listOfProfessors.add(idx, updatedProfessor);

        return null;
    }

    public String cancelUpdate(Professor professor) {

        professor.setEditable(false);

        return null;
    }

    public String deleteProfessor(int professorId) {

        Response response = webTarget
                .register(auth)
                .path(PROFESSOR_RESOURCE_NAME + "/" + professorId)
                .request()
                .get();

        Professor professor =
                response.readEntity(Professor.class);

        if (professor != null) {

            response = webTarget
                    .register(auth)
                    .path(PROFESSOR_RESOURCE_NAME + "/" + professor.getId())
                    .request()
                    .delete();

            Professor deletedProfessor =
                    response.readEntity(Professor.class);

            listOfProfessors.remove(deletedProfessor);
        }

        return null;
    }

    public String addNewProfessor(Professor professor) {

        Response response = webTarget
                .register(auth)
                .path(PROFESSOR_RESOURCE_NAME)
                .request()
                .post(Entity.json(professor));

        Professor newProfessor =
                response.readEntity(Professor.class);

        listOfProfessors.add(newProfessor);

        return null;
    }

    public String refreshProfessorForm() {

        Iterator<FacesMessage> iterator =
                facesContext.getMessages();

        while (iterator.hasNext()) {
            iterator.remove();
        }

        loadProfessors();

        return MAIN_PAGE_REDIRECT;
    }
}