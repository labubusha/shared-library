def call(Map args = [:]) {
    def defaultValues = [STATUS: 'None', PING: 'None']
    def config = defaultValues << args
    
    echo "STATUS: ${config.STATUS}"
    echo "PING: ${config.PING}"
}