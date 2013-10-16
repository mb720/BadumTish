/**
 * 
 */
package at.MatthiasBraun.BadumTishLogic;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import at.MatthiasBraun.R;
import at.MatthiasBraun.BadumTishData.Achievement;

/**
 * @author Matthias Braun
 * 
 */
public class AchievementManager {

	private static final String XML_ACHIEVEMENT_TAG = "achievement";
	public static final String PREF_ACHIEVEMENT = "achievement_done_nr";
	private static final int ACHIEVEMENT_TOAST_DURATION = 5000;
	private LinkedHashMap<String, Achievement> allPossibleAchievements;
	private BadumTish mainActivity;
	// The media player used for playing the achievement unlocked sound
	private MediaPlayer mediaPlayer;

	public AchievementManager(BadumTish mainActivity) {
		this.mainActivity = mainActivity;

		// Prepare the achievement unlocked sound
		mediaPlayer = MediaPlayer.create(mainActivity,
				R.raw.achievement_unlocked);

		try {
			loadAchievementsFromFile();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		loadPermAchData();

	}

	/**
	 * Load the permanent data about the achievements
	 */
	private void loadPermAchData() {

		// For every achievement set if it was unlocked or not
		for (String key : allPossibleAchievements.keySet()) {
			allPossibleAchievements.get(key).setUnlocked(mainActivity.prefs
					.getBoolean(PREF_ACHIEVEMENT + key, false));
		}

//		Log.i(mainActivity.getLogTag(), "Nr of achievements: "
//				+ allPossibleAchievements.size());
//		for (Achievement ach : allPossibleAchievements.values()) {
//			Log.i(mainActivity.getLogTag(), ach.toString());
//		}

	}

	/**
	 * Get the progress of the player regarding the achievement points
	 * 
	 * @return
	 */
	public int getAchievementProgress() {
		float achievedPoints = 0.0f;
		float totalPoints = 0.0f;

		for (Achievement ach : allPossibleAchievements.values()) {
			totalPoints += ach.getPoints();
			if (ach.isUnlocked()) {
				achievedPoints += ach.getPoints();
			}
		}

		return Math.round(achievedPoints / totalPoints * 100);
	}

	/**
	 * Parse the jokes from an XML file.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void loadAchievementsFromFile() throws XmlPullParserException,
			IOException {

		allPossibleAchievements = new LinkedHashMap<String, Achievement>();
		String key;
		XmlResourceParser xrp = mainActivity.getResources().getXml(
				R.xml.achievements);
		while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
			Achievement ach = new Achievement();
			if (xrp.getEventType() == XmlResourceParser.START_TAG) {

				if (xrp.getName().equals(XML_ACHIEVEMENT_TAG)) {

					key = xrp.getAttributeValue(null, "key");
					ach.setName(xrp.getAttributeValue(null, "name"));
					ach.setDescription(xrp.getAttributeValue(null, "desc"));
					ach.setPoints(xrp.getAttributeIntValue(null, "pts", -1));

					// Add the achievement to the set
					allPossibleAchievements.put(key, ach);
				}

			}
			xrp.next();
		}
		xrp.close();
	}

	public HashMap<String, Achievement> getAllPossibleAchievements() {
		return allPossibleAchievements;
	}

	/**
	 * This method is called when the user unlocks an achievement
	 * 
	 * @param key
	 *            The key of the achievement.
	 */
	public void unlockAchievement(String key) {
		Achievement unlockedAch = allPossibleAchievements.get(key);

		if (unlockedAch == null) {
			Log.w(mainActivity.getLogTag(),
					"Warning: Could not find achievement with key " + key);
			return;
		}
		// Do nothing if it was already unlocked
		if (unlockedAch.isUnlocked())
			return;
		// Save that this achievement was unlocked and show a message
		else {

			unlockedAch.setUnlocked(true);
			mainActivity.savePermanently(AchievementManager.PREF_ACHIEVEMENT
					+ key, true);

		}
		// Show a message with the achievement name to the user
		showAchievementToast(unlockedAch.getName(), ACHIEVEMENT_TOAST_DURATION);

		// Play the achievement unlocked sound
		mediaPlayer.start();

	}

	/**
	 * Show a custom toast as defined in drawable/achievement_toast.xml with a
	 * specified duration.
	 * 
	 * @param msg
	 *            The message that will be shown.
	 * @param duration
	 *            The duration of the toast in milliseconds.
	 */
	private void showAchievementToast(String achievementName, int duration) {

		LayoutInflater inflater = mainActivity.getLayoutInflater();
		View layout = inflater.inflate(R.drawable.achievement_toast, null);

		TextView text = (TextView) layout
				.findViewById(R.id.achievement_toast_text);
		text.setText(Html.fromHtml("<small>"
				+ mainActivity.getString(+R.string.achievement_unlocked)
				+ "</small><br/> <b>" + achievementName + "</b>"));

		final Toast t = new Toast(mainActivity);
		t.setDuration(Toast.LENGTH_LONG);
		t.setView(layout);

		t.show();
		// Show the toast multiple times
		new CountDownTimer(duration, 1000) {

			public void onTick(long millisUntilFinished) {
				t.show();
			}

			public void onFinish() {
				t.show();
			}
		}.start();

	}
}
