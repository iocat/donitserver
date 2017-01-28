package xyz.donit.rest.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.apache.commons.dbcp2.BasicDataSource;
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
    private static class AuthenticationException extends Exception {
    }

    // an id which cannot be assigned to any user
    // If the corresponding user cannot be found this id is the principle
    private static long EMPTY_USER_ID = 0L;
    private User initUser(GoogleIdToken.Payload payload) throws ResourceException{
        User user = new User();
        user.setUsername(payload.getSubject());
        user.setEmail(payload.getEmail());
        user.store(db);
        return user;
    }
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Get the HTTP Authorization header from the request
        String authorizationHeader =
                requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        // Check if the HTTP Authorization header is present and formatted correcly
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Authorization header must be provided");
        }
        // Extract the token from the HTTP Authorization header
        String token = authorizationHeader.substring("Bearer".length()).trim();

        try {
            final GoogleIdToken.Payload payload = validateIdToken(token);
            long temp = 0L;
            try {
                temp = this.getUserIdByName(db, payload.getSubject());
            } catch (NotFoundException e) {
                // Initialize user
                try{
                    temp = initUser(payload).getId();
                }catch (ResourceException resEx){
                    resEx.printStackTrace();
                    throw new InternalServerErrorException("could not initialize user");
                }
            } catch (SQLException e) {
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
            });
        } catch (AuthenticationException e) {
            throw new NotAuthorizedException("Invalid ID Token");
        }
    }

    @Inject
    DataSource db;
    private static final String QUERY_GET_USERNAME = "SELECT user_id FROM users WHERE username = ?";

    // get the user id corresponding to the username (google sign-in id is the unique username
    private long getUserIdByName(DataSource db, String googleId) throws NotFoundException, SQLException {
        long id;
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(QUERY_GET_USERNAME)) {
            stmt.setString(1, googleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new NotFoundException();
                }
                id = rs.getLong(1);
            }
        }
        return id;
    }

    private static final String GOOGLE_CLIENT_ID = "585773028239-rosufgk1oe6f9afhjdhn0qg2i418j0a6.apps.googleusercontent.com";

    private static GoogleIdToken.Payload validateIdToken(String tokenString) throws AuthenticationException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID)).build();
        try {
            // verify the token
            GoogleIdToken idToken = verifier.verify(tokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                return payload;
            } else {
                throw new AuthenticationException();
            }
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new AuthenticationException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new AuthenticationException();
        }
    }
}
