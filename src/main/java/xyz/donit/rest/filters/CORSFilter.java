package xyz.donit.rest.filters;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created by felix on 1/27/17.
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {
    @Override
    public void filter(final ContainerRequestContext res,
                       final ContainerResponseContext cres) throws IOException {
        cres.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        cres.getHeaders().putSingle("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        cres.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        cres.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        cres.getHeaders().putSingle("Access-Control-Max-Age", "1209600");
    }
}
