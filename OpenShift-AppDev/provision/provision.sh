#!/bin/bash

# usage : ./provision.sh  opentlcUser labGuid
# Parameters: 
#   opentlcUser - the SA opentlc username who provisioned the labs in RHPDS
#   labGuid - the combination of city-name and a unique string
# Prompts:
#   * First, the script will prompt for the ssh password for the bastion host
#   * Then, it will prompt for the opentlc-mgr password for the cluster
# Both prompts are provided by RHPDS when the lab has completed provisioning

# e.g. ./provision.sh akochnev-redhat.com nisky-c155
OPENTLC_USER="$1"
LAB_GUID="$2"

# First, go and fix the timeout issue with the terminals
echo "Fixing terminal timeout issue in quarkus lab"
ssh $OPENTLC_USER@bastion.$LAB_GUID.open.redhat.com 'bash -s' < ./quarkus-timeout.sh


# Then, login as the cluster admin and make users 100-200 cluster admins as well
echo "Granting cluster admin to users 100-200"
oc login https://api.cluster-$LAB_GUID.$LAB_GUID.open.redhat.com:6443/ -u opentlc-mgr --insecure-skip-tls-verify

# There are 200 users created out of the box in the cluster, so we could use users1-100 as the regular users and users 100-200 as the admin users
for ((i=1;i<=100;i++))  do     
    userNum=$(( 100 + $i ))
    adminUser="user$userNum"
    echo "Creating admin user $userNum" 
    oc adm policy add-cluster-role-to-user cluster-admin $adminUser
done

