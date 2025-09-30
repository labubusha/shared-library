private def check_bot_items(parameters) {
    if (parameters.containsKey("bot_token") && parameters.containsKey("chat_id")) {
        return true
    } else {
        return false
    }
}

private def check_item(params) {
    if (params.containsKey("user") && params.containsKey("token") && params.containsKey("jenkins_url") && params.containsKey("job_name") && params.containsKey("build_id")) {
        return true
    } else {
        return false
    }
}

def bot_send_message(Map parameters, result) {
    if (!(check_bot_items(parameters))) {
        echo "Error! Missing required parameters — bot_token, chat_id."
        return 
    }
    def message = [
        resultString: "", helpString: "", 
        emoji: "", resultType: "", ping: "", number: "", 
        steam_branch_string: "", type: "", shelve: "", 
        platform: "", target: "", config: "", branch: "",
        change: "", map: "", revisionRange: ""
    ]

    switch (result) {
        case 'FAILURE': 
            message.emoji = "[char]::ConvertFromUtf32(0x274C)"
            message.resultType = "<b>FAILURE</b>"
            if (parameters.containsKey("status")) {
                message.helpString = " `n`r<b>Failed at step</b> - ${parameters.status}"
            }
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
            message.resultType = "<b>REGRESSION</b>"
            if (parameters.containsKey("status")) {
                message.helpString = " `n`r<b>Failed at step</b> - ${parameters.status}"
            }
            break
        case 'PARTIAL SUCCESS':
            message.emoji = "[char]::ConvertFromUtf32(0x26A0)"
            message.resultType = "<b>PARTIAL SUCCESS</b>"
            break
    }

    if ( parameters.containsKey("type") ) {
        message.type = "`n`r<b>Type</b> - ${parameters.type}"
    }

    if ( parameters.containsKey("platform") ) {
        message.platform = " `n`r<b>Platform</b> - ${parameters.platform}"
    }

    if ( parameters.containsKey("target") ) {
        message.target = " `n`r<b>Target</b> - ${parameters.target}"
    }

    if ( parameters.containsKey("config") ) {
        message.config = " `n`r<b>Configuration</b> - ${parameters.config.replace("+", "%2B")}"
    }

    if ( parameters.containsKey("branch") ) {
        message.branch = " `n`r<b>Branch</b> - ${parameters.branch}"
    }

    if ( parameters.containsKey("number") ) {
        message.number = " `n`r<b>Number</b> - ${parameters.number}"
    }

    if ( parameters.containsKey("change") ) {
        message.change = "`n`r<b>Changelist</b> - ${parameters.change}"
    }

    if ( parameters.containsKey("steam_branch_string") ) {
        message.steam_branch_string = "`n`r<b>Steam branch</b> - ${parameters.steam_branch_string}"
    }
    
    if ( parameters.containsKey('shelve') ) {
        message.shelve = " `n`r<b>SHELVE</b> - ${parameters.shelve}"
    }
    
    if ( parameters.containsKey("ping") ) {
        message.ping = " `n`r@${parameters.ping}"
    }

    if ( parameters.containsKey("map") ) {
        message.map = "`n`r<b>Maps</b> - ${parameters.map} `n`r"
    }

    if ( parameters.containsKey("revisionRange") ) {
        message.revisionRange = "`n`rCrash between CL${parameters.revisionRange[0]} and CL${parameters.revisionRange[1]}"
    }

    message.resultString = "\$emoji\$emoji\$emoji ${message.resultType} \$emoji\$emoji\$emoji `n`r${message.type}${message.map}${message.platform}${message.target}${message.config}${message.branch}${message.steam_branch_string}${message.number}${message.change}${message.revisionRange}${message.shelve}${message.helpString}${message.ping}"

    powershell """
        \$change = "${parameters.change}"
        \$emoji = ${message.emoji}
        \$message = "${message.resultString}"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${parameters.bot_token}/sendMessage?chat_id=${parameters.chat_id}&text=\$(\${message})&parse_mode=HTML"
    """   
            
}

private def get_7z_filename(logFileName) {
    if (logFileName.contains(".txt")) {
        return logFileName.replace(".txt","")
    }
    if (logFileName.contains(".log")) {
        return logFileName.replace(".log","")
    }
}

private def send_log_bat(main_items, logFileName, Boolean get7z = false) {
    if (!get7z) {
        bat """
            curl -X POST "https://api.telegram.org/bot${main_items.bot_token}/sendDocument" -F chat_id=${main_items.chat_id} -F document="@${logFileName}"
        """
    } else {
        def newFileName = get_7z_filename(logFileName)
        bat """
            "C:\\Program Files\\7-Zip\\7z.exe" a -t7z ${newFileName}.7z ${logFileName}
            curl -X POST "https://api.telegram.org/bot${main_items.bot_token}/sendDocument" -F chat_id=${main_items.chat_id} -F document="@${newFileName}.7z"
        """
    }
    
}

def send_log(main_items, logFileName, Boolean checkFileSize = false) {
    if (!check_bot_items(main_items)) {
        echo "Error! Missing required parameters — bot_token, chat_id."
        return 
    }
    if (checkFileSize) {
        def fileSizeInBytes = powershell(returnStdout: true, script: "(Get-Item '${logFileName}').Length")
        def fileSize = fileSizeInBytes.toInteger()
        def fileSizeInMB = fileSize / (1024 * 1024)
        if (fileSizeInMB < 50)
        {
            send_log_bat(main_items, logFileName)
        } else {
            send_log_bat(main_items, logFileName, true)
        }
    } else {
        send_log_bat(main_items, logFileName)
    }
    
}

def download_log(main_items, logFileName) {
    if (!(check_item(main_items))) {
        echo "Error! Missing required parameters — user, token, jenkins_url, job_name, build_id."
        return 
    }
    bat """
        curl -m 600 -X POST https://${main_items.user}:${main_items.token}@${main_items.jenkins_url}/job/${main_items.job_name}/${main_items.build_id}/consoleText > ${logFileName} 2>&1
        exit /b 0
    """
}

def send_error_message(main_items, htmlMessage) {
    if (!check_bot_items(main_items)) {
        echo "Error! Missing required parameters — bot_token, chat_id."
        return 
    }
    println htmlMessage
    powershell """
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$botToken = "${main_items.bot_token}"
        \$chatId = "${main_items.chat_id}"
        \$encodedMessage = [uri]::EscapeDataString('${htmlMessage}')
        \$uri = "https://api.telegram.org/bot\$botToken/sendMessage?chat_id=\$chatId&text=\$encodedMessage&parse_mode=HTML"
        Write-Host \$uri
        \$Response = Invoke-RestMethod -Uri \$uri -Method Get
        Start-Sleep -Seconds 1
    """
}