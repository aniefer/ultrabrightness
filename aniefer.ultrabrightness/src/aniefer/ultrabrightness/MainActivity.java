/*******************************************************************************
 * Copyright (c) 2013 Andrew Niefer. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package aniefer.ultrabrightness;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.text_view);
	}

	@Override
	protected void onStart() {
		super.onStart();

		int oldUltra = getUltraBrightnessMode();
		if (oldUltra != -1) {
			setBrightnessLevel(oldUltra == 1 ? 20 : 100);
			onUltraBrightnessChanged(oldUltra == 1 ? false : true);
		}

	}

	private void log(String msg) {
		mTextView.append(msg);
	}

	private int getUltraBrightnessMode() {
		try {
			log("checking screen_brightness_ultra_mode: ");
			int i = Settings.System.getInt(getBaseContext().getContentResolver(), "screen_brightness_ultra_mode");
			log(String.valueOf(i) + "\n");
			return i;
		} catch (Settings.SettingNotFoundException localSettingNotFoundException) {
			log(localSettingNotFoundException.getMessage());
		}
		return -1;
	}

	private void setUltraMode(int paramInt) {
		log("Setting ultramode to " + paramInt);
		Settings.System.putInt(getBaseContext().getContentResolver(), "screen_brightness_ultra_mode", paramInt);
	}

	private void setBrightnessLevel(int level) {
		float floatLevel = level / 100.0f;

		log("Setting brightness float " + floatLevel + "\n");
		log("Setting brightness 255: " + floatLevel * 255 + "\n");
		Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) floatLevel * 255);

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = floatLevel;
		getWindow().setAttributes(lp);
	}

	private boolean writeOneLine(String paramString1, String paramString2) {
		try {
			FileWriter localFileWriter = new FileWriter(paramString1);
			try {
				localFileWriter.write(paramString2);
				return true;
			} finally {
				localFileWriter.close();
			}
		} catch (IOException localIOException) {
			log("Error writing to " + paramString1 + ". Exception: \n" + localIOException.getMessage());
		}
		return false;
	}

	private void onUltraBrightnessChanged(boolean paramBoolean) {
		int i = paramBoolean ? 1 : 0;
		String file = "/sys/devices/i2c-0/0-0036/mode";
		String value;

		Log.d("BrightnessPreference", "ultrabrightess set to " + paramBoolean);
		File localFile = new File(file);
		if ((localFile.isFile()) && (localFile.canRead())) {
			value = paramBoolean ? "i2c_pwm" : "i2c_pwm_als";
			log("Writing " + file + " " + value + "\n");
			if (writeOneLine(file, value)) {
				setUltraMode(i);
			}
		} else {
			log("Failed to read " + file + "\n");
		}
	}

}
