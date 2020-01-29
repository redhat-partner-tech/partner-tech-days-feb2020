

# Install and Configure Decision Manager (DM)

In OpenShift 4.2 the installation of PAM / DM is performed primarily through Operators. This provides a very smooth usage model, where as a user, you can just specify that you want a set of capabilities (e.g. PAM or DM), and the operator that is responsible for those capabilities will provision and configure them. 

1. Because your "normal" user account does not have OpenShift cluster administrator privileges, you are not able to install the Business Automation operator with your regular user account. In order to do accomplish that, logout from the OpenShift console and log in using your admin credentials user<100+NN> (e.g. if you're user2, log in as user102 who has admin privileges)

2. Navigate to the Operators -> Operator Hub section, and search for Automation in the search box. 

![OperatorHub](images/lab2_operatorhub.png)

3. Choose the “Business Automation” operator and click on the “Install” button to install the operator. Choose the option to install the operator in "A specific namespace on the cluster" and choose your normal user's project (e.g. userNN-project) in which the subscription will be created

![Operator Subscription](images/lab2_operator_subscription.png)

Now hit the subscribe button and keep the default settings
![Installed BA Operator](images/lab2_installed_ba_operator.png)

4. Switch into your regular user account to continue working with the operator - log out from the "admin" account (e.g. user<100+NN>) and log back in as the "regular" user account (userNN)


5. Now, navigate to the “Operators” -> Installed Operators section.You should see the newly subscribed Business Automation operator. Click into the operator tile, and on the Operator details page, click the “Create Instance” link/button

NOTE:  If you get an “Unauthorized” error on the page, make sure that the “Project” dropdown at the top left of the page has your new “userNN-project” project selected. 

![BA Operator Details](images/lab2_ba_operator_details.png)

6. Accept the default values and click the “Create” button to proceed with the creation of the KieApp custom resource (be sure to replace userNN with your actual username)
   
```yaml
apiVersion: app.kiegroup.org/v2
kind: KieApp
metadata:
 name: rhpam-trial
 namespace: userNN-project
spec:
 environment: rhpam-trial
```

The “environment” parameter here is most important - it specifies how the product will be deployed. There are other “environment” values that can be used for production, HA, authoring, etc (e.g. rhpam-trial,  rhpam-authoring, rhpam-authoring-ha, etc)  - more details are available on the OpenShift PAM/DM installation documentation . 

7. Now, stop and explore: 
* First click on the blue rhpam-trial KieApp
* Then select the resource tab and observe the multitude of resources that just got created in the cluster. If you had tried to install Decision Manager on your own, you would have had to create all of these resources on your own and would have had to make sure that they all work together as they should. The Business Automation operator did all of that work for you based on the custom KieApp resource. 
  
![BA operator resources](images/lab2_ba_operator_resources.png)
* Navigate to the Workloads menu and observe what you can see there : Pods, Deployments, and Deployment Configs . Is everything working as you would expect it to ? 
  
![DM Deployment Configs](images/lab2_dm_deployment_configs.png)

- ![STOP](https://placehold.it/15/f03c15/000000?text=+) `STOP : Do not proceed further, spend 5 minutes to try to figure out what is happening here. `

[**NEXT LAB -> DM Operator Troubleshooting**](2_2_Troubleshoot_Operator.md)
