private def check_param(parameters, key) {
    if (parameters.containsKey(key) && parameters[key] != "") {
        return true
    } else {
        return false
    }
}

def bot_send_message(Map parameters, result, Bool platform, Bool target, Bool config, Bool branch) {
    if (!(check_param(parameters,"change") && check_param(parameters, "bot_token") && check_param(parameters, "chat_id"))) {
        echo "Error! Missing required parameters — change, bot_token, chat_id. "
        return 
    }
    println result
    def message = [
        resultString: "", helpString: "", 
        emoji: "", resultType: "", ping: "", number: "", 
        steam_branch_string: "", type: "", shelve: "", 
        platform: "", target: "", config: "", branch: ""
    ]

    if ( parameters.containsKey("status") ) {
        switch (result) {
            case 'FAILURE': 
                message.emoji = "[char]::ConvertFromUtf32(0x274C)"
                message.helpString = " `n`r<b>Failed at step</b> - ${parameters.status}"
                message.resultType = "<b>FAILURE</b>"
                break
            case 'SUCCESS': 
                message.emoji = "[char]::ConvertFromUtf32(0x2705)"
                message.resultType =  "<b>SUCCESSFUL</b>"
                break
            case 'ABORTED': 
                message.emoji = "[char]::ConvertFromUtf32(0x2716)"
                message.resultType =  "<b>ABORTED</b>"
                break
            case 'FIXED':
                message.emoji = "[char]::ConvertFromUtf32(0x2705)"
                message.resultType =  "<b>FIXED</b>"
                break
            case 'REGRESSION': 
                message.emoji = "[char]::ConvertFromUtf32(0x274C)"
                message.helpString = " `n`r<b>Failed at step</b> - ${parameters.status}"
                message.resultType = "<b>REGRESSION</b>"
                break
        }    
    }

    if ( parameters.containsKey("type") ) {
        message.type = "`n`r<b>Type</b> - ${parameters.type}"
    }

    if ( platform ) {
        message.platform = " `n`r<b>Platform</b> - \$env:PLATFORM"
    }

    if ( target ) {
        message.target = " `n`r<b>Target</b> - \$env:BUILD_TARGET"
    }

    if ( config ) {
        message.config = " `n`r<b>Configuration</b> - \$env:VS_CONFIG.Replace('+', '%2B')"
    }

    if ( branch ) {
        message.branch = " `n`r<b>Branch</b> - \$env:BRANCH"
    }

<<<<<<< HEAD
=======
    if ( parameters.containsKey("steam_branch_string") ) {
        message.steam_branch_string = "`n`r<b>Steam branch</b> - ${parameters.steam_branch_string}"
    }

    if ( parameters.containsKey("ping") ) {
        message.ping = " `n`r${parameters.ping}"
    }
>>>>>>> 532c337 (add optional param branch)
    if ( parameters.containsKey("number") ) {
        message.number = "<b>Number</b> - ${parameters.number}"
    }

<<<<<<< HEAD
    if ( parameters.containsKey("steam_branch_string") ) {
        message.steam_branch_string = "`n`r<b>Steam branch</b> - ${parameters.steam_branch_string}"
    }

    

=======
>>>>>>> 532c337 (add optional param branch)
    
    
    if ( parameters.containsKey('shelve') ) {
        message.shelve = parameters.shelve
    }
    
<<<<<<< HEAD
    if ( parameters.containsKey("ping") ) {
        message.ping = " `n`r${parameters.ping}"
    }

=======
>>>>>>> 532c337 (add optional param branch)
    message.resultString = "\$emoji\$emoji\$emoji <b>${message.resultType}</b> \$emoji\$emoji\$emoji `n`r${message.type}${message.platform}${message.target}${message.config}${message.branch}${message.steam_branch_string}${message.number}`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve${message.helpString}${message.ping}"
    
    powershell """
        \$change = "${parameters.change}"
        \$shelve = "${message.shelve}"
        echo \$shelve
        \$emoji = ${message.emoji}
        \$message = "${message.resultString}"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${parameters.bot_token}/sendMessage?chat_id=${parameters.chat_id}&text=\$(\${message})&parse_mode=HTML"
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