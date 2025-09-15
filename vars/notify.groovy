def bot_send_message(main_items, STATUS = 'None', PING = 'None', NUMBER = '\$env:BUILD_ID', STEAM_BRANCH_STRING = '') {
    // main_items, STATUS = 'None', PING = 'None', NUMBER = 'None', STEAM_BRANCH_STRING = 'None'
    // def main_items = args.get()
    def helpString = ""
    def emoji = "[char]::ConvertFromUtf32(0x2716)"
    def resultString =  "<b>ABORTED</b>"
    def type = "\$env:JOB_BASE_NAME"

    if ( STATUS != 'None' ) {
        emoji = "[char]::ConvertFromUtf32(0x274C)"
        helpString = " `n`r<b>Failed at step</b> - ${STATUS}"
        resultString = "<b>FAILURE</b>"
    } else if ( PING != 'None' ) {
         resultString = "<b>FAILURE</b>"
         emoji = "[char]::ConvertFromUtf32(0x274C)"
         helpString = " `n`r${PING}"
         steamBranchString = "${STEAM_BRANCH_STRING}"
    } else if ( NUMBER != '\$env:BUILD_ID') {
        emoji = "[char]::ConvertFromUtf32(0x2705)"
        resultString =  "<b>SUCCESSFUL</b>"
        steamBranchString = "${STEAM_BRANCH_STRING}"
        type = "fullBuild"
    }

    powershell """
        \$change = "${main_items.CHANGE}"
        \$shelve = "${main_items.SHELVE}"
        \$steambranch = "${STEAM_BRANCH_STRING}"
        echo \$shelve
        \$config = \$env:VS_CONFIG.Replace('+', '%2B')
        \$emoji = ${emoji}
        \$message = "\$emoji\$emoji\$emoji ${resultString} \$emoji\$emoji\$emoji `n`r`n`r<b>Type</b> - ${type} `n`r<b>Platform</b> - \$env:PLATFORM `n`r<b>Target</b> - \$env:BUILD_TARGET `n`r<b>Configuration</b> - \$config `n`r<b>Branch</b> - \$env:BRANCH `n`r\$steambranch<b>Number</b> - ${NUMBER}`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve${helpString}"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${main_items.BOT_TOKEN}/sendMessage?chat_id=${main_items.CHAT_ID}&text=\$(\${message})&parse_mode=HTML"
    """

    // powershell """    
    // echo \$shelve
    // \$config = \$env:VS_CONFIG.Replace('+', '%2B')
    // \$emoji = [char]::ConvertFromUtf32(0x2705)
    // \$message = "\$emoji\$emoji\$emoji <b>SUCCESSFUL</b> \$emoji\$emoji\$emoji `n`r`n`r<b>Type</b> - fullBuild `n`r<b>Platform</b> - \$env:PLATFORM `n`r<b>Target</b> - \$env:BUILD_TARGET `n`r<b>Configuration</b> - \$config `n`r<b>Branch</b> - \$env:BRANCH`n`r\$steambranch<b>Number</b> - \$number`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve"
    // [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    // \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage?chat_id=${CHAT_ID}&text=\$(\${message})&parse_mode=HTML"
    // """
    
    // powershell """
    // \$ping = "${PING}"
    // \$steambranch = "${STEAM_BRANCH_STRING}"
    // echo \$shelve
    // \$config = \$env:VS_CONFIG.Replace('+', '%2B')
    // \$emoji = [char]::ConvertFromUtf32(0x274C)
    // \$message = "\$emoji\$emoji\$emoji <b>FAILURE</b> \$emoji\$emoji\$emoji `n`r`n`r<b>Type</b> - \$env:JOB_BASE_NAME `n`r<b>Platform</b> - \$env:PLATFORM `n`r<b>Target</b> - \$env:BUILD_TARGET `n`r<b>Configuration</b> - \$config `n`r<b>Branch</b> - \$env:BRANCH `n`r\$steambranch<b>Number</b> - \$env:BUILD_ID`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve `n`r\$ping"
    // [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    // \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage?chat_id=${CHAT_ID}&text=\$(\${message})&parse_mode=HTML"
    // """
           
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