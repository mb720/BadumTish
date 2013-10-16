/**
 * 
 */
package at.MatthiasBraun.BadumTishLogic;

import java.text.DecimalFormat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Debug;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import at.MatthiasBraun.R;
import at.MatthiasBraun.BadumTishData.Achievement;
import at.MatthiasBraun.BadumTishGUI.TextProgressBar;

/**
 * @author Matthias Braun
 * 
 */
public class GUImanager implements OnCheckedChangeListener, OnClickListener,
		OnSeekBarChangeListener, OnShakeListener {

	private static final String PREF_ENGLISH_JOKES_ENABLED = "englishJokesEnabled";
	private static final String PREF_GERMAN_JOKES_ENABLED = "germanJokesEnabled";
	private static final String PREF_SAFE_JOKES_ENABLED = "safeJokesEnabled";
	private static final String PREF_MATURE_JOKES_ENABLED = "matureJokesEnabled";
	private static final String PREF_SHAKE_SENSITIVITY = "shakeSensitivity";
	private static final String PREF_VIB_IS_ENABLED = "vibIsEnabled";
	private static final String VIB_IS_ENABLED = "vibIsEnabled";
	private static final String SHAKE_NR_WITH_VIBRATION = "nrOfShakesWithVibration";
	private static final int CONTRIBUTE_JOKE_REQ_CODE = 0;
	private static final int SHARE_JOKE_REQ_CODE = 1;

	// Is the user currently in the sharing activity?
	boolean userIsInShareActivity = false;

	// The phone vibrates when shaken
	private Vibrator vib;
	private boolean vibIsEnabled;

	// The media player used for playing the drumroll sound
	private MediaPlayer mediaPlayer;

	private View chooseJokesView, preferencesView, achievementsView;

	private CheckBox chkEnglishJokes, chkGermanJokes, chkSafeJokes,
			chkMatureJokes;

	private int nrOfShakesWithVib;

	public SeekBar sensitivityBar;

	public BadumTish mainActivity;

	private MyCountDownTimer steadyHandCountDown;

	public GUImanager(BadumTish mainActivity) {
		this.mainActivity = mainActivity;
		// Initialize the media player for the drum roll sound
		mediaPlayer = MediaPlayer.create(mainActivity, R.raw.drum);

		Log.i(getLogTag(), "GUImanager constructor called");

		// createGUIitems();
		// Prepare the vibrator
		vib = (Vibrator) mainActivity
				.getSystemService(BadumTish.VIBRATOR_SERVICE);

		restorePreferences();
	}

	/**
	 * Restore permanently stored GUI values .
	 */
	private void restorePreferences() {

		setVib(mainActivity.prefs.getBoolean(PREF_VIB_IS_ENABLED, true));
		nrOfShakesWithVib = mainActivity.prefs.getInt(SHAKE_NR_WITH_VIBRATION,
				0);

		float shakeSensitivity = mainActivity.prefs.getFloat(
				PREF_SHAKE_SENSITIVITY, 5.0f);
		getShaker().setSensitivity(shakeSensitivity);

	}

	public void setVib(boolean isOn) {
		vibIsEnabled = isOn;
	}

	public void logHeap() {
		Double allocated = Double.valueOf(Debug.getNativeHeapAllocatedSize())
				/ Double.valueOf((1048576));
		Double available = Double.valueOf(Debug.getNativeHeapSize()) / 1048576.0;
		Double free = Double.valueOf(Debug.getNativeHeapFreeSize()) / 1048576.0;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		Log.d(getLogTag(), "=================================");
		Log.d(getLogTag(), "heap native: allocated " + df.format(allocated)
				+ "MB of " + df.format(available) + "MB (" + df.format(free)
				+ "MB free)");
		Log.d(getLogTag(),
				"memory: allocated: "
						+ df.format(Double.valueOf(Runtime.getRuntime()
								.totalMemory() / 1048576))
						+ "MB of "
						+ df.format(Double.valueOf(Runtime.getRuntime()
								.maxMemory() / 1048576))
						+ "MB ("
						+ df.format(Double.valueOf(Runtime.getRuntime()
								.freeMemory() / 1048576)) + "MB free)");
	}

	/**
	 * This method is called by the shaker when the device is a-shaking.
	 */
	public void onShake() {
		// logHeap();

		playDrumRoll();
		getJokeManager().showRandomJoke();

		getAchievementManager().unlockAchievement("FIRST_SHAKE");

		if (vibIsEnabled) {
			vib.vibrate(200);

			mainActivity.savePermanently(SHAKE_NR_WITH_VIBRATION,
					++nrOfShakesWithVib);
			if (nrOfShakesWithVib >= 20) {
				getAchievementManager().unlockAchievement("20_VIBS");
			}
		}

		if (getSensitivityBar().getProgress() >= getSensitivityBar().getMax()) {
			// Shook on highest sensitivity
			getAchievementManager().unlockAchievement("HIGHEST_SENS");
			// The user shook the device on highest sensitivity --> reset
			// the steady hand countdown
			getSteadyHandCountDown().start();
			Log.i(getLogTag(), "OnShake: Resetting countdown");
		}

	}

	/**
	 * Shows a toast to the user.
	 * 
	 * @param text
	 *            The text that will be displayed.
	 * @param length
	 *            The amount of time it will be displayed.
	 */
	private void displayToast(String text, int length) {
		Toast toast = Toast.makeText(mainActivity, text, length);
		toast.show();
	}

	/**
	 * Get a reference to some buttons and set their listeners.
	 */
	private void createGUIitems() {

		chkEnglishJokes = (CheckBox) getChooseJokeView().findViewById(
				R.id.english_jokes);
		chkEnglishJokes.setOnCheckedChangeListener(this);
		chkGermanJokes = (CheckBox) getChooseJokeView().findViewById(
				R.id.german_jokes);
		chkGermanJokes.setOnCheckedChangeListener(this);
		chkSafeJokes = (CheckBox) getChooseJokeView().findViewById(
				R.id.safe_jokes);
		chkSafeJokes.setOnCheckedChangeListener(this);
		chkMatureJokes = (CheckBox) getChooseJokeView().findViewById(
				R.id.mature_jokes);
		chkMatureJokes.setOnCheckedChangeListener(this);

		getSensitivityBar().setOnSeekBarChangeListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.toggleVibrate) {
			boolean vibIsEnabled = ((ToggleButton) preferencesView
					.findViewById(R.id.toggleVibrate)).isChecked();
			setVib(vibIsEnabled);
			mainActivity.savePermanently(VIB_IS_ENABLED, vibIsEnabled);

		} else if (v.getId() == R.id.worlds_best_joke) {

			if (getAchievementManager().getAchievementProgress() == 100) {

				// Show the funniest joke in the world according to
				// http://en.wikipedia.org/wiki/World%27s_funniest_joke
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						mainActivity);
				dialogBuilder
						.setMessage(R.string.worlds_funniest_joke_content)
						.setCancelable(true)
						.setNeutralButton(
								mainActivity
										.getString(R.string.laughter_onomatopoeia),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog bestJokeDialog = dialogBuilder.create();

				bestJokeDialog.setTitle(mainActivity
						.getString(R.string.worlds_funniest_joke));
				bestJokeDialog.setIcon(R.drawable.app_icon);
				bestJokeDialog.show();
			} else {
				displayToast(
						mainActivity
								.getString(R.string.best_joke_not_available),
						Toast.LENGTH_LONG);
			}
		} else {
			Log.w(getLogTag(), "Unknown button pressed");
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// Do nothing on purpose
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing on purpose
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();

		Log.i(getLogTag(), "onStopTrackingTouch() was called");
		// Convert the progress from the seek bar to a float for the shaker
		float sensitivity = (seekBar.getMax() - progress) / 10.0f;
		// Sensitivity must be above zero
		if (sensitivity <= 0.0f) {
			sensitivity = 0.1f;
		}
		getShaker().setSensitivity(sensitivity);
		mainActivity.savePermanently(PREF_SHAKE_SENSITIVITY, sensitivity);

		if (seekBar.getProgress() >= seekBar.getMax()) {
			getSteadyHandCountDown().start();
		} else {
			getSteadyHandCountDown().cancel();
		}

	}

	/**
	 * Populate the achievement view with achievements.
	 */
	private void fillAchievementView() {
		TableLayout table = (TableLayout) achievementsView
				.findViewById(R.id.achievementsTable);

		for (Achievement ach : getAchievementManager()
				.getAllPossibleAchievements().values()) {
			TableRow row = new TableRow(mainActivity);

			CheckBox achName = new CheckBox(mainActivity);
			achName.setText(ach.getName() + " (" + ach.getPoints() + " "
					+ mainActivity.getString(R.string.points_abbr) + ")");
			achName.setChecked(ach.isUnlocked());
			achName.setFocusable(false);
			achName.setClickable(false);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			// Set a right margin of 20 dip
			float dens = mainActivity.getResources().getDisplayMetrics().density;
			lp.setMargins(0, 0, (int) (20 * dens), 0);
			achName.setLayoutParams(lp);

			TextView achDescr = new TextView(mainActivity);
			// If the achievement is unlocked, show its description, else show
			// '???'
			String descr = ach.isUnlocked() ? ach.getDescription()
					: mainActivity.getString(R.string.ach_unknown_exp);
			achDescr.setText(descr);
			lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			// Set a right margin of 10 dip
			dens = mainActivity.getResources().getDisplayMetrics().density;
			lp.setMargins(0, 0, (int) (10 * dens), 0);
			achDescr.setLayoutParams(lp);

			// Add the CheckBox and the TextView to the new TableRow
			row.addView(achName);
			row.addView(achDescr);

			table.addView(row, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}

	}

	/**
	 * Called when the users selects a menu item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {

		case R.id.menu_share_joke:
			shareJoke();
			break;
		case R.id.menu_send_in_joke:
			contributeJoke();
			break;

		case R.id.menu_preferences:

			// The preferencesView and its buttons have to be initialized again
			// in order to remove all its children (a child can only be added
			// once).
			preferencesView = View.inflate(mainActivity, R.layout.preferences,
					null);

			createGUIitems();
			AlertDialog.Builder menuBuilder = new AlertDialog.Builder(
					mainActivity);
			menuBuilder.setView(preferencesView);

			menuBuilder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog listViewDialog = menuBuilder.create();
			listViewDialog.setTitle(R.string.menu_preferences);

			listViewDialog.show();
			break;

		case R.id.menu_achievements:
			achievementsView = View.inflate(mainActivity,
					R.layout.achievements_menu, null);
			fillAchievementView();
			createGUIitems();
			menuBuilder = new AlertDialog.Builder(mainActivity);
			menuBuilder.setView(achievementsView);
			menuBuilder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			listViewDialog = menuBuilder.create();
			listViewDialog.setTitle(R.string.menu_achievements);
			listViewDialog.setIcon(R.drawable.achievement_icon_small);

			listViewDialog.show();
			break;

		case R.id.menu_select_jokes:
			// The chooseJokesView and its buttons have to be initialized again
			// in order to remove all its children (a child can only be added
			// once).
			chooseJokesView = View.inflate(mainActivity, R.layout.choose_jokes,
					null);
			createGUIitems();
			menuBuilder = new AlertDialog.Builder(mainActivity);
			menuBuilder.setView(chooseJokesView);
			menuBuilder.setNeutralButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			listViewDialog = menuBuilder.create();
			listViewDialog.setTitle(R.string.joke_category);
			listViewDialog.show();
			break;

		}
		updateMenuItem(item);
		return false;
	}

	/**
	 * Used for inflating the menu.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {

		mainActivity.getMenuInflater().inflate(R.menu.main_menu, menu);

		createGUIitems();
		// Discovering the menu unlocks an achievement
		getAchievementManager().unlockAchievement("MENU");

		return true;
	}

	private View getChooseJokeView() {
		if (chooseJokesView == null) {
			chooseJokesView = View.inflate(mainActivity, R.layout.choose_jokes,
					null);
		}
		return chooseJokesView;
	}

	private View getPreferencesView() {
		if (preferencesView == null) {
			preferencesView = View.inflate(mainActivity, R.layout.preferences,
					null);
		}
		return preferencesView;
	}

	/**
	 * Update a menu item.
	 * 
	 * @param menuItem
	 */
	private void updateMenuItem(MenuItem menuItem) {

		if (menuItem != null) {
			switch (menuItem.getItemId()) {
			case R.id.menu_select_jokes:
				// Check the appropriate boxes
				chkEnglishJokes.setChecked(getJokeManager().getChosenLanguage()
						.contains(JokeManager.ENGLISH_JOKES));
				chkGermanJokes.setChecked(getJokeManager().getChosenLanguage()
						.contains(JokeManager.GERMAN_JOKES));
				chkSafeJokes.setChecked(getJokeManager().getChosenAdultness()
						.contains(JokeManager.SAFE_JOKES));
				chkMatureJokes.setChecked(getJokeManager().getChosenAdultness()
						.contains(JokeManager.MATURE_JOKES));
				break;
			case R.id.menu_preferences:
				((ToggleButton) preferencesView
						.findViewById(R.id.toggleVibrate))
						.setChecked(vibIsEnabled);

				int progress = (int) (getSensitivityBar().getMax() + 1 - getShaker()
						.getSensitivity() * 10);

				getSensitivityBar().setProgress(progress);
				Log.i(getLogTag(), "Setting progress to " + progress);
				Log.i(getLogTag(), "getSensitivityBar().getProgress(): "
						+ getSensitivityBar().getProgress());
				Log.i(getLogTag(), "sensitivityBar().getProgress(): "
						+ sensitivityBar.getProgress());

				break;
			case R.id.menu_achievements:
				int prog = getAchievementManager().getAchievementProgress();
				((TextProgressBar) achievementsView
						.findViewById(R.id.achievement_progressbar))
						.setProgress(prog);
				((TextProgressBar) achievementsView
						.findViewById(R.id.achievement_progressbar))
						.setText(prog + "%");

			default:
				break;
			}
		}

		else {
			Log.w(getLogTag(), "Item is null!");

		}

	}

	/**
	 * 
	 * @return An instance of the Shaker.
	 */
	public Shaker getShaker() {
		return mainActivity.getShaker();
	}

	public SeekBar getSensitivityBar() {

		sensitivityBar = ((SeekBar) getPreferencesView().findViewById(
				R.id.shakeSensitivityBar));

		return sensitivityBar;
	}

	/**
	 * 
	 * @return An instance of the AchievementManager.
	 */
	public AchievementManager getAchievementManager() {
		return mainActivity.getAchievementManager();
	}

	/**
	 * @return An instance of the JokeManager.
	 */
	public JokeManager getJokeManager() {
		return mainActivity.getJokeManager();
	}

	/**
	 * 
	 * @return A countdown starting at 30 seconds.
	 */
	public MyCountDownTimer getSteadyHandCountDown() {
		if (steadyHandCountDown == null) {
			steadyHandCountDown = new MyCountDownTimer(this, 30000, 1000);
		}

		return steadyHandCountDown;
	}

	/**
	 * 
	 * @param resId
	 *            The resource Id of the string
	 * @return The string from the strings.xml
	 */
	private String getString(int resId) {
		return mainActivity.getString(resId);
	}

	/**
	 * Share the current joke with somebody else.
	 */
	void shareJoke() {
		userIsInShareActivity = true;
		if (getJokeManager().getCurrJoke() == getString(R.string.no_joke_found)) {
			displayToast(getString(R.string.no_joke_found), Toast.LENGTH_LONG);
			return;
		}

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
		i.putExtra(Intent.EXTRA_TEXT, getJokeManager().getCurrJoke());
		try {
			mainActivity.startActivityForResult(Intent.createChooser(i,
					getString(R.string.menu_share_joke)), SHARE_JOKE_REQ_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			displayToast(getString(R.string.no_email_clients_installed),
					Toast.LENGTH_LONG);
		}

	}

	/**
	 * Send in a joke via email.
	 */
	void contributeJoke() {
		userIsInShareActivity = true;
		Intent i = new Intent(Intent.ACTION_SEND);
		// Go directly to the email app
		i.setType("message/rfc822");
		i.putExtra(
				Intent.EXTRA_EMAIL,
				new String[] { mainActivity.getString(R.string.email_recipient) });
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
		try {
			mainActivity.startActivityForResult(
					Intent.createChooser(i, getString(R.string.send_email)),
					CONTRIBUTE_JOKE_REQ_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			displayToast(getString(R.string.no_email_clients_installed),
					Toast.LENGTH_LONG);

		}
	}

	public void onActivityResult(int requestCode) {
		if (requestCode == SHARE_JOKE_REQ_CODE) {
			getJokeManager().incrNrOfSharedJokes();
		} else if (requestCode == CONTRIBUTE_JOKE_REQ_CODE) {
			getJokeManager().incrNrOfContributedJokes();
		}
		userIsInShareActivity = false;

	}

	public String getLogTag() {
		return mainActivity.getLogTag();
	}

	/**
	 * Play the sound of a drumroll
	 */
	private void playDrumRoll() {

		if (mediaPlayer != null) {
			mediaPlayer.start();
		} else {
			Log.w(getLogTag(), "GUImanager: mediaPlayer is null.");
		}
	}

	/**
	 * React when the user checks a check box.
	 */
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.english_jokes:
			if (isChecked) {
				getJokeManager().getChosenLanguage().add(
						JokeManager.ENGLISH_JOKES);
				mainActivity.savePermanently(PREF_ENGLISH_JOKES_ENABLED, true);
			} else {
				// At least one language must be selected
				if (getJokeManager().getChosenLanguage().size() > 1) {
					getJokeManager().getChosenLanguage().remove(
							JokeManager.ENGLISH_JOKES);
					mainActivity.savePermanently(PREF_ENGLISH_JOKES_ENABLED,
							false);
				} else { // Reselect the item
					buttonView.setChecked(true);
				}
			}
			break;
		case R.id.german_jokes:
			if (isChecked) {
				getJokeManager().getChosenLanguage().add(
						JokeManager.GERMAN_JOKES);
				mainActivity.savePermanently(PREF_GERMAN_JOKES_ENABLED, true);
			} else {
				if (getJokeManager().getChosenLanguage().size() > 1) {
					getJokeManager().getChosenLanguage().remove(
							JokeManager.GERMAN_JOKES);
					mainActivity.savePermanently(PREF_GERMAN_JOKES_ENABLED,
							false);
				} else { // Reselect the item
					buttonView.setChecked(true);
				}
			}
			break;
		case R.id.safe_jokes:
			if (isChecked) {
				getJokeManager().getChosenAdultness().add(
						JokeManager.SAFE_JOKES);
				mainActivity.savePermanently(PREF_SAFE_JOKES_ENABLED, true);
			} else {
				if (getJokeManager().getChosenAdultness().size() > 1) {
					getJokeManager().getChosenAdultness().remove(
							JokeManager.SAFE_JOKES);
					mainActivity
							.savePermanently(PREF_SAFE_JOKES_ENABLED, false);
				} else { // Reselect the item if no language would be selected
					buttonView.setChecked(true);
				}
			}
			break;
		case R.id.mature_jokes:
			if (isChecked) {
				getJokeManager().getChosenAdultness().add(
						JokeManager.MATURE_JOKES);
				mainActivity.savePermanently(PREF_MATURE_JOKES_ENABLED, true);
			} else {
				if (getJokeManager().getChosenAdultness().size() > 1) {
					getJokeManager().getChosenAdultness().remove(
							JokeManager.MATURE_JOKES);
					mainActivity.savePermanently(PREF_MATURE_JOKES_ENABLED,
							false);
				} else { // Reselect the item if no adultness would be selected
					buttonView.setChecked(true);
				}
			}
			break;
		default:
			break;
		}

	}

}
