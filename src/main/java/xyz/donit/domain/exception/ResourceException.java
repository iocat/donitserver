package xyz.donit.domain.exception;

import javax.ws.rs.core.Response;

/**
 * Created by felix on 1/12/17.
 */
public class ResourceException extends Exception {
    private ResourceErrCode code;
    public ResourceException(ResourceErrCode code){
        this.code = code;
    }
    public ResourceException(ResourceErrCode code, String msg){
        super(msg);
        this.code = code;
    }
    public int getHTTPCode(){
        switch(code){
            case DOES_NOT_EXIST:
                return Response.Status.NOT_FOUND.getStatusCode();
            case CANNOT_STORE:
                return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            default:
                return 0;
        }
    }
}
