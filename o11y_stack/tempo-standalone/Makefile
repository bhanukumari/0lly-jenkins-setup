get-helm-chart:
	curl https://github.com/OT-CONTAINER-KIT/helm-charts/releases/download/tempo-standalone-1.0.0/tempo-standalone-1.0.0.tgz -O -J -L
	tar -xf tempo-standalone-1.0.0.tgz

generate-manifest:
	helm template --name-template=tempo tempo-standalone/ -n observability -f values.yaml

apply:
	kubectl create namespace observability --dry-run=client -o yaml | kubectl apply -f -
	helm template --name-template=tempo tempo-standalone/ -n observability -f values.yaml | kubectl apply -f -

clean:
	rm -rf tempo-standalone-1.0.0.tgz tempo-standalone
