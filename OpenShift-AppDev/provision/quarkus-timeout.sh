#!/bin/bash

# Fixes the terminal timeout issue in the Quarkus lab in RHPDS

AWS_REGION=$(jq -r .aws.region ~/cluster-*/metadata.json)
LBNAME=$(sudo -u ec2-user aws --region $AWS_REGION elb describe-load-balancers | jq  '.LoadBalancerDescriptions | map(select( .DNSName == "'$(oc get svc router-default -n openshift-ingress -o jsonpath='{.status.loadBalancer.ingress[].hostname}')'" ))' | jq -r .[0].LoadBalancerName)
sudo -u ec2-user aws --region $AWS_REGION elb modify-load-balancer-attributes --load-balancer-name $LBNAME --load-balancer-attributes "{\"ConnectionSettings\":{\"IdleTimeout\":300}}"


