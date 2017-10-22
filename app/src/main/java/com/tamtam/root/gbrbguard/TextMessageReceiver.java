package com.tamtam.root.gbrbguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by root on 10/22/17.
 */

public class TextMessageReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Bundle bundle=intent.getExtras();

        /*Object[] messages=(Object[])bundle.get("pdus");
        SmsMessage[] sms=new SmsMessage[messages.length];



        for(int n=0;n<messages.length;n++){
            sms[n]= SmsMessage.createFromPdu((byte[]) messages[n]);
        }

        for(SmsMessage msg:sms) {
            FullscreenActivity.updateMessageText(msg.getMessageBody(),msg.getDisplayOriginatingAddress());
        }*/
    }
}
