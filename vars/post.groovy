def call(Map args = [:], main_items) {
    def defaultValues = [STATUS: 'None', PING: 'None']
    def config = defaultValues << args
    
    echo "STATUS: ${config.STATUS}"
    echo "PING: ${config.PING}"
}