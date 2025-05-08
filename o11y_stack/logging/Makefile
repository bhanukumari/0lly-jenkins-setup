get-helm-chart:
	curl https://github.com/OT-CONTAINER-KIT/helm-charts/releases/download/loki-1.0.1/loki-1.0.1.tgz -O -J -L
	tar -xf loki-1.0.1.tgz

generate-manifest:
	helm template --name-template=logging loki/ -n logging -f values.yaml

apply:
	kubectl create namespace logging --dry-run=client -o yaml | kubectl apply -f -
	helm template --name-template=logging loki/ -n logging -f values.yaml | kubectl apply -f -

clean:
	rm -rf loki-1.0.1.tgz loki
