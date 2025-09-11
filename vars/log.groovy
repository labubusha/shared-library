def info(message) {
    echo "INFO: ${message}"
}

def aborted(CHANGE, SHELVE, BOT_TOKEN, CHAT_ID) {
    echo 'aborted!'
    powershell """
        \$change = "${CHANGE}"
        \$shelve = "${SHELVE}"
        echo \$shelve
        \$config = \$env:VS_CONFIG.Replace('+', '%2B')
        \$emoji = [char]::ConvertFromUtf32(0x2716)
        \$message = "\$emoji\$emoji\$emoji <b>ABORTED</b> \$emoji\$emoji\$emoji `n`r`n`r<b>Type</b> - \$env:JOB_BASE_NAME `n`r<b>Platform</b> - \$env:PLATFORM `n`r<b>Target</b> - \$env:BUILD_TARGET `n`r<b>Configuration</b> - \$config `n`r<b>Branch</b> - \$env:BRANCH `n`r<b>Number</b> - \$env:BUILD_ID`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${BOT_TOKEN}/sendMessage?chat_id=${CHAT_ID}&text=\$(\$message)&parse_mode=HTML"
    """
}