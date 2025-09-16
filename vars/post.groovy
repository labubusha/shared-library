def test(Map args = [:], main_items) {
    defaultValues = [STATUS: 'None', PING: 'None']
    args = defaultValues << args
    return "${STATUS} ${PING}"
}