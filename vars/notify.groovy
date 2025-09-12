def bot_send_message(main_items, STATUS = 'None') {
    helper = ""
    if ( STATUS != 'None' ) {
        helper = " `n`r<b>Failed at step</b> - ${STATUS}"
    }
    powershell """
        \$change = "${main_items.CHANGE}"
        \$shelve = "${main_items.SHELVE}"
        echo \$shelve
        \$config = \$env:VS_CONFIG.Replace('+', '%2B')
        \$emoji = [char]::ConvertFromUtf32(0x274C)
        \$message = "\$emoji\$emoji\$emoji <b>FAILURE</b> \$emoji\$emoji\$emoji `n`r`n`r<b>Type</b> - \$env:JOB_BASE_NAME `n`r<b>Platform</b> - \$env:PLATFORM `n`r<b>Target</b> - \$env:BUILD_TARGET `n`r<b>Configuration</b> - \$config `n`r<b>Branch</b> - \$env:BRANCH `n`r<b>Number</b> - \$env:BUILD_ID`n`r<b>Changelist</b> - \$change `n`r<b>SHELVE</b> - \$shelve${helper}"
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        \$Response = Invoke-RestMethod -Uri "https://api.telegram.org/bot${main_items.BOT_TOKEN}/sendMessage?chat_id=${main_items.CHAT_ID}&text=\$(\${message})&parse_mode=HTML"
    """
}