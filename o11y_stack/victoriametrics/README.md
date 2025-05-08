## Victoria Metrics Deployment Documentation
### Introduction
This documentation provides a detailed guide for deploying and managing Victoria Metrics in a Kubernetes environment. It covers the installation process, configuration, and how to redeploy the application. Additionally, it highlights key considerations during the redeployment process to ensure seamless operations.



### Prerequisites
Before deploying Victoria Metrics, ensure the following:

Kubernetes Cluster: You need a running Kubernetes cluster (version 1.16+).

Helm : Helm is a Kubernetes package manager used to deploy and manage the application.

Kubectl: Command-line tool for interacting with your Kubernetes cluster.

### Required resources
| **Component**            | **Memory Request** | **CPU Request** | **Memory Limit** | **CPU Limit** |
|--------------------------|--------------------|-----------------|------------------|---------------|
| **Grafana**              | 250Mi              | 0.25           | 500Mi            | 0.25         |
| **Alertmanager**         | 250Mi              | 0.25            | 500Mi            | 0.25           |
| **vmstorage**            | 300Mi              | 0.3             | 500Mi            | 0.5           |
| **VMSelect**             | 250Mi              | 0.2             | 500Mi            | 0.3           |
| **VMInsert**             | 250Mi              | 0.2             | 500Mi            | 0.3           |
| **VMAgent**              | 250Mi              | 0.2             | 500Mi            | 0.3           |
|  **VMAlert**             |250Mi               | 0.2              | 500Mi           | 0.3           |



### Data Flow
The components of Victoria Metrics work together to collect, store, and alert on metrics data. The data flow can be summarized as follows:

Node Exporter scrap metrics from the nods and  then send to VMagent

VMAgent scrapes metrics from various sources, such as Kubernetes nodes, pods, and services. It collects data at regular intervals. It forward it to Vminsert.

VMSelect handles query processing and querying the metrics stored by VMStorage.

VMInsert is responsible for ingesting data into the Victoria Metrics database (vmstorage).

VMStorage stores all the time-series data, enabling high-speed access for querying.

Alertmanager triggers alerts based on defined thresholds or rules.

VMAlert is responsible for alert it checks the vmstorage for threshold breach.

### Setup:- 

First we have to create a storage class like in our case we are using buildpiper-storage
make a storage class file and  apply it 
```
kubectl apply -f <storageclass> 
```
The file is located in the below path
```
cd central-devops-infra/prod/o11y-setup-prod
```
**Clone the Repository**:

```
git clone https://github.com/Dehaat/central-devops-infra.git
```

**Navigate to the Correct Branch**:

```
git checkout olly_setup
```

Move to the loki Distributed Chart Directory:


Move to the folder which contain the values.yaml and makefile
```
cd central-devops-infra/prod/o11y-setup-prod/victoriametrics
```
There you find 2 files Makefile and values.yaml 

![image](https://github.com/user-attachments/assets/2ed0748c-4c89-4f57-a5d3-b9a1e021c231)

open the make file 
```
cat Makefile
```
![image](https://github.com/user-attachments/assets/17ada149-d82c-4d11-8a16-f7b4aba672c9)

Run the Makefile Commands:

Get Helm Chart: Download the required chart and extract it.

```
make get-helm-chart
```
![image](https://github.com/user-attachments/assets/312b7bc7-eae4-42ab-9d39-9c545e1c2565)

Generate Kubernetes Manifest: This will render the Helm chart templates and generate the final Kubernetes manifest file.

```
make generate-manifest

```
![image](https://github.com/user-attachments/assets/961f4286-5606-411e-8364-793996074cfc)

Apply CRDs: Apply the Custom Resource Definitions (CRDs) for Victoria Metrics.
```
make apply-crd
```
![image](https://github.com/user-attachments/assets/ab28629f-b2aa-4e83-8df0-6a6c01a71e2c)

Deploy Application: This will deploy the application to your Kubernetes cluster using the generated manifests.
```
make apply
```

![image](https://github.com/user-attachments/assets/b1e28835-94da-4486-87d1-439a0b281359)


### Redeploying
Steps for Redeployment

Ensure Kubernetes Cluster and Resources are Stable: Check the cluster health, storage resources, and namespace configuration before proceeding.

Update values.yaml: Modify the values.yaml configuration file based on the required changes. This file contains configurations for all components in the stack, such as resource limits, node selectors, persistence settings, and alerting rules.

You can customize resource requests and limits (CPU, memory) for each vm component, as well as define node selectors to control the nodes on which Loki pods are scheduled. These settings are configured within the values.yaml file under the respective component specifications.

Redeploying the Chart with Updated Configurations:

Generate Kubernetes Manifest: This will render the Helm chart templates and generate the final Kubernetes manifest file based on your changes.

```
make generate-manifest

```

Redeploy Application: This will redeploy the application to your Kubernetes cluster using the generated manifest above.
```
make apply
```
To change the component images there is a  section in each component  for image .

You can change the version of the image by changing the image in the image section of the values.yaml 

**Grafana**:- 
All the pods which will generate after this one of the pod is grafana 
we just need to apply the datasource file and the dashboard files simply by 
```
Kubectl apply -f dashboard/datasourcs
```
for dashboardand datasource move to the grafana folder
![image](https://github.com/user-attachments/assets/74a45be7-1c23-4a8c-850c-ffc3154d73da)

there is also a Makefile you can also use it 

### Important considerations

**Persistence Configuration**: Ensure that the persistence configuration for Victoria Metrics is correctly set up to avoid data loss during redeployment. The values.yaml file specifies the storage configuration for various components.

**Resource Allocation**: Be cautious when updating resource requests and limits in the values.yaml file. Over-allocating resources can lead to cluster resource exhaustion, while under-allocating can cause performance degradation.


**Helm Chart Version**: Always ensure that you are using the correct Helm chart version. Mismatched versions can lead to compatibility issues. The version v0.25.5 is recommended for your current deployment.


**CRDs Compatibility**: Ensure that the CRDs are compatible with the Helm chart version you are deploying. Any changes to the CRDs should be thoroughly tested in a staging environment before applying to production.


This guide outlines the necessary steps for deploying and redeploying Victoria Metrics in a Kubernetes environment. By following the instructions provided here, you can manage your metrics collection and monitoring system efficiently. Always take necessary precautions such as backup and testing to avoid disruptions during redeployment.




