package xyz.donit.rest.auth;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

/**
 * Created by felix on 1/13/17.
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
@UserIdAuthorized
public class UserIdAuthorization implements ContainerRequestFilter{
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MultivaluedMap<String, String> params = (MultivaluedHashMap<String, String>) requestContext.getUriInfo().getPathParameters();
        String userId = params.get("userid").get(0);
        if (userId.equals(requestContext.getSecurityContext().getUserPrincipal())){
            return;
        }
        throw new ForbiddenException("Cannot request another user's data");
    }
}
