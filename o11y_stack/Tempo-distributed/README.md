## Documentation for Tempo Stack

### Table of content :-
* [Introduction](#Introduction)
* [ Prerequisites](#Prerequisites)
* [Resources](#Resources)
* [Architecture and flow](#architecture-and-flow)
* [Steps to set-up](#Steps-to-set-up)
* [Redeploying ](#Redeploying )
* [Important Considerations During Redeployment](#important-considerations-during-redeployment)

### Introduction
This document outlines the architecture, prerequisites, resource requirements, data flow, and redeployment process for the Tempo stack configured using the provided values.yaml.The aim of this  Tempo stack is to collect, aggregate, store, and provide querying capabilities for traces within the Kubernetes cluster

###  Prerequisites
Before deploying this tempo stack, ensure the following prerequisites are met:

**Kubernetes Cluster:**  A running Kubernetes cluster .

**Helm:** Helm package manager installed and configured to interact with your Kubernetes cluster.

**Storage Provisioner:**  A default StorageClass configured in your cluster like in our case  buildpiper-storage .

### Resources:

| **Component**        | **CPU Requests** | **CPU Limits** | **Memory Requests** | **Memory Limits** |
|----------------------|------------------|----------------|----------------------|-------------------|
| **Ingester**         | 0.3              | 0.5            | 300Mi                | 600Mi             |
| **Metrics Generator**| 0.2              | 0.4            | 200Mi                | 400Mi             |
| **Distributor**      | 0.2              | 0.4            | 200Mi                | 400Mi             |
| **Compactor**        | 0.3              | 0.5            | 400Mi                | 700Mi             |
| **Querier**          | 0.2              | 0.4            | 200Mi                | 400Mi             |
| **Query Frontend**   | 0.3              | 0.5            | 300Mi                | 500Mi             |
| **Memcached**        | 0.3              | 0.5            | 400Mi                | 700Mi             |

**Important Considerations:**

The resource requests and limits should be adjusted based on the actual log volume and query load in your environment.

Ensure your Kubernetes nodes have sufficient resources to accommodate these deployments.

The persistent volume sizes for Ingesters, Memcached, and Index Gateway should be determined based on your retention policies and expected data growth.

### Architecture and flow 

![Screenshot 2025-04-11 005237](https://github.com/user-attachments/assets/ea5d6b85-2794-49b8-8ef6-625b530f1bd6)

The above diagram shows the working architecture of Grafana Tempo.

Firstly, the distributor receives spans in different formats  OpenTelemetry and sends these spans to ingesters by hashing the trace ID. Ingester then creates batches of traces which are called blocks.

Then it sends those blocks to the backend storage (S3/GCS). When you have a trace ID that you want to troubleshoot, you will use Grafana UI and put the trace ID in the search bar. Now querier is responsible for getting the details from either ingester or object storage about the trace ID you entered.

Firstly, it checks if that trace ID is present in the ingester; if it doesn’t find it, it then checks the storage backend.  Meanwhile, the compactor takes the blocks from the storage, combines them, and sends them back to the storage to reduce the number of blocks in the storage.

### Steps to set-up:

**Clone the Repository**:

```
git clone https://github.com/Dehaat/central-devops-infra.git
```

**Navigate to the Correct Branch**:

```
git checkout olly_setup
```

Move to the Tempo Distributed Chart Directory:

```
cd central-devops-infra/prod/o11y-setup-prod/tempo-distributed
```
This directory contains the Helm chart for deploying Tempo in a distributed configuration.

**Understand Helm Chart Files**:

Chart.yaml: This file holds metadata about the Tempo Helm chart, crucial for Helm to identify, version, and manage the Tempo deployment.
values.yaml: This file provides configuration values for the Tempo Helm chart. It serves as a central point for customizing various aspects of the Tempo deployment without directly modifying the chart's templates. By providing a custom values.yaml, you override the default settings.
**Update Helm Chart Dependencies**:
```
helm dependency update
```
This command fetches and updates any dependent charts defined in the Chart.yaml file. Upon successful execution, it generates or updates the Chart.lock file, pinning the exact versions of the dependencies used. It also creates a charts/ directory containing the downloaded dependencies, their default values.yaml, and templates.
![image](https://github.com/user-attachments/assets/54972659-a6ff-4e04-a5f8-5c0b92eb93ff)


**Deploy Tempo using Helm**:
```
helm upgrade --install tracing . -n observability --create-namespace -f values.yaml
```
This command either upgrades an existing Tempo deployment named tracing in the observability namespace or installs it if it doesn't exist. It utilizes the Helm chart located in the current directory (.) and applies the configurations specified in the values.yaml file. The --create-namespace flag ensures the observability namespace is created if it's not already present. The dot (.) specifies the current directory as the location of the Chart.yaml file. If your Chart.yaml is located elsewhere, replace . with the correct path.

![image](https://github.com/user-attachments/assets/5acf3aa1-8129-4f2b-8188-f8db4d456683)

**Verify the Deployment:**
```
kubectl get pods -n observability
```
This command lists the pods running within the observability namespace, allowing you to verify that the Tempo pods have been successfully created and are in a Running or Ready state.

**Scaling and Versioning:**

*Scaling*:

To ensure optimal performance and availability under varying loads, the Tempo deployment supports horizontal scaling via Kubernetes replica counts. While autoscaling is generally disabled by default, it can be enabled for the ingester, distributor, and queryFrontend components based on CPU or memory utilization metrics by adjusting their configurations in the values.yaml file.

*Dependency Versioning*:

The dependencies section in the Chart.yaml file enables you to manage the versions and sources of dependent Helm charts. For example:

```
dependencies:
  - name: tempo-distributed
    alias: tempo
    version: 1.9.3
    repository: https://grafana.github.io/helm-charts
```
Modify the version, alias, or repository fields as needed and run helm dependency update to apply the changes. This allows you to utilize specific versions or internal/custom Helm repositories.

*Tempo Application Versioning*:

The version of the Tempo application deployed is controlled by the image.tag field within the tempo.image section of the values.yaml file:

```
tempo:
  image:
    registry: docker.io
    repository: grafana/tempo
    tag: <version>
```
Changing the <version> tag will update the Tempo version for all core components. For production deployments, it is highly recommended to specify stable versions instead of using latest.

Alternatively, you can specify different image versions for individual Tempo components by locating the specific image configuration block under each component's definition in the values.yaml file but it is not recommended in prod.

**Resource Management and Node Selection**:

You can customize resource requests and limits (CPU, memory) for each Tempo component, as well as define node selectors to control the nodes on which Tempo pods are scheduled. These settings are configured within the values.yaml file under the respective component specifications.

Redeploying the Chart with Updated Configurations:

After modifying the values.yaml file, apply the changes to your Tempo deployment using the following command:

```
helm upgrade tracing . -f values.yaml -n observability
```
Note: If your Tempo release is in a different namespace, replace -n observability with the correct namespace.

**Troubleshooting:**

Pods Stuck in Pending State:

If Tempo pods are stuck in a Pending state, investigate the following:

*Describe the Pod*: Obtain detailed information about the pod's scheduling status and any events:
```
kubectl describe po <pod_name> -n observability
```
Check Pod Logs: If the pod has started but is failing, examine its logs:
```
kubectl logs <pod_name> -n observability
```
StatefulSet PVC Status: For StatefulSets, verify if the PersistentVolumeClaims (PVCs) have been created and are bound to the correct StorageClass:
```
kubectl get pvc -n observability
```
If a PVC is in a Pending state, describe it for more details:
```
kubectl describe pvc <pvc_name> -n observability
```
Taints and Tolerations: If pods are failing to schedule due to taints on nodes, add the necessary tolerations to the corresponding pod specification in the values.yaml file and redeploy the Helm chart.

#### Tempo integration with otel :
Move to the otel folder having the path 
*central-devops-infra/prod/o11y-setup-prod/otel*
there you find a Make file which basically contains some command to setup otel . Along with Make file there is a folder otel collector move to thet respective folder . This folder contains a otel-collector.yaml in this there is a section exporter which which you need to edit if required . As per data flow the otel collector after collecting the traces forward them to the distributor so we just need to add the distributor service along with port as shown in image below:-
![image](https://github.com/user-attachments/assets/ea7059c2-3514-4ea5-8344-d45d3d22fba8)
After adding this there is s section below services and in the services section you have traces section include the exporter name as same as above

![image](https://github.com/user-attachments/assets/9e4e5711-1afb-490c-bce4-590be5fbeb84)

after making these changes move one folder back which basically contains the Makefile open the make file ans nun command one by one in it 
**make get-helm-chart**: Downloads the specified Helm chart (otel-operator-1.0.0.tgz) from the given GitHub release URL, saves it with the correct filename, and then extracts its contents.

**make  generate-manifest**: Uses Helm to generate the Kubernetes manifests for the otel-operator chart located in the otel-operator/ directory. It names the release otel, targets the observability namespace, and applies configurations from the values.yaml file. The output is the raw Kubernetes YAML, but it's only printed to the console and not applied to the cluster.

**apply-helm**: First, it creates the observability namespace if it doesn't exist using kubectl create namespace with a dry-run to output the YAML, which is then applied.
Then, It applies  the above generated manifests to your Kubernetes cluster.
**apply-otel**: Applies Kubernetes configurations from two local files: instrumentation and otel-collector. These files likely contain YAML definitions for OpenTelemetry Instrumentation resources and OpenTelemetry Collector deployments/configurations.

These steps will setup the otel in observability ns to verify this run below command :-
```
kubectl get pods -n observability
```
You see otel pods are up and running 
![image](https://github.com/user-attachments/assets/f0e7c443-947e-4595-985a-a08c57965b40)
 


 I
To redeploy your tempo stack after making changes to the values.yaml file, you will typically use the Helm command-line interface. Assuming you initially deployed tempo using a Helm release named tracing .
Navigate to the directory containing your updated values.yaml file.

Run the Helm upgrade command:
```
helm upgrade tracing . -f values.yaml -n logging
```

The . in the command refers to the current directory where your Helm chart (if you have one locally) is located. If you deployed from a remote chart repository, you would specify the chart name and repository again 

Monitor the rollout: After running the upgrade command, Helm will apply the changes defined in your updated values.yaml to your Kubernetes cluster. You can monitor the status of the deployments and stateful sets using kubectl:

### Important Considerations During Redeployment

**Rolling Updates:** Helm typically performs rolling updates for Deployments and StatefulSets, minimizing downtime. However, depending on the changes you've made (e.g., significant resource changes, image updates), some pods might be temporarily unavailable.

**Persistent Volumes:** Changes to persistent volume claims (size, storage class) might not be applied automatically and could require manual intervention or recreation of the resources. Be cautious when modifying these sections.

**Syntax Errors:** Ensure your values.yaml file is syntactically correct (YAML format). Helm will likely fail to upgrade if there are errors in the file.

### Note:- 

Can Validate if the data is coming or not by visualizing it on grafana . For grafana the end point will be the loki-frontend-query svc.

 Configuring tempo to use S3/GCS storage in your production environment, you'll benefit from improved durability, scalability, and cost-effectiveness for your tracing infrastructure. Remember to secure your S3/GCS bucket with appropriate  policies

By following these steps, you can effectively manage and update your Tempo stack configuration using Helm and the values.yaml file. Remember to always review the changes you make to values.yaml and understand their potential impact on your running system.








