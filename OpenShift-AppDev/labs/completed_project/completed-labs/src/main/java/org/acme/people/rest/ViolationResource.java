package org.acme.people.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.people.service.DecisionService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

@Path("/violation")
public class ViolationResource {
    
   Logger logger = LoggerFactory.getLogger(ViolationResource.class);

   @Inject
   @RestClient
   DecisionService decisionService;
 
      @ConfigProperty(name = "basic.authHeader")
   String authHeader;
 
   @ConfigProperty(name = "violation.containerId")
   String violationContainerId;
 
   @GET
   @Path("/hello")
   @Produces(MediaType.TEXT_PLAIN)
   public String hello() {
       return "hello RH PDT";
   }

   @GET
   @Path("/check")
   @Produces(MediaType.APPLICATION_JSON)
public Response check(
           @QueryParam("age") int age,
           @QueryParam("points") int points,
           @QueryParam("actualSpeed") int actualSpeed) { 
       final JsonObject dmnRequest = getDmnEvalBody(age,points,actualSpeed);
 
       logger.info("Getting violation info with authHeaders {}, container {}, and body {}", authHeader,
               violationContainerId, dmnRequest.toString());
 
       final Response driverSuspended = decisionService.checkDriverSuspended(authHeader, violationContainerId,
               dmnRequest.toString());
       logger.info("Driver suspended ?  {}", driverSuspended);
 
       return driverSuspended;
   }
   
   private JsonObject getDmnEvalBody(final int age, final int points, final int actualSpeed) {
 
       final JsonObject modelHeader = new JsonObject();
       modelHeader.put("model-namespace",
       "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF");
       modelHeader.put("model-name", "Traffic Violation");
       modelHeader.put("decision-name", "Should the driver be suspended?");
 
       final JsonObject dmnContext = new JsonObject();
 
       final JsonObject driverInfo = new JsonObject();
       driverInfo.put("Name", "Bob");
       driverInfo.put("Age", age);
       driverInfo.put("Points", points);
 
       dmnContext.put("Driver", driverInfo);
 
       final JsonObject violationInfo = new JsonObject();
       violationInfo.put("Code", "speed-stop");
       violationInfo.put("Speed Limit", 30);
       violationInfo.put("Actual Speed", actualSpeed);
       violationInfo.put("Type", "speed");
 
       dmnContext.put("Violation", violationInfo);
 
       modelHeader.put("dmn-context", dmnContext);
 
       return modelHeader;
   }
}