package xyz.donit.rest;

import xyz.donit.domain.client.User;
import xyz.donit.domain.exception.ResourceException;
import xyz.donit.rest.auth.GoogleTokenIDSecured;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users")
public class UserResourceEndpoint {
    @Inject
    DataSource db;

    @GET
    @Path("{userid}")
    @GoogleTokenIDSecured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJSON(@PathParam("userid") long userId){
        User user = new User(userId);
        try{
            user.retrieve(db);
            return Response.status(200).entity(user).build();
        }catch (ResourceException e){
            return Response.status(e.getHTTPCode()).build();
        }
    }

    @POST
    @GoogleTokenIDSecured
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(User user){
        try{
            user.store(db);
        }catch (ResourceException e){
            return Response.status(e.getHTTPCode()).build();
        }
        return Response.status(200).entity(
                user
        ).build();
    }
}
