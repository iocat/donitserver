package xyz.donit.rest.auth;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.jetty.server.HttpTransport;
import xyz.donit.domain.client.User;
import xyz.donit.domain.exception.ResourceException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

/**
 * Created by felix on 1/13/17.
 */
@Provider
@GoogleTokenIDSecured
@Priority(Priorities.AUTHENTICATION)
public class GoogleSignInAuthFilter implements ContainerRequestFilter {
    private static class AuthenticationException extends Exception{}
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException{
        // Get the HTTP Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        // Check if the HTTP Authorization header is present and formatted correcly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();
        try{
            final GoogleIdToken.Payload payload = validateIdToken(token);
            /*long temp = 0;
            try{
                temp = this.getUserIdByName(db, payload);
            }catch(NotFoundException e){
                try{
                    temp = this.initNewUser(db, payload);
                }catch (ResourceException ce){
                    throw new InternalServerErrorException();
                }
            }catch (SQLException e){
                e.printStackTrace();
                throw new InternalServerErrorException();
            }
            final long userId = temp;
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return new Principal() {
                        @Override
                        public String getName() {
                            return new Long(userId).toString();
                        }
                    };
                }
                @Override
                public boolean isUserInRole(String role) {
                    // TODO: No roles for now
                    return true;
                }
                @Override
                public boolean isSecure() {
                    return true;
                }
                @Override
                public String getAuthenticationScheme() {
                    return DIGEST_AUTH;
                }
            });*/
        }catch (AuthenticationException e){
            throw new NotAuthorizedException("Invalid ID Token");
        }
    }

    @Inject
    BasicDataSource db;
    private static final String QUERY_GET_USERNAME = "SELECT user_id FROM users WHERE username = ?";

    // get the user id corresponding to the username (google sign-in id is the unique username
    private long getUserIdByName(DataSource db, GoogleIdToken.Payload payload) throws NotFoundException, SQLException{
        long id;
        try(Connection conn = db.getConnection();
            PreparedStatement stmt = conn.prepareStatement(QUERY_GET_USERNAME)){
            stmt.setString(1, payload.getSubject());
            try(ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new NotFoundException();
                }
                id = rs.getLong(1);
            }
        }
        return id;
    }

    // Create new user if the user id does not exist
    private long initNewUser(DataSource db, GoogleIdToken.Payload payload)throws ResourceException{
        User user = new User(payload.getSubject(),payload.getEmail());
        user.store(db);
        System.out.println("create user"+user.getId()+user.getEmail());
        return user.getId();
    }

    private static final String GOOGLE_CLIENT_ID = "585773028239-rosufgk1oe6f9afhjdhn0qg2i418j0a6.apps.googleusercontent.com";

    private GoogleIdToken.Payload validateIdToken(String tokenString) throws AuthenticationException{
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(UrlFetchTransport.getDefaultInstance(), new JacksonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID)).build();
        try{
            // verify the token
            GoogleIdToken idToken = verifier.verify(tokenString);
            if(idToken != null){
                GoogleIdToken.Payload payload = idToken.getPayload();
                return payload;
            }else{
                throw new AuthenticationException();
            }
        }catch (GeneralSecurityException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
