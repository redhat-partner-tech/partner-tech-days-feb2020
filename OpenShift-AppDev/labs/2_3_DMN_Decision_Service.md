## Create a DMN decision service

Now that we have all of our components up and running, we will use Decision Manager to build a simple service using [DMN](https://access.redhat.com/documentation/en-us/red_hat_process_automation_manager/7.1/html/designing_a_decision_service_using_dmn_models/dmn-con_dmn-models).

First, a few words about [Decision Manager](https://www.redhat.com/en/technologies/jboss-middleware/decision-manager) and DMN, and why would we want to use these tools for our service. There are many domains such as insurance, healthcare, and others where the development teams in an organization need to work very closely with the domain experts. In those circumstances, it is imperative that the business rules and the domain knowledge is extracted from the code of the product, and is made available to the domain experts who can build and evolve complex rules for making decisions.

DMN is a fully supported standard in Decision Manager, and we will use it to describe a decision that will be made. Decision Manager provides a comprehensive set of tools for building, testing, and deploying DMN models on OpenShift. We will use these capabilities to build a decision service, and then, we will expose that decision using the Decision Manager runtime (kieserver) as a REST service that can be consumed by other services.

1. Navigate to **Networking -> Routes** and click through the **rhpam-trial-rhpamcentr** route.

![Business Central Route](images/lab23_bizcentral_route.png)

2. That will take you to the Business Central login screen. Login as : **“adminUser” / “RedHat”**, then click through to the **“Design”** section of Business Central

![Open Business Central](images/lab23_open_bizcentral.png)

**NOTE**: If you ever forget the credentials for Business Central, you can always easily find them by inspecting the rhpam-trial custom resources after it is created (**Catalog** -> **Installed Operator** -> **KieApp** -> **rhpam-trial** -> **YAML** tab)

```yaml

apiVersion: app.kiegroup.org/v2
kind: KieApp
metadata:
 annotations:
   app.kiegroup.org: 1.2.1
 creationTimestamp: '2019-12-11T08:47:49Z'
 generation: 8
 name: rhpam-trial
 namespace: user1-project
 resourceVersion: '37323'
 selfLink: /apis/app.kiegroup.org/v2/namespaces/pam-dm1/kieapps/rhpam-trial
 uid: e8d79bc7-1bf2-11ea-a3f0-0a133e3734e8
spec:
 auth: {}
 commonConfig:
   amqPassword: RedHat
   mavenPassword: RedHat
   adminPassword: RedHat
   amqClusterPassword: RedHat
   controllerPassword: RedHat
   dbPassword: RedHat
   adminUser: adminUser
   applicationName: rhpam-trial
   keyStorePassword: RedHat
   serverPassword: RedHat
   imageTag: 7.5.1
 environment: rhpam-trial
 objects:
   console:
     resources: {}
   servers:
     - deployments: 1
       name: rhpam-trial-kieserver
       resources: {}
 upgrades:
   enabled: false
   minor: false
 version: 7.5.1
```

3. A space is a logical grouping of projects in Business Central that helps users organize their work. Click on **Menu->Projects** to display the available projects, then click on the **My Space** space.
![Show Menu Project](images/dm_menu_projects.png)

A space is a logical grouping of projects in Business Central that helps users organize their work.

![Show MySpace Project](images/lab23_myspace_details.png)

4. On the space details page, click on the “Try Samples” button, choose the “Traffic Violation” project and click “OK” to proceed with the import.

![Try Samples](images/lab23_try_samples.png)

![Traffic Violation](images/lab23_traffic_violation_sample.png)


The result of this process is a very simple Decision implemented using DMN, a couple of data objects, and a few test scenarios. For now, we will focus on working with the DMN model.

![Traffic Violation Model](images/lab23_traffic_violation_model.png)

5.  Click through the **Traffic Violation** DMN model. This very simple model shows that there are two inputs into the decision (indicated by an oval in the decision model):
* Driver - details about the driver for which the decision is being made, e.g. such as the driver's name, age, etc.
* Violation - information about the specific violation that occurred

Based on these inputs, the DMN makes two decisions : whether a fine should be applied to the driver, and whether the driver's license should be suspended. Each one of those decisions is implemented in a decision table (indicated by a rectangle in the DMN model)

![DMN Model](images/lab23_dmn_model.png)



![Decision table](images/lab23_decision_table.png)

The decision table encodes a number of rules with the specific data points that are needed for the decision. You can see that the Deision Table can use special syntax (using the FEEL expression syntax) to indicate ranges (e.g. **[10..30)** or comparisons **>=30**)

6. Play around with the Violation Scenarios and see how TDD is implemented in this space. Click on the **Validation Scenarios** Test Scenario to see the test scenarios run

![Decision Scenarios](images/lab23_decision_tests.png)

7. Whenever you’re satisfied with the logic of the model, navigate back to the main **Traffic Violation** assets screen, click on the Build button, and then on the Deploy Button. You should see some green success messages indicating that the decision is successfully deployed

![Decision Model Build Deploy](images/lab23_build_deploy.png)


8. Now, navigate to the **Menu** -> **Deploy** -> **Execution servers** of Business Central. You will now observer that the Traffic Violation decision is deployed to the kieservers and that it can be started/stopped, etc.

![Deploy Decision Service](images/lab23_deploy_decision_service.png)

## Explore the REST APIs available through the kieserver and Swagger UI

This is where the fun begins - you can now interact with the Decision Model that you deployed through the REST APIs available through kieserver

1. Go back to the OpenShift console, and navigate to **Networking** -> **Routes**, and copy the URL of the *rhpam-trial-kieserver* route. Paste it into a new browser window, and append /docs to the end of that URL, e.g: https://rhpam-trial-kieserver-http-userNN-project.apps.<your-openshift-base-url>/docs/

![KieServer Docs URL](images/lab23_kieserver_docs_url.png)


2. This opens up the Swagger UI console which allows us to play a little bit with the REST APIs. There are many different APIs to explore here, but we will focus on DMN at this time.

![KieServer DMN Models Swagger UI](images/lab23_swagger_ui.png)

Click on the DMN models section and pick the “GET” request to /server/containers/<containerId>/dmn , then click the **Try it Out** button

![Swagger UI DMN Get](images/lab23_dmn_tryout.png)


3. For this request, we will use **traffic-violation_1.0.0-SNAPSHOT** for the **containerId** parameter (which is a concatenation of the Maven artifactId and version) and “application/json” as the content type, then click the **Execute** button.

![DMN Get Tryout](images/lab23_dmn_tryout.png)

4. The kieserver prompts for authentication (you should use your kieserver credentials - "adminUser" / "RedHat") and gives back details about the DMN model:


```json
{
  "type": "SUCCESS",
  "msg": "OK models successfully retrieved from container 'traffic-violation_1.0.0-SNAPSHOT'",
  "result": {
    "dmn-model-info-list": {
      "models": [
        {
          "model-namespace": "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
          "model-name": "Traffic Violation",
          "model-id": "_1C792953-80DB-4B32-99EB-25FBE32BAF9E",
          "decisions": [
            {
              "decision-id": "_4055D956-1C47-479C-B3F4-BAEB61F1C929",
              "decision-name": "Fine"
            },
            {
              "decision-id": "_8A408366-D8E9-4626-ABF3-5F69AA01F880",
              "decision-name": "Should the driver be suspended?"
            }
          ],
          "inputs": [
            {
              "inputdata-id": "_1929CBD5-40E0-442D-B909-49CEDE0101DC",
              "inputdata-name": "Violation",
              "inputdata-typeRef": {
                "namespace-uri": "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
                "local-part": "tViolation",
                "prefix": ""
              }
            },
            {
              "inputdata-id": "_1F9350D7-146D-46F1-85D8-15B5B68AF22A",
              "inputdata-name": "Driver",
              "inputdata-typeRef": {
                "namespace-uri": "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
                "local-part": "tDriver",
                "prefix": ""
              }
            }
          ],
         …. Snipped for brevity …
          "decisionServices": []
        }
      ]
    }
  }
}
```

The top of the response gives us the key elements that we care about :
* The correct **model-namespace** value
* **Two inputs** that we will need to provide to invoke the decision service : *Driver* and *Violation*
* **Two different decisions** that we can trigger *“Fine”* and *“Should driver be suspended?”*
* If we navigate back to the DMN for the decision, and we choose the “**Data Model**” tab, we can see what properties we can set on the input models

![Data Model Properties](images/lab23_datamodel_props.png)


5. With that information (and based on the Decision Manager documentation), we can now move on to executing the DMN using the POST method body in the Swagger UI:



Inputs:
* **containerId**  - use "traffic-violation_1.0.0-SNAPSHOT" which is the maven artifact ID of the decision service deployed to the kie server
* *body* - copy the the "model-namespace" GUID value  from the previous step (_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF) and use that to replace the <replace-with-model-namespace-id> placeholder below

Inputs:
* containerId  - use "traffic-violation_1.0.0-SNAPSHOT" which is the maven artifact ID of the decision service deployed to the kie server
* body - copy the the "model-namespace" GUID value  from the previous step (_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF) and use that to replace the <replace-with-model-namespace-id> placeholder below

```json
{
  "model-namespace":"https://github.com/kiegroup/drools/kie-dmn/<replace-with-model-namespace-id>",
  "model-name":"Traffic Violation",
  "decision-name": "Should the driver be suspended?",
  "decision-id":null,
  "dmn-context":{
    "Driver":{
        "Name": "Bob",
        "Age": "23",
        "Points": 2
    },
    "Violation":  	{
    		"Code": "speed-stop",
    		"Date": "01/01/2019",
                "Type": "speed",
                "Speed Limit": 30,
                "Actual Speed": 45
    }
  }
}
```
![DMN Execute Post](images/lab23_execute_dmn_post.png)

… and the kieserver , obligingly responds with the decision …

```json

{
  "type" : "SUCCESS",
  "msg" : "OK from container 'traffic-violation_1.0.0-SNAPSHOT'",
  "result" : {
    "dmn-evaluation-result" : {
      "messages" : [ ],
      "model-namespace" : "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
      "model-name" : "Traffic Violation",
      "decision-name" : "Should the driver be suspended?",
      "dmn-context" : {
        "Violation" : {
          "Type" : "speed",
          "Speed Limit" : 30,
          "Actual Speed" : 45,
          "Code" : "speed-stop",
          "Date" : "01/01/2019"
        },
        "Driver" : {
          "Points" : 2,
          "Age" : "23",
          "Name" : "Bob"
        },
        "Fine" : {
          "Points" : 3,
          "Amount" : 500
        },
        "Should the driver be suspended?" : "No"
      },
      "decision-results" : {
        "_4055D956-1C47-479C-B3F4-BAEB61F1C929" : {
          "messages" : [ ],
          "decision-id" : "_4055D956-1C47-479C-B3F4-BAEB61F1C929",
          "decision-name" : "Fine",
          "result" : {
            "Points" : 3,
            "Amount" : 500
          },
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

6. The swagger UI does give us the command line version to execute (note that I added a “-k” argument to accept the self signed certificate); however, that fails with an “Unauthorized” error. Of course - we didn’t give it credentials (and we did authenticate in the browser to allow the execution from the swagger UI)

```bash
akochnev@localhost quarkus-kieserver-client]$ curl -k -X POST "https://rhpam-trial-kieserver-pam-dm1.apps.ocp-pam-cluster-1.clusters.<your-openshift-base-url>/services/rest/server/containers/traffic-violation_1.0.0-SNAPSHOT/dmn" -H "accept: application/xml" -H "content-type: application/json" -d "{ \"model-namespace\":\"https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF\", \"model-name\":\"Traffic Violation\", \"decision-name\": \"Should the driver be suspended?\", \"dmn-context\":{ \"Driver\":{ \"Name\": \"Bob\", \"Age\": \"23\", \"Points\": 2 }, \"Violation\": \t{ \t\t\"Code\": \"speed-stop\", \t\t\"Date\": \"01/01/2019\", \"Type\": \"speed\", \"Speed Limit\": 30, \"Actual Speed\": 45 } }}"
<html><head><title>Error</title></head><body>Unauthorized</body></html>
```

7. Let's try again exercising the REST API, this time on the Code Ready Workspaces command line:
* Do base64 encoding on the username/password to allow passing the credentials with basic auth to curl

```bash
$ AUTH=$(echo -ne "adminUser:RedHat" | base64 --wrap 0)
$ echo $AUTH
YWRtaW5Vc2VyOlJlZEhhdA==
```

* In Code Ready Worspaces project folder, create a file named "**violation-data.json**", paste the contents of the **body** parameter above into the file, and re-run the DMN curl execution, this time with the proper authentication

(note that I added a '-k' parameter to ignore the self signed certificate and added a "-d" parameter to reference the file with the body data)
```bash
$ curl -X POST "http://rhpam-trial-kieserver-http-user2-project.apps.cluster-nisky-c155.nisky-c155.open.redhat.com/services/rest/server/containers/traffic-violation_1.0.0-SNAPSHOT/dmn" -k -H "Authorization:Basic YWRtaW5Vc2VyOlJlZEhhdA==" -H "accept: application/json" -H "content-type: application/json" --data-binary @violation-data.json
{
  "type" : "SUCCESS",
  "msg" : "OK from container 'traffic-violation_1.0.0-SNAPSHOT'",
  "result" : {
    "dmn-evaluation-result" : {
      "messages" : [ ],
      "model-namespace" : "https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF",
      "model-name" : "Traffic Violation",
      "decision-name" : "Should the driver be suspended?",
      "dmn-context" : {
        "Violation" : {
          "Type" : "speed",
          "Speed Limit" : 30,
          "Actual Speed" : 45,
          "Code" : "speed-stop",
          "Date" : "01/01/2019"
        },
        "Driver" : {
          "Points" : 2,
          "Age" : "23",
          "Name" : "Bob"
        },
        "Fine" : {
          "Points" : 3,
          "Amount" : 500
        },
        "Should the driver be suspended?" : "No"
      },
      "decision-results" : {
        "_4055D956-1C47-479C-B3F4-BAEB61F1C929" : {
          "messages" : [ ],
          "decision-id" : "_4055D956-1C47-479C-B3F4-BAEB61F1C929",
          "decision-name" : "Fine",
          "result" : {
            "Points" : 3,
            "Amount" : 500
          },
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

![STOP](https://placehold.it/15/008000/000000?text=+) `Congratulations, you just completed Module 2 and have a Decision Service implemented in DMN running on OpenShift, and you interacted with kieserver’s REST APIs
`

[**NEXT LAB -> Extend Quarkus App**](2_4_Extend_Quarkus_App.md)







