def call(Map args = [:]) {
    def defaultValues = [STATUS: 'None', PING: 'None', NUMBER: '\$env:BUILD_ID', STEAM_BRANCH_STRING: '']
    def config = defaultValues << args
    
    return config.STATUS, config.PING, config.NUMBER, config.STEAM_BRANCH_STRING
}