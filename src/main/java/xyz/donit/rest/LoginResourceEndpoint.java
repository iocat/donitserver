package xyz.donit.rest;

import org.apache.commons.dbcp2.BasicDataSource;
import xyz.donit.domain.client.User;
import xyz.donit.domain.exception.ResourceException;
import xyz.donit.domain.goal.Goal;
import xyz.donit.rest.auth.GoogleSignInAuthFilter;
import xyz.donit.rest.auth.GoogleTokenIDSecured;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;

/**
 * Created by felix on 1/26/17.
 */
@Path("/login")
public class LoginResourceEndpoint {
    @Inject
    DataSource db;

    static final class Result{
        private User currentUser;

        public User getCurrentUser() {
            return currentUser;
        }

        public void setCurrentUser(User currentUser) {
            this.currentUser = currentUser;
        }

        public ArrayList<Goal> getGoals() {
            return goals;
        }

        public void setGoals(ArrayList<Goal> goals) {
            this.goals = goals;
        }

        private ArrayList<Goal> goals;

        public Result(){
            this.currentUser = new User();
            this.goals = new ArrayList<>();
        }
        public Result(User user, ArrayList<Goal> goals) {
            this.currentUser = user;
            this.goals = goals;
        }
    }

    /*
        Returns all user necessary data for app start-up
        Initializes new user if the user does not exist
     */
    @GET
    @GoogleTokenIDSecured
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginIn(@Context SecurityContext sec) {
        long userId = Long.parseLong(sec.getUserPrincipal().getName());
        User user = new User(userId);
        ArrayList<Goal> goals = new ArrayList<>();
        try {
            user.retrieve(db);
            goals = Goal.getGoals(db, user.getId());
        }catch (ResourceException e){
            return Response.status(e.getHTTPCode()).build();
        }
        Result res = new Result(user, goals);
        return Response.status(200).entity(res).build();
    }
}
