package com.twac.smstest;

import android.os.Bundle;  
import android.app.Activity;  
import android.app.PendingIntent;  
import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.content.IntentFilter;  
import android.telephony.SmsManager;  
import android.telephony.SmsMessage;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.widget.Button;  
import android.widget.EditText;  
import android.widget.TextView;  
import android.widget.Toast;  

public class MainActivity extends Activity {
	private TextView sender, content;
	private EditText input, to;
	private Button send;
	
	private IntentFilter receiveFilter,sendFilter;
	private MessageReceiver messageReceiver;
	private SendStatusReceiver sendStatusReceiver;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sender = (TextView) findViewById(R.id.sender);
		content = (TextView) findViewById(R.id.content);
		input = (EditText) findViewById(R.id.input);
		to = (EditText) findViewById(R.id.to);
		send = (Button) findViewById(R.id.send);

		sendFilter=new IntentFilter();
		sendFilter.addAction("SENT_SMS_ACTION");
		sendStatusReceiver=new SendStatusReceiver();
		registerReceiver(sendStatusReceiver, sendFilter);
		
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				SmsManager smsManager = SmsManager.getDefault();
				Intent sendIntent=new Intent("SENT_SMS_ACTION");
				PendingIntent pi =PendingIntent.getBroadcast(MainActivity.this, 0, sendIntent, 0);
				
				smsManager.sendTextMessage(to.getText().toString(), null, input
						.getText().toString(), pi, null);
			}
		});

		receiveFilter = new IntentFilter();
		receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		messageReceiver = new MessageReceiver();
		registerReceiver(messageReceiver, receiveFilter);
        
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
		unregisterReceiver(messageReceiver);
		unregisterReceiver(sendStatusReceiver);
	}

	class MessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] messages = new SmsMessage[pdus.length];
			for (int i = 0; i < messages.length; i++) {
				messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				String address = messages[0].getOriginatingAddress();
				String fullMessage = "";
				for (SmsMessage message : messages) {
					fullMessage += message.getMessageBody();
				}
				sender.setText(address);
				content.setText(fullMessage);
			}
		}
	}
	class SendStatusReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context conetxt, Intent intent) {
			if (getResultCode()==RESULT_OK) {
				Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(MainActivity.this, "发送失败，请重新操作", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
