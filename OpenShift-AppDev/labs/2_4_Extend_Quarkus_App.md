## Extend Quarkus app to delegate to decision service with the Microprofile REST client

1. Create a ViolationResource class that looks like this

```java
public class ViolationResource {
 
   @GET
   @Path("/hello")
   @Produces(MediaType.TEXT_PLAIN)
   public String hello() {
       return "hello RH PDT";
   }
}

```
2. Add the check() method to the ViolationResource

```java
 @GET
   @Path("/check")
   @Produces(MediaType.APPLICATION_JSON)
   public String check() {
       // … something here
   }

```

3. Let’s add the Microprofile HTTP client service (based on https://quarkus.io/guides/rest-client and https://download.eclipse.org/microprofile/microprofile-rest-client-1.2.1/microprofile-rest-client-1.2.1.html)

Because we want to lean on the MiroProfile REST client, we will add a very simple service interface and annotate it appropriately (inside of org.acme.service package). Create a new Service: 

```java
package org.acme.service;
 
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

```

In short, this service interface will : 
Call the URL indicated by the @Path annotation
* It will consume and produce JSON
* It will take an “authorization” parameter and put it in the header of the request to the kieserver
* It will also take a containerId parameter which will be used to reach the right Path
* Finally, the request body would be POST-ed to the kieserver

4. Update the property files with the base URL In order to make this service work, I need to add the following in applicaiton.properties (note that for this case I’m pointing it to the http route to the kieserver, not the https):

```properties
org.acme.service.DecisionService/mp-rest/url=http://rhpam-trial-kieserver-http-pam-dm1.apps.ocp-pam-cluster-1.clusters.thinkjitsu.me/
org.acme.service.DecisionService/mp-rest/scope=javax.inject.Singleton

```

5. Inject the Decision Service in our resource and lean on it to call the kieserver (only showing the changes here). Added a couple of static values for the auth header and the container ID (to be dealt with a bit further down)

```java
   @Inject
   @RestClient
   DecisionService decisionService;
 
   String authHeader = "Basic YWRtaW5Vc2VyOlJlZEhhdA==";
   String violationContainerId = "traffic-violation_1.0.0-SNAPSHOT";
  
   @GET
   @Path("/check")
   @Produces(MediaType.APPLICATION_JSON)
   public Response check() {
       JsonObject dmnRequest = getDmnEvalBody();
 
       logger.info("Getting violation info with authHeaders {}, container {}, and body {}", authHeader,
               "traffic-violation_1.0.0-SNAPSHOT", dmnRequest.toString());
 
       final Response driverSuspended = decisionService.checkDriverSuspended(authHeader, violationContainerId,
               dmnRequest.toString());
       logger.info("Driver suspended ?  {}", driverSuspended);
 
       return driverSuspended;
   }

```

6. Finally, add a utility method that builds our JSON body that kieserver needs to evaluate the DMN: 

```java
private JsonObject getDmnEvalBody() {
 
       final JsonObject modelHeader = new JsonObject();
       modelHeader.put("model-namespace",
       "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF");
       modelHeader.put("model-name", "Traffic Violation");
       modelHeader.put("decision-name", "Should the driver be suspended?");
 
       final JsonObject dmnContext = new JsonObject();
 
       final JsonObject driverInfo = new JsonObject();
       driverInfo.put("Name", "Bob");
       driverInfo.put("Age", 23);
       driverInfo.put("Points", 2);
 
       dmnContext.put("Driver", driverInfo);
 
       final JsonObject violationInfo = new JsonObject();
       violationInfo.put("Code", "speed-stop");
       // violationInfo.put("Date","01/01/2019");
       violationInfo.put("Speed Limit", 30);
       violationInfo.put("Actual Speed", 45);
 
       dmnContext.put("Violation", violationInfo);
 
       modelHeader.put("dmn-context", dmnContext);
 
       return modelHeader;
   }

```

7. Now we can re-run our quarkus:dev method and see it in action : 

```bash
[akochnev@localhost vz-pamdm-quark1]$ mvn clean package quarkus:dev -DskipTests
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< org.acme:vz-pamdm-quark1 >----------------------
.... Snipped .... 
[INFO] --- quarkus-maven-plugin:1.0.1.Final:dev (default-cli) @ vz-pamdm-quark1 ---
Listening for transport dt_socket at address: 5005
2019-12-11 06:40:58,132 INFO  [io.quarkus] (main) Quarkus 1.0.1.Final started in 1.333s. Listening on: http://0.0.0.0:8080
2019-12-11 06:40:58,134 INFO  [io.quarkus] (main) Profile dev activated. Live Coding activated.
2019-12-11 06:40:58,135 INFO  [io.quarkus] (main) Installed features: [cdi, rest-client, resteasy, resteasy-jackson, vertx, vertx-web]
2019-12-11 06:41:45,708 INFO  [org.acm.ViolationResource] (vert.x-worker-thread-3) Getting violation info with authHeaders Basic YWRtaW5Vc2VyOlJlZEhhdA==, container traffic-violation_1.0.0-SNAPSHOT, and body {"model-namespace":"https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF","model-name":"Traffic Violation","decision-name":"Should the driver be suspended?","dmn-context":{"Driver":{"Name":"Bob","Age":23,"Points":2},"Violation":{"Code":"speed-stop","Speed Limit":30,"Actual Speed":45}}}
2019-12-11 06:41:45,847 INFO  [org.acm.ViolationResource] (vert.x-worker-thread-3) Driver suspended ?  org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine$1@692bfb5e
```

Above, we can see a couple of the logging statements we added, and the response comes back the same as from our swagger UI: 

```json
{
  "type" : "SUCCESS",
  "msg" : "OK from container 'traffic-violation_1.0.0-SNAPSHOT'",
  "result" : {
    "dmn-evaluation-result" : {
      "messages" : [ {
        "dmn-message-severity" : "WARN",
        "message" : "No rule matched for decision table 'Fine' and no default values were defined. Setting result to null.",
        "message-type" : "FEEL_EVALUATION_ERROR",
        "source-id" : "_4055D956-1C47-479C-B3F4-BAEB61F1C929"
      } ],
      "model-namespace" : "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
      "model-name" : "Traffic Violation",
      "decision-name" : "Should the driver be suspended?",
      "dmn-context" : {
        "Violation" : {
          "Speed Limit" : 30,
          "Actual Speed" : 45,
          "Code" : "speed-stop"
        },
        "Driver" : {
          "Points" : 2,
          "Age" : 23,
          "Name" : "Bob"
        },
        "Fine" : null,
        "Should the driver be suspended?" : "No"
      },
      "decision-results" : {
        "_4055D956-1C47-479C-B3F4-BAEB61F1C929" : {
          "messages" : [ ],
          "decision-id" : "_4055D956-1C47-479C-B3F4-BAEB61F1C929",
          "decision-name" : "Fine",
          "result" : null,
          "status" : "SUCCEEDED"
        },
        "_8A408366-D8E9-4626-ABF3-5F69AA01F880" : {
          "messages" : [ ],
          "decision-id" : "_8A408366-D8E9-4626-ABF3-5F69AA01F880",
          "decision-name" : "Should the driver be suspended?",
          "result" : "No",
          "status" : "SUCCEEDED"
        }
      }
    }
  }
}
```

8. Now that we have some of this working, we can do just a tiny bit of cleanup : we will move two of the static properties into configuration, as they really shouldn’t be hardcoded strings in the resource (granted, there might be more static strings that could be moved into the properties file, but this start illustrates how it works): 
* Add @Configuration annotations in the resource

```java
   @ConfigProperty(name = "basic.authHeader")
   String authHeader;
 
   @ConfigProperty(name = "violation.containerId")
   String violationContainerId;

```

* Move the configuration values into application.properties

```properties
basic.authHeader=Basic YWRtaW5Vc2VyOlJlZEhhdA==
violation.containerId=traffic-violation_1.0.0-SNAPSHOT
```

* Add QueryParams for the values that I want to expose to users as query params, change the check() and utility method parameters, e.g. :

```java
public Response check(
           @QueryParam("age") int age,
           @QueryParam("points") int points,
           @QueryParam("actualSpeed") int actualSpeed) { 
       final JsonObject dmnRequest = getDmnEvalBody(age,points,actualSpeed);
….
}

private JsonObject getDmnEvalBody(final int age, final int points, final int actualSpeed) {
  /// don’t forget to pass these parameters into the json object
}

```

* Now, I can run violation checks through my new API direction using params from the command line : 

```bash

[akochnev@localhost quarkus-kieserver-client]$ curl http://localhost:8080/violation/check?age=22&points=22&actualSpeed=55

```

# Package app and deploy to OpenShift
So, now we have a working application that provides a simple interface to query violations. There are at least a few more things to clean up (for extra credit), but the app achieves most of its goals. Let’s deploy it. 

1. In order to use native compilation, you would need to have GraalVM installed and the gu utility needs to be installed as an addition:

```bash
gu install native-image
```

2. One of the biggest benefits of building the app with quarkus is to be able to compile it to native code, so let’s do it: 

```bash
[akochnev@localhost vz-pamdm-quark1]$ mvn clean package -Pnative -DskipTests
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------< org.acme:vz-pamdm-quark1 >----------------------
[INFO] Building vz-pamdm-quark1 1.0.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
.... Snipped .... 
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]    classlist:   8,530.36 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]        (cap):   1,354.68 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]        setup:   3,227.08 ms
07:08:45,880 INFO  [org.jbo.threads] JBoss Threads version 3.0.0.Final
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]   (typeflow):  22,544.91 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]    (objects):  13,944.58 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]   (features):     763.03 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]     analysis:  39,333.58 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]     (clinit):     838.09 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]     universe:   2,453.58 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]      (parse):   4,350.36 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]     (inline):   6,412.94 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]    (compile):  31,860.19 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]      compile:  45,217.52 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]        image:   3,268.57 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]        write:     649.52 ms
[vz-pamdm-quark1-1.0.0-SNAPSHOT-runner:427634]      [total]: 103,103.97 ms
[INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 105508ms
```

It does take a little while longer to finish packaging; however, now we have a natively compiled application that is small and blazingly fast: 

```bash

[akochnev@localhost vz-pamdm-quark1]$ ls -lh target/
total 42M
-rw-rw-r--. 1 akochnev akochnev 6.7K Dec 11 07:08 vz-pamdm-quark1-1.0.0-SNAPSHOT.jar
drwxrwxr-x. 3 akochnev akochnev 4.0K Dec 11 07:10 vz-pamdm-quark1-1.0.0-SNAPSHOT-native-image-source-jar
-rwxrwxr-x. 1 akochnev akochnev  41M Dec 11 07:10 vz-pamdm-quark1-1.0.0-SNAPSHOT-runner
[akochnev@localhost vz-pamdm-quark1]$ target/vz-pamdm-quark1-1.0.0-SNAPSHOT-runner 

```

And I can call it exactly the same way: 

```bash

curl http://localhost:8080/violation/check?age=22&points=22&actualSpeed=55

```

3. Build a linux container
   
```bash
[akochnev@localhost vz-pamdm-quark1]$ cp src/main/docker/Dockerfile.native . && buildah bud --tag viz-pamdm-quark1 Dockerfile.native .
STEP 1: FROM registry.access.redhat.com/ubi8/ubi-minimal
STEP 2: WORKDIR /work/
STEP 3: COPY target/*-runner /work/application
STEP 4: RUN chmod 775 /work
2019-12-11T12:15:58.000287962Z: cannot configure rootless cgroup using the cgroupfs manager
STEP 5: EXPOSE 8080
STEP 6: CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
STEP 7: COMMIT viz-pamdm-quark1
Getting image source signatures
Copying blob 26b543be03e2 skipped: already exists
Copying blob a066f3d73913 skipped: already exists
Copying blob 2c4e76571936 done
Copying config 5ff1140ea3 done
Writing manifest to image destination
Storing signatures
5ff1140ea30d0b625447c74d3ed8093994c4a2507ff2b35f35658e4cc0a99463

[akochnev@localhost vz-pamdm-quark1]$ podman images
REPOSITORY                                    TAG       IMAGE ID       CREATED         SIZE
localhost/viz-pamdm-quark1                    latest    5ff1140ea30d   8 seconds ago   150 MB
…snipped … 
```

4. Finally, push the container to quay and create the app: 

```bash
podman push 5ff1140ea30d quay.io/akochnev_redhat/viz-pamdm-quark1

oc new-app quay.io/akochnev_redhat/viz-pamdm-quark1:latest

oc expose svc viz-pamdm-quark1
```

After running these commands, the native app is running on OpenShift : 

```json
[akochnev@localhost quarkus-kieserver-client]$ curl http://viz-pamdm-quark1-pam-dm1.apps.ocp-pam-cluster-1.clusters.thinkjitsu.me/violation/check
{
  "type" : "SUCCESS",
  "msg" : "OK from container 'traffic-violation_1.0.0-SNAPSHOT'",
  "result" : {
    "dmn-evaluation-result" : {
      "messages" : [ {
        "dmn-message-severity" : "WARN",
        "message" : "No rule matched for decision table 'Fine' and no default values were defined. Setting result to null.",
        "message-type" : "FEEL_EVALUATION_ERROR",
        "source-id" : "_4055D956-1C47-479C-B3F4-BAEB61F1C929"
      } ],
      "model-namespace" : "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
      "model-name" : "Traffic Violation",
      "decision-name" : "Should the driver be suspended?",
      "dmn-context" : {
        "Violation" : {
          "Speed Limit" : 30,
          "Actual Speed" : 0,
          "Code" : "speed-stop"
        },
        "Driver" : {
          "Points" : 0,
          "Age" : 0,
          "Name" : "Bob"
        },
        "Fine" : null,
        "Should the driver be suspended?" : "No"
      },
      "decision-results" : {
        "_4055D956-1C47-479C-B3F4-BAEB61F1C929" : {
          "messages" : [ ],
          "decision-id" : "_4055D956-1C47-479C-B3F4-BAEB61F1C929",
          "decision-name" : "Fine",
          "result" : null,
          "status" : "SUCCEEDED"
        },
        "_8A408366-D8E9-4626-ABF3-5F69AA01F880" : {
          "messages" : [ ],
          "decision-id" : "_8A408366-D8E9-4626-ABF3-5F69AA01F880",
          "decision-name" : "Should the driver be suspended?",
          "result" : "No",
          "status" : "SUCCEEDED"
        }
      }
    }
  }
}
```

4. The final app source code is available on github at https://github.com/akochnev/pamdm-quarkus-lab (if you run into issues with any of the piecemeal source code) 

  ![STOP](https://placehold.it/15/008000/000000?text=+) `Congratulations, you just completed Module 3 and deployed a natively compiled quarkus app into OpenShift
`





