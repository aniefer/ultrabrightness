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
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

	private TextView mTextView;

	private final static String ULTRA_BRIGHTNESS_PROP = "persist.sys.ultrabrightness";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.text_view);
	}

	@Override
	protected void onStart() {
		super.onStart();

		String oldUltra = GET(getBaseContext(), ULTRA_BRIGHTNESS_PROP);
		if (oldUltra != null) {
			if (oldUltra.equals("0")) {
				setBrightnessLevel(100);
				onUltraBrightnessChanged(true);
				mTextView.append("Brightness set to 100%.");
			} else {
				setBrightnessLevel(20);
				onUltraBrightnessChanged(false);
				mTextView.append("Brightness set to 20%.");
			}
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		finish();
	}
	
	private void setBrightnessLevel(int level) {
		float floatLevel = level / 100.0f;
		Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) floatLevel * 255);

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = floatLevel;
		getWindow().setAttributes(lp);
	}

	private boolean writeOneLine(String fileName, String value) {
		try {
			FileWriter localFileWriter = new FileWriter(fileName);
			try {
				localFileWriter.write(value);
				return true;
			} finally {
				localFileWriter.close();
			}
		} catch (IOException localIOException) {
		}
		return false;
	}

	private void onUltraBrightnessChanged(boolean newValue) {
		String file = "/sys/devices/i2c-0/0-0036/mode";
		File localFile = new File(file);
		if ((localFile.isFile()) && (localFile.canRead())) {
			String value = newValue ? "i2c_pwm" : "i2c_pwm_als";
			if (writeOneLine(file, value)) {
				SET(getBaseContext(), ULTRA_BRIGHTNESS_PROP, newValue ? "1" : "0");
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private String GET(Context context, String key) throws IllegalArgumentException {
		try {
			ClassLoader cl = context.getClassLoader();
			Class SystemProperties = cl.loadClass("android.os.SystemProperties");
			Method getMethod = SystemProperties.getMethod("get", String.class);
			return (String) getMethod.invoke(SystemProperties, key);
		} catch (Exception e) {
		}

		return null;
	}

	private void SET(Context context, String key, String value) throws IllegalArgumentException {
		try {
			String arg = "setprop " + key + " " + value;
			/* don't bother waiting for this */
			Runtime.getRuntime().exec(new String[] {"/system/xbin/su", "-c", arg});
		} catch (IOException e) {
		}
	}
}
