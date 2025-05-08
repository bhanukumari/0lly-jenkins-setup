get-helm-chart:
	curl https://github.com/OT-CONTAINER-KIT/helm-charts/releases/download/vm-0.0.3/vm-0.0.3.tgz -O -J -L -s
	tar -xf vm-0.0.3.tgz

generate-manifest:
	helm template --name-template=vm vm -n monitoring -f values.yaml

apply-crd:
	kubectl apply -f https://raw.githubusercontent.com/VictoriaMetrics/helm-charts/refs/tags/victoria-metrics-k8s-stack-0.25.5/charts/victoria-metrics-k8s-stack/charts/crds/crds/crd.yaml
apply:
	kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -
	helm template --name-template=vm vm -n monitoring -f values.yaml | kubectl apply -f -

clean:
	rm -rf vm-0.0.3.tgz vm
