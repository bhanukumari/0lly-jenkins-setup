def call(Map config = [:]) {
    def repoUrl = config.repoUrl ?: error("repoUrl not provided")
    def branch = config.branch ?: "main"

    echo "Cloning ${repoUrl} on branch ${branch}..."
    checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${branch}"]],
        userRemoteConfigs: [[url: repoUrl]]
    ])
}

