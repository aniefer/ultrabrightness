/*******************************************************************************
 * Copyright (c) 2013 Andrew Niefer. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package aniefer.ultrabrightness;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.TextView;

public class TextActivity extends Activity {

	private TextView mTextView;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_text);
		mTextView = (TextView) findViewById(R.id.text_view);
		
		// call sms function
		try {
			sendSmsMessage("8005554321", "Message Here.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendSmsMessage(String address, String message) throws Exception {

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";
		
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (getResultCode() == Activity.RESULT_OK) {
					mTextView.setText("Message Sent");
				} else {
					mTextView.setText("Problem sending message (" + getResultCode() + ")");
				}
			}
		}, new IntentFilter(SENT));

		PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);
		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				if (getResultCode() == Activity.RESULT_OK) {
					mTextView.setText("Message delivered");
					finish();
				} else {
					mTextView.setText("Problem sending message (" + getResultCode() + ")");
				}
			}
		}, new IntentFilter(DELIVERED));
		
		SmsManager smsMgr = SmsManager.getDefault();
		smsMgr.sendTextMessage(address, null, message, sentPI, deliverPI);
	}
}
