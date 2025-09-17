def call(Map args = [:]) {
    def defaultValues = [STATUS: 'None', PING: 'None', NUMBER: '\$env:BUILD_ID', STEAM_BRANCH_STRING: '', TYPE: '\$env:JOB_BASE_NAME']
    def config = defaultValues << args
    return config
}