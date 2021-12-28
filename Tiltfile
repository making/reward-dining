SOURCE_IMAGE = os.getenv("SOURCE_IMAGE", default='ghcr.io/making/src')
LOCAL_PATH = os.getenv("LOCAL_PATH", default='.')
NAMESPACE = os.getenv("NAMESPACE", default='default')

k8s_custom_deploy(
    'reward-dining',
    apply_cmd="tanzu apps workload apply -f tap/workload.yaml --live-update" +
               " --local-path " + LOCAL_PATH +
               " --source-image " + SOURCE_IMAGE +
               " --namespace " + NAMESPACE +
               " --yes >/dev/null " +
              "&& kubectl get workload reward-dining --namespace " + NAMESPACE + " -o yaml",
    delete_cmd="tanzu apps workload delete -f config/workload.yaml --namespace " + NAMESPACE + " --yes",
    deps=['pom.xml', './target/classes'],
    image_selector='ghcr.io/making/reward-dining-' + NAMESPACE,
    live_update=[
      sync('./target/classes', '/workspace/BOOT-INF/classes')
    ]
)

k8s_resource('reward-dining', port_forwards=["8080:8080"],
            extra_pod_selectors=[{'serving.knative.dev/service': 'reward-dining'}])
allow_k8s_contexts('leopard-admin@leopard')
