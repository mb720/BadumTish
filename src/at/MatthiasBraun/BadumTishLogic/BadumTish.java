package at.MatthiasBraun.BadumTishLogic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import at.MatthiasBraun.R;

public class BadumTish extends Activity {

	private static final String LOGTAG = "Badum-Tish";

	// Permanently stored settings of the app
	public SharedPreferences prefs;

	private Shaker shaker;
	private GUImanager guiMan;
	private JokeManager jokeMan;
	private AchievementManager achievementMan;

	/** Called when the activity loses focus. */
	@Override
	public void onPause() {
		super.onPause();

		// The user leaves the app via the hardware BACK button
		if (this.isFinishing()) {
			userLeavesApp();
		}

		// Deactivate the shaker
		SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.unregisterListener(shaker);
		shaker = null;

		getGUImanager().getSteadyHandCountDown().cancel();
	}

	/** Called when the activity (re)gains focus. */
	@Override
	public void onResume() {
		super.onResume();

		getShaker();
		getGUImanager();
		getJokeManager().displayCurrJoke();

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = getPreferences(MODE_PRIVATE);
		//Debug.startMethodTracing(getLogTag());
		
		setContentView(R.layout.main);

		// Set the volume stream to music so the user can control the volume
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

	}

	/**
	 * Don't rotate the GUI if the device's orientation changes
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	/**
	 * Saves a value permanently on the device.
	 * 
	 * @param key
	 *            The name of the value to be saved.
	 * @param val
	 *            The value to be saved permanently.
	 * @return Whether the value was saved successfully or not
	 */
	public boolean savePermanently(String key, Object val) {
		if (val == null) {
			return false;
		}
		SharedPreferences.Editor editor = prefs.edit();

		if (val.getClass() == Integer.class) {
			editor.putInt(key, (Integer) val);
		} else if (val.getClass() == String.class) {
			editor.putString(key, (String) val);
		} else if (val.getClass() == Boolean.class) {
			editor.putBoolean(key, (Boolean) val);
		} else if (val.getClass() == Float.class) {
			editor.putFloat(key, (Float) val);
		} else if (val.getClass() == Long.class) {
			editor.putLong(key, (Long) val);
		} else {
			Log.w(LOGTAG,
					"Failed to save value of unknown type " + val.getClass());
		}
		return editor.commit();
	}

	/**
	 * Used for inflating the menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);
		return getGUImanager().onCreateOptionsMenu(menu);
	}

	/**
	 * Called when the users selects a menu item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return getGUImanager().onOptionsItemSelected(item);
	}

	/**
	 * Is called when the app returns from another called activity like the
	 * email activity.
	 * 
	 * @param requestCode
	 *            The code used to identify the called activity.
	 * @param resultCode
	 *            The result code of the activity.
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		getGUImanager().onActivityResult(requestCode);
	}

	public void onClick(View v) {
		getGUImanager().onClick(v);
	}

	/**
	 * Called when the user leaves the app via the HOME button
	 */
	@Override
	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		// onUserLeaveHint is sometimes called when the user enters a share
		// activity
		if (!getGUImanager().userIsInShareActivity) {
			userLeavesApp();
		}
	}

	/**
	 * Called when the user leaves the app either through the HOME or the BACK
	 * button.
	 */
	public void userLeavesApp() {
		// Leaving the app unlocks an achievement
		getAchievementManager().unlockAchievement("QUIT");

		// Reset the joke text so it shows "Shake me" on the joke view
		getJokeManager().resetJoke();

		//Debug.stopMethodTracing();

	}

	/**
	 * 
	 * @return An instance of the AchievementManager.
	 */
	public AchievementManager getAchievementManager() {
		if (achievementMan == null)
			achievementMan = new AchievementManager(this);
		return achievementMan;
	}

	/**
	 * 
	 * @return An instance of the JokeManager.
	 */
	public JokeManager getJokeManager() {
		if (jokeMan == null) {
			jokeMan = new JokeManager(this);
		}
		return jokeMan;
	}

	/**
	 * 
	 * @return An instance of the GUImanager.
	 */
	public GUImanager getGUImanager() {
		if (guiMan == null)
			guiMan = new GUImanager(this);
		return guiMan;
	}

	/**
	 * 
	 * @return An instance of the Shaker.
	 */
	public Shaker getShaker() {
		if (shaker == null) {
			shaker = Shaker.getInstance();
			shaker.init((SensorManager) getSystemService(SENSOR_SERVICE));
			shaker.addListener(getGUImanager());
			Log.i(getLogTag(), "Shaker was freshly initialized");
		}
		return shaker;
	}

	/**
	 * 
	 * @return The logtag of the app.
	 */
	public String getLogTag() {
		return LOGTAG;
	}
}