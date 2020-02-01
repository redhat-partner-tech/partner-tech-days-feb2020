# Introduction
In this lab, we will use Red Hat Decision Manager to build a decision service using business rules to determine whether a driver needs to be fined and the his/her license suspended in a traffic violation scenario,  we will deploy it to OpenShift , then test it using its REST interface. Then, we will enhance the Quarkus "people" microservice that we had already built to integrate with the Decision Manager REST API to provide a simpler interface to invoking the Traffic Violation service. 


# Prerequisites

1. **Red Hat Developer Account**: If you do not have a Red Hat developer account, go to https://access.redhat.com/terms-based-registry/ and click on the Register link to create a developer account. 

2. **REST API Tooling**:  We will be exploring some REST APIs - we will be using [curl](https://curl.haxx.se/docs/manpage.html) and the [Swagger UI](https://swagger.io/tools/swagger-ui/) for our services, and there is nothing to install for these tools, as they are provided in the lab environment. You could also use a tool like PostMan on your workstation (https://www.getpostman.com/) which has a nice GUI for exploring REST APIs . 
   

3. **Privileged Account**: In this lab, we will be using an account with Cluster Administrator privileges to perform some of the task. Your "cluster admininstrator" username will be the similar to the userNN that you used in the Quarkus workshop, but your "administrator" user index will be "100 more" than your "regular user" index. For example, if your regular "userNN" is "user6", then your "administrator user" will be "user106". The password for your "privileged account" is the same as your "normal user" account. 
*NOTE*: Please be mindful of using your privileged account



[**Next LAB -> DM Operator Setup**](2_1_Operator_Setup.md)