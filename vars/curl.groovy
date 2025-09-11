def curl_cred(items, logFileName) {
    bat """
        url -m 600 -X POST https://${items.user}:${items.token}@${items.jenkins_url}/job/${items.JOB_NAME}/${items.BUILD_ID}/consoleText > ${logFileName} 2>&1
        exit /b 0
    """
}