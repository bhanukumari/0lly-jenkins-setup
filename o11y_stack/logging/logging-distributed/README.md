## Documentation for  Logging Stack

### Table of content :-
* [Introduction](#Introduction)
* [ Prerequisites](#Prerequisites)
* [Required Resources](#Required_Resources)
* [Data Flow](#Data_Flow)
* [Steps to set-up](#Steps-to-set-up)
* [Important Considerations During Redeployment](#Important_Considerations_During_Redeployment)

### Introduction
This document outlines the architecture, prerequisites, resource requirements, data flow, and redeployment process for the logging stack configured using the provided values.yaml.The aim of this Loki logging stack is to collect, aggregate, store, and provide querying capabilities for logs within the Kubernetes cluster

###  Prerequisites
Before deploying this Loki stack, ensure the following prerequisites are met:

**Kubernetes Cluster:**  A running Kubernetes cluster .

**Helm:** Helm package manager installed and configured to interact with your Kubernetes cluster.

**Storage Provisioner:**  A default StorageClass configured in your cluster like in our case  buildpiper-storage .

###  Required Resources
| Component              | Requests (CPU / Memory) | Limits (CPU / Memory) |
|------------------------|-------------------------|------------------------|
| Logging Gateway        | 0.2 / 300Mi             | 0.4 / 500Mi            |
| Ingester               | 0.5 / 600Mi             | 0.7 / 800Mi            |
| Distributor            | 0.3 / 500Mi             | 0.6 / 800Mi            |
| Querier                | 0.2 / 400Mi             | 0.4 / 700Mi            |
| Query Frontend         | 0.2 / 300Mi             | 0.4 / 600Mi            |
| Memcached (Chunks)     | 0.3 / 400Mi             | 0.5 / 700Mi            |
| Memcached (Frontend)   | 0.3 / 400Mi             | 0.5 / 700Mi            |
| Memcached (Index)      | 0.3 / 400Mi             | 0.5 / 700Mi            |
| Index Gateway          | 0.4 / 400Mi             | 0.7 / 700Mi            |


**Important Considerations:**

The resource requests and limits should be adjusted based on the actual log volume and query load in your environment.

Ensure your Kubernetes nodes have sufficient resources to accommodate these deployments.

The persistent volume sizes for Ingesters, Memcached, and Index Gateway should be determined based on your retention policies and expected data growth.

### Data Flow

![image](https://github.com/user-attachments/assets/9d55674e-3bf4-4c7a-af87-f073d8202e49)

The log data flows through the system as follows:

Log Generation: Applications within the Kubernetes cluster generate log data.

Promtail: Promtail, deployed on each node (typically as a DaemonSet), scrapes logs from configured sources (e.g., container logs, system logs).

Logging Gateway : Promtail forwards the scraped logs to the Logging Gateway  service at http://logging-gateway.logging.svc.cluster.local/loki/api/v1/push.
then it routing the /loki/api/v1/push requests to the Loki Distributor service.

Loki Distributor: The Distributor receives the log entries, processes them, and batches them before sending them to the Ingesters. It uses a consistent hashing mechanism based on the log stream's labels to determine which Ingester should receive the data.

Loki Ingester: The Ingesters are responsible for building chunks of log data in memory and then flushing these chunks to the backend storage  periodically or when they reach a certain size. They also maintain an in-memory index for faster querying.

Loki Querier: When a query is received (typically via the Logging Gateway at /api/prom/* or /loki/api/*), the Queriers fetch the relevant chunks from the backend storage and the in-memory index from the Ingesters to process the query and return the results. The Query Frontend can be used to optimize and parallelize queries.

Loki Query Frontend: The Query Frontend is an optional component that can sit in front of the Queriers. It can optimize queries, split them into smaller parts, and cache results (using Memcached in this configuration).

Loki Compactor: The Compactor is responsible for compacting the index files in the backend storage, which improves query performance and reduces storage costs over time. It also handles retention policies if enabled.

Loki Ruler: The Ruler component periodically evaluates configured alerting and recording rules. If alerts are triggered, it sends them to the configured Alertmanager instance.

Memcached: This distributed caching system is used by various Loki components (Chunks, Frontend, Index Queries) to improve performance by caching frequently accessed data.

Index Gateway: This component can be used to separate index reads and writes, potentially improving scalability for large Loki deployments.
### Steps to set-up

**Clone the Repository**:

```
git clone https://github.com/Dehaat/central-devops-infra.git
```

**Navigate to the Correct Branch**:

```
git checkout olly_setup
```

Move to the loki Distributed Chart Directory:

```
 cd central-devops-infra/prod/o11y-setup-prod/logging/loki-distributed
```
This directory contains the Helm chart for deploying loki in a distributed configuration.

**Understand Helm Chart Files**:

Chart.yaml: This file holds metadata about the Loki Helm chart, crucial for Helm to identify, version, and manage the loki deployment.
values.yaml: This file provides configuration values for the Loki Helm chart. It serves as a central point for customizing various aspects of the Loki deployment without directly modifying the chart's templates. By providing a custom values.yaml, you override the default settings.

**Update Helm Chart Dependencies**:
```
helm dependency update
```
This command fetches and updates  dependent charts defined in the Chart.yaml file. Upon successful execution, it generates or updates the Chart.lock file, pinning the exact versions of the dependencies used. It also creates a charts/ directory containing the downloaded dependencies, their default values.yaml, and templates.
![image](https://github.com/user-attachments/assets/4b68e093-e4a6-4d9a-bfd7-03213e90be9e)


**Deploy Loki using Helm**:
```
helm upgrade --install loki . -n logging --create-namespace -f values.yaml
```
This command either upgrades an existing Loki deployment  in the logging namespace or installs it if it doesn't exist. It utilizes the Helm chart located in the current directory (.) and applies the configurations specified in the values.yaml file. The --create-namespace flag ensures the logging namespace is created if it's not already present. The dot (.) specifies the current directory as the location of the Chart.yaml file. If your Chart.yaml is located elsewhere, replace . with the correct path.

![image](https://github.com/user-attachments/assets/ea1c2c96-2c94-43ff-bfdb-6a809466f432)

**Verify the Deployment:**
```
kubectl get pods -n logging
```
This command lists the pods running within the logging namespace, allowing you to verify that the Loki pods have been successfully created and are in a Running or Ready state.

**Versioning:**

*Dependency Versioning*:

The dependencies section in the Chart.yaml file enables you to manage the versions and sources of dependent Helm charts. 

Modify the version, alias, or repository fields as needed and run helm dependency update to apply the changes. This allows you to utilize specific versions or internal/custom Helm repositories.

*Loki Application Versioning*:

The version of the Loki application deployed is controlled by the image.tag field within the tempo.image section of the values.yaml file:

```
 loki:
    image:
      registry: docker.io
      repository: grafana/loki
      tag: 2.9.10
```

for Promtail there is also versioning section:
```
promtail:
  image:
    registry: docker.io
    repository: grafana/promtail
    tag: 3.0.0
```

Changing the  tag will update the loki and promtail version for all  components. For production deployments, it is highly recommended to specify stable versions instead of using latest.

Alternatively, you can specify different image versions for individual Loki components by locating the specific image configuration block under each component's definition in the values.yaml file but it is not recommended in prod.

**Resource Management and Node Selection**:

You can customize resource requests and limits (CPU, memory) for each Loki component, as well as define node selectors to control the nodes on which Loki pods are scheduled. These settings are configured within the values.yaml file under the respective component specifications.

Redeploying the Chart with Updated Configurations:

After modifying the values.yaml file, apply the changes to your Loki deployment using the following command:

```
helm upgrade loki . -f values.yaml -n logging
```

**Troubleshooting:**

Pods Stuck in Pending State:

If loki pods are stuck in a Pending state, investigate the following:

*Describe the Pod*: Obtain detailed information about the pod's scheduling status and any events:
```
kubectl describe po <pod_name> -n logging
```
Check Pod Logs: If the pod has started but is failing, examine its logs:
```
kubectl logs <pod_name> -n logging
```
StatefulSet PVC Status: For StatefulSets, verify if the PersistentVolumeClaims (PVCs) have been created and are bound to the correct StorageClass:
```
kubectl get pvc -n logging
```
If a PVC is in a Pending state, describe it for more details:
```
kubectl describe pvc <pvc_name> -n logging
```
Taints and Tolerations: If pods are failing to schedule due to taints on nodes, add the necessary tolerations to the corresponding pod specification in the values.yaml file and redeploy the Helm chart.



### Important Considerations During Redeployment

**Rolling Updates:** Helm typically performs rolling updates for Deployments and StatefulSets, minimizing downtime. However, depending on the changes you've made (e.g., significant resource changes, image updates), some pods might be temporarily unavailable.

**Persistent Volumes:** Changes to persistent volume claims (size, storage class) might not be applied automatically and could require manual intervention or recreation of the resources. Be cautious when modifying these sections.

**Configuration Changes:** Changes to the config section under loki will trigger a rolling restart of the Loki pods to apply the new configuration.

**Syntax Errors:** Ensure your values.yaml file is syntactically correct (YAML format). Helm will likely fail to upgrade if there are errors in the file.

### Note:- 
Can Validate if the data is coming or not by visualizing it on grafana . For grafana the end point will be the loki-frontend-query svc.

 Configuring Loki to use S3 for storage in your production environment, you'll benefit from improved durability, scalability, and cost-effectiveness for your logging infrastructure. Remember to secure your S3 bucket with appropriate IAM policies

By following these steps, you can effectively manage and update your Loki logging stack configuration using Helm and the values.yaml file. Remember to always review the changes you make to values.yaml and understand their potential impact on your running system.
