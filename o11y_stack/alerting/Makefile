alert-apply:
	kubectl apply -f rules/alerts/kube/infra/records
	kubectl apply -f rules/alerts/kube/infra/pods
	kubectl apply -f rules/alerts/kube/infra/nodes
	kubectl apply -f rules/alerts/kube/infra/monitor
	kubectl apply -f rules/alerts/kube/infra/master
	kubectl apply -f rules/alerts/kube/infra/custom
	kubectl apply -f rules/alerts/kube/infra/cluster
	kubectl apply -f alertmanager

alert-delete:
	kubectl delete -f rules/alerts/kube/infra/records
	kubectl delete -f rules/alerts/kube/infra/pods
	kubectl delete -f rules/alerts/kube/infra/nodes
	kubectl delete -f rules/alerts/kube/infra/monitor
	kubectl delete -f rules/alerts/kube/infra/master
	kubectl delete -f rules/alerts/kube/infra/custom
	kubectl delete -f rules/alerts/kube/infra/cluster
