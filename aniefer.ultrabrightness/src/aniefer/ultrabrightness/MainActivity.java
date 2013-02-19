package aniefer.ultrabrightness;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();

		int ultra = getUltraBrightnessMode();
		if (ultra != -1) {
			onUltraBrightnessChanged(ultra == 1 ? false : true);
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
