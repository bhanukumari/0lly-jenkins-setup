get-helm-chart:
	curl https://github.com/OT-CONTAINER-KIT/helm-charts/releases/download/otel-operator-1.0.0/otel-operator-1.0.0.tgz -O -J -L
	tar -xf otel-operator-1.0.0.tgz

generate-manifest:
	helm template --name-template=otel otel-operator/ -n observability -f values.yaml

# After applying the helm we need to restart the otel Operator pod
apply-helm:
	kubectl create namespace observability --dry-run=client -o yaml | kubectl apply -f -
	helm template --name-template=otel otel-operator/ -n observability -f values.yaml | kubectl apply -f -

apply-otel:
	kubectl apply -f instrumentation
	kubectl apply -f otel-collector

clean:
	rm -rf otel-operator otel-operator-1.0.0.tgz
