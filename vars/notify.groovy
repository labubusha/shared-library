def bot_send_message(main_items, parameters) {
    def resultString = ""
    def helpString = ""
    def emoji = ""
    def ResultType =  ""
    def ping = ""
    def currentBuild = currentBuild.currentResult
    if ( parameters.PING != 'None' ) {
        ping = " `n`r${parameters.PING}"
    }
    if ( parameters.STATUS != 'None' ) {
        switch (currentBuild) {
            case 'FAILURE': 
                emoji = "[char]::ConvertFromUtf32(0x274C)"
                helpString = " `n`r<b>Failed at step</b> - ${parameters.STATUS}"
                ResultType = "<b>FAILURE</b>"
                break
            case 'SUCCESS': 
                emoji = "[char]::ConvertFromUtf32(0x2705)"
                ResultType =  "<b>SUCCESSFUL</b>"
                break
            case 'ABORTED': 
                emoji = "[char]::ConvertFromUtf32(0x2716)"
                ResultType =  "<b>ABORTED</b>"
                break
            case 'FIXED':
                emoji = "[char]::ConvertFromUtf32(0x2705)"
                ResultType =  "<b>FIXED</b>"
                break
            case 'REGRESSION': 
                emoji = "[char]::ConvertFromUtf32(0x274C)"
                helpString = " `n`r<b>Failed at step</b> - ${parameters.STATUS}"
                ResultType = "<b>REGRESSION</b>"
                break
        }    
    }
    resultString = "\$emoji\$emoji\$emoji <b>${ResultType}</b> \$emoji\$emoji\$emoji `n`r`n`r<b>Type</b> - ${parameters.TYPE} `n`r<b>Platform</b> - \$env:PLATFORM `n`r<b>Target</b> - \$env:BUILD_TARGET `n`r<b>Configuration</b> - \$config `n`r<b>Branch</b> - \$env:BRANCH`n`r${parameters.STEAM_BRANCH_STRING}<b>Number</b> - ${parameters.NUMBER}`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve${helpString}${ping}"
    powershell """
        \$change = "${main_items.CHANGE}"
        \$shelve = "${main_items.SHELVE}"
        echo \$shelve
        \$config = \$env:VS_CONFIG.Replace('+', '%2B')
        \$emoji = ${emoji}
        \$message = "${resultString}"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${main_items.BOT_TOKEN}/sendMessage?chat_id=${main_items.CHAT_ID}&text=\$(\${message})&parse_mode=HTML"
    """           
}

def send_log(main_items, logFileName) {
    def fileSizeInBytes = powershell(returnStdout: true, script: "(Get-Item '${logFileName}').Length")
    def fileSize = fileSizeInBytes.toInteger()
    def fileSizeInMB = fileSize / (1024 * 1024)
    if (fileSizeInMB < 50)
    {
        bat """
            curl -X POST "https://api.telegram.org/bot${main_items.BOT_TOKEN}/sendDocument" -F chat_id=${main_items.CHAT_ID} -F document="@${logFileName}"
        """
    }
    else
    {
        bat """
            "C:\\Program Files\\7-Zip\\7z.exe" a -t7z ${logFileName}.7z ${logFileName}
            curl -X POST "https://api.telegram.org/bot${main_items.BOT_TOKEN}/sendDocument" -F chat_id=${main_items.CHAT_ID} -F document="@${logFileName}.7z"
        """
    }
}

def download_log(curl_items, logFileName) {
    bat """
        url -m 600 -X POST https://${curl_items.user}:${curl_items.token}@${curl_items.jenkins_url}/job/${curl_items.JOB_NAME}/${curl_items.BUILD_ID}/consoleText > ${logFileName} 2>&1
        exit /b 0
    """
}

def send_error_message(main_items, htmlMessage) {
    powershell """
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$botToken = "${main_items.BOT_TOKEN}"
        \$chatId = "${main_items.CHAT_ID}"
        \$encodedMessage = [uri]::EscapeDataString('${htmlMessage}')
        \$uri = "https://api.telegram.org/bot\$botToken/sendMessage?chat_id=\$chatId&text=\$encodedMessage&parse_mode=HTML"
        \$Response = Invoke-RestMethod -Uri \$uri -Method Get
        Start-Sleep -Seconds 1
    """
}