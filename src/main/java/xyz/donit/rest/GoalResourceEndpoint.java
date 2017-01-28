package xyz.donit.rest;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import xyz.donit.domain.goal.Goal;
import xyz.donit.domain.exception.ResourceException;
import xyz.donit.rest.auth.GoogleTokenIDSecured;

import java.util.ArrayList;

/**
 * Created by felix on 1/12/17.
 */
@Path("/users/{userid}/goals")
public class GoalResourceEndpoint {
    @Inject
    DataSource db;

    private static Response constructResponse(ResourceException e){
        return Response.status(e.getHTTPCode()).entity(e.getLocalizedMessage()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @GoogleTokenIDSecured
    public Response getManyWithPaginator(@PathParam("userid") final long userId){
        ArrayList<Goal> goals = null;
        try{
            goals = Goal.getGoals(db, userId);
        }catch (ResourceException e){
            return constructResponse(e);
        }
        return Response.status(200).entity(goals).build();
    }

    @GET
    @Path("/paging")
    @GoogleTokenIDSecured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getManyWithPaginator(@PathParam("userid") final long userId,
                            @QueryParam("limit") int limit,
                            @QueryParam("offset") int offset){
        if(limit == 0){
            limit = Integer.MAX_VALUE;
        }
        ArrayList<Goal> goals = null;
        try{
            goals = Goal.getGoals(db, userId, limit, offset);
        }catch (ResourceException e){
            return constructResponse(e);
        }
        return Response.status(200).entity(goals).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @GoogleTokenIDSecured
    public Response createOne(Goal goal, @PathParam("userid") final long userId){
        try{
            goal.store(db, userId);
            return Response.status(200).entity(goal).build();
        }catch (ResourceException e){
            e.printStackTrace();
            return constructResponse(e);
        }
    }

    @GET
    @Path("/{goalid}")
    @GoogleTokenIDSecured
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOne(@PathParam("userid") final long userId,
                            @PathParam("goalid") final long goalId){
        Goal goal = new Goal(goalId);
        try {
            goal.retrieve(db, userId);
        } catch (ResourceException e) {
            return constructResponse(e);
        }
        return Response.status(200).entity(goal).build();
    }

}
