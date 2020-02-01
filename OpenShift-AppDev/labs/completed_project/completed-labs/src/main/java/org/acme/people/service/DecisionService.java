package org.acme.people.service;
 
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.Consumes;
 
@RegisterRestClient
@Produces("application/json")
@Consumes("application/json")
public interface DecisionService {
 
   @POST
   @Path("/services/rest/server/containers/{containerId}/dmn")
   Response checkDriverSuspended(
       @HeaderParam("Authorization") String authorization,
       @javax.ws.rs.PathParam("containerId") String containerId,
       String requestBody) ;
}
