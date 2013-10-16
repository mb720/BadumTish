/**
 * 
 */
package at.MatthiasBraun.UnitTests;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.util.Log;
import android.widget.TextView;
import at.MatthiasBraun.R;
import at.MatthiasBraun.BadumTishData.Achievement;
import at.MatthiasBraun.BadumTishLogic.BadumTish;

/**
 * @author Matthias Braun
 * 
 */
public class BadumTishUnitTests extends
		ActivityInstrumentationTestCase2<BadumTish> {

	private BadumTish activity;

	private TextView jokeTextView;

	public BadumTishUnitTests() {
		super("at.MatthiasBraun.BadumTishLogic.BadumTish", BadumTish.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.gc();
		activity = this.getActivity();
	
		// Clear the apps preferences
		Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
		editor.clear();
		if(editor.commit()){
			Log.i(activity.getLogTag(), "editor.commit() called successfully");
		}

		//activity.jokeMan = new JokeManager(activity);
		
		activity.getGUImanager().setVib(false);

		jokeTextView = (TextView) activity
				.findViewById(at.MatthiasBraun.R.id.joke_text_view);
		// resourceString = mActivity
		// .getString(at.MatthiasBraun.R.string.joke_text);
	}

	public void testJokeTextViewNotNull() {
		assertNotNull(activity
				.findViewById(at.MatthiasBraun.R.id.joke_text_view));
	}

	public void testCurrJokeIsSafedWhenOtherIntentStarts() {
		String oldJoke = (String) jokeTextView.getText();

		// Go to the home screen
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		activity.startActivity(i);

		// Restart the activity
		activity = this.getActivity();

		String newJoke = (String) jokeTextView.getText();

		assertTrue(oldJoke.equals(newJoke));

	}

	@UiThreadTest
	public void testThereIsAFirstJoke() {
		activity.getGUImanager().onShake();

		String initialJokeText = activity
				.getString(at.MatthiasBraun.R.string.shake_me);
		String currJokeText = (String) jokeTextView.getText();
		assertFalse(
				"After a shake the joke text mustn't be equal to the initial joke text",
				currJokeText.equals(initialJokeText));
	}

	@UiThreadTest
	public void testNewJokeAfterShake() {
		String oldJoke = (String) jokeTextView.getText();
		activity.getGUImanager().onShake();

		String newJoke = (String) jokeTextView.getText();
		assertFalse("There must be a new joke after a shake. Old joke: "
				+ oldJoke + " New joke: " + newJoke, oldJoke.equals(newJoke));
	}

	/**
	 * The text of the joke view should be the "Shake Me" text after the user
	 * has left and restarted the app.
	 */
	public void testJokeTextIsResetWhenUserLeavesApp() {
		activity.userLeavesApp();
		// Restart the activity
		activity = this.getActivity();
		String jokeViewText = (String) jokeTextView.getText();
		String shakeMe = activity.getString(R.string.shake_me);
		assertTrue(shakeMe.equals(jokeViewText));

	}

	// Test the achievements

	public void testOptionsMenuAchievement() {
		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("MENU");
		assertFalse(menuAch.isUnlocked());
		activity.openOptionsMenu();

		assertTrue(menuAch.isUnlocked());
	}

	public void testUserLeavesAppAchievement() {
		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("QUIT");
		assertFalse(menuAch.isUnlocked());
		activity.userLeavesApp();

		assertTrue(menuAch.isUnlocked());
	}

	public void testAllAchievementsAreLocked() {

		for (Achievement ach : activity.getAchievementManager()
				.getAllPossibleAchievements().values()) {

			assertFalse("This achievement should be locked: " + ach.toString(),
					ach.isUnlocked());

		}
	}

	@UiThreadTest
	public void testFirstShakeAchievement() {

		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("FIRST_SHAKE");

		activity.getGUImanager().onShake();

		assertTrue(menuAch.isUnlocked());
	}

	@UiThreadTest
	public void testSeen5JokesAchievement() {

		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("SEEN_5_JOKES");

		for (int i = 0; i < 5; i++) {
			Log.i(activity.getLogTag(), "testSeen5JokesAchievement is shaking");
			activity.getGUImanager().onShake();
		}

		assertTrue(menuAch.isUnlocked());
	}

	@UiThreadTest
	public void testSeen15JokesAchievement() {
		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("SEEN_15_JOKES");

		for (int i = 0; i < 15; i++) {
			activity.getGUImanager().onShake();
		}

		assertTrue(menuAch.isUnlocked());
	}
	@UiThreadTest
	public void testSeen30JokesAchievement() {
		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("SEEN_30_JOKES");

		for (int i = 0; i < 30; i++) {
			activity.getGUImanager().onShake();
		}

		assertTrue(menuAch.isUnlocked());
	}
	@UiThreadTest
	public void testSeen60JokesAchievement() {
		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("SEEN_60_JOKES");

		for (int i = 0; i < 60; i++) {
			activity.getGUImanager().onShake();
		}

		assertTrue(menuAch.isUnlocked());
	}
	
	@UiThreadTest
	public void testHighestSensAchievement() {
		Achievement menuAch = activity.getAchievementManager()
				.getAllPossibleAchievements().get("HIGHEST_SENS");

		int highestSens = activity.getGUImanager().getSensitivityBar().getMax();
		
		activity.getGUImanager().getSensitivityBar().setProgress(highestSens);
		activity.getGUImanager().onShake();
		
		assertTrue(menuAch.isUnlocked());
	}
//	@UiThreadTest
//	public void testHighestSensAchievement() {
//		Achievement menuAch = activity.getAchievementManager()
//				.getAllPossibleAchievements().get("HIGHEST_SENS");
//
//		for (int i = 0; i < 60; i++) {
//			activity.getGUImanager().onShake();
//		}
//
//		assertTrue(menuAch.isUnlocked());
//	}
	
	
	
	
}