## Documentation for  Logging Stack

### Table of content :-
* [Introduction](#Introduction)
* [ Prerequisites](#Prerequisites)
* [Required Resources](#Required_Resources)
* [Data Flow](#Data_Flow)
* [Redeploying ](#Redeploying )
* [Important Considerations During Redeployment](#Important_Considerations_During_Redeployment)

### Introduction
This document outlines the architecture, prerequisites, resource requirements, data flow, and redeployment process for the Loki logging stack configured using the provided values.yaml.The aim of this Loki logging stack is to collect, aggregate, store, and provide querying capabilities for logs within the Kubernetes cluster

###  Prerequisites
Before deploying this Loki stack, ensure the following prerequisites are met:

**Kubernetes Cluster:**  A running Kubernetes cluster .

**Helm:** Helm package manager installed and configured to interact with your Kubernetes cluster.

**Storage Provisioner:**  A default StorageClass configured in your cluster like in our case  buildpiper-storage .

###  Required Resources
This Loki stack deploys several components, each with its own resource requirements defined in the values.yaml. Here's a breakdown:

**Logging Gateway :**

Autoscaling: Enabled, with a minimum of 2 and a maximum of 10 replicas.
Resources (per pod):
Requests: 500Mi memory, 0.3 CPU
Limits: 700Mi memory, 0.5 CPU

**Loki:**
# Loki Component Resource Configuration

| Component         | Memory Request | CPU Request | Memory Limit | CPU Limit |
|------------------|----------------|-------------|---------------|------------|
| Ingester          | 400Mi        | 0.3         | 700Mi         | 0.5        |
| Distributor       | 500Mi        | 0.3         | 700Mi         | 0.5        |
| Querier           | 300Mi        | 0.2         | 600Mi         | 0.4       |
| Query Frontend    | 500Mi        | 0.3         | 700Mi         | 0.5        |
| Index Gateway     | 400Mi        | 0.3         | 600Mi         | 0.5        |
| Memcached (All)   |   600Mi      | 0.3           |    800Mi     | 0.5        |


**Important Considerations:**

The resource requests and limits should be adjusted based on the actual log volume and query load in your environment.

Ensure your Kubernetes nodes have sufficient resources to accommodate these deployments.

The persistent volume sizes for Ingesters, Memcached, and Index Gateway should be determined based on your retention policies and expected data growth.

### Data Flow
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

### Redeploying 
To redeploy your Loki stack after making changes to the values.yaml file, you will typically use the Helm command-line interface. Assuming you initially deployed Loki using a Helm release named logging .
Navigate to the directory containing your updated values.yaml file.

Run the Helm upgrade command:
```
helm upgrade logging . -f values.yaml -n logging
```

The . in the command refers to the current directory where your Helm chart (if you have one locally) is located. If you deployed from a remote chart repository, you would specify the chart name and repository again 

Monitor the rollout: After running the upgrade command, Helm will apply the changes defined in your updated values.yaml to your Kubernetes cluster. You can monitor the status of the deployments and stateful sets using kubectl:


### Important Considerations During Redeployment

**Rolling Updates:** Helm typically performs rolling updates for Deployments and StatefulSets, minimizing downtime. However, depending on the changes you've made (e.g., significant resource changes, image updates), some pods might be temporarily unavailable.

**Persistent Volumes:** Changes to persistent volume claims (size, storage class) might not be applied automatically and could require manual intervention or recreation of the resources. Be cautious when modifying these sections.

**Configuration Changes:** Changes to the config section under loki will trigger a rolling restart of the Loki pods to apply the new configuration.

**Syntax Errors:** Ensure your values.yaml file is syntactically correct (YAML format). Helm will likely fail to upgrade if there are errors in the file.

### Note:- 
 Configuring Loki to use S3 for storage in your production environment, you'll benefit from improved durability, scalability, and cost-effectiveness for your logging infrastructure. Remember to secure your S3 bucket with appropriate IAM policies

By following these steps, you can effectively manage and update your Loki logging stack configuration using Helm and the values.yaml file. Remember to always review the changes you make to values.yaml and understand their potential impact on your running system.
