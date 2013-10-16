/**
 * 
 */
package at.MatthiasBraun.BadumTishLogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import android.util.Log;
import android.widget.TextView;
import at.MatthiasBraun.R;
import at.MatthiasBraun.BadumTishData.Joke;

/**
 * @author Matthias Braun
 * 
 */
public class JokeManager {

	private BadumTish mainActivity;
	private int nrOfContributedJokes;
	private int nrOfSharedJokes;

	// All the jokes
	private ArrayList<Joke> jokes;

	// The user can choose the language and adultness of the shown jokes
	private HashSet<String> chosenLanguage = new HashSet<String>();
	private HashSet<String> chosenAdultness = new HashSet<String>();

	// Constants
	public static final String XML_JOKE_TAG = "joke";
	public static final String PREF_NR_OF_CONTRIBUTED_JOKES = "nrOfContributedJokes";
	public static final String PREF_NR_OF_SHARED_JOKES = "nrOfSharedJokes";
	public static final String PREF_JOKE_WAS_SHOWN = "jokeWasShown";
	public static final String ENGLISH_JOKES = "english";
	public static final String GERMAN_JOKES = "german";
	public static final String SAFE_JOKES = "safe";
	public static final String MATURE_JOKES = "mature";

	public static final short INITIAL_JOKE_NR = -1;

	private int currJokeNr = INITIAL_JOKE_NR;

	public JokeManager(BadumTish mainActivity) {

		this.mainActivity = mainActivity;

		// Load the jokes from a file
		try {
			loadJokesFromFile();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		loadPermJokeData();
	}

	/**
	 * Restore preferences and load permanently stored data about the jokes.
	 */
	private void loadPermJokeData() {

		// Enable the different joke categories
		boolean langIsGerman = Locale.getDefault().getLanguage().equals("de");

		if (mainActivity.prefs.getBoolean("englishJokesEnabled", !langIsGerman))
			getChosenLanguage().add(ENGLISH_JOKES);
		if (mainActivity.prefs.getBoolean("germanJokesEnabled", langIsGerman))
			getChosenLanguage().add(GERMAN_JOKES);

		// Only show safe jokes per default
		if (mainActivity.prefs.getBoolean("safeJokesEnabled", true))
			getChosenAdultness().add(SAFE_JOKES);
		if (mainActivity.prefs.getBoolean("matureJokesEnabled", false))
			getChosenAdultness().add(MATURE_JOKES);

		currJokeNr = mainActivity.prefs.getInt("currJokeNr", INITIAL_JOKE_NR);

		// Load the permanent data about the jokes
		for (Joke j : jokes) {
			j.setWasEverShownBefore(mainActivity.prefs.getBoolean(
					PREF_JOKE_WAS_SHOWN + j.getId(), false));
		}

		nrOfContributedJokes = mainActivity.prefs.getInt(
				PREF_NR_OF_CONTRIBUTED_JOKES, 0);
		nrOfSharedJokes = mainActivity.prefs.getInt(PREF_NR_OF_SHARED_JOKES, 0);

	}

	/**
	 * Parse the jokes from an XML file.
	 * 
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private void loadJokesFromFile() throws XmlPullParserException, IOException {

		jokes = new ArrayList<Joke>();
		XmlResourceParser xrp = mainActivity.getResources().getXml(R.xml.jokes);
		while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
			Joke j = new Joke();
			if (xrp.getEventType() == XmlResourceParser.START_TAG) {

				if (xrp.getName().equals(XML_JOKE_TAG)) {

					j.setId(xrp.getAttributeIntValue(null, "id", -1));
					j.setAdultness(xrp.getAttributeValue(null, "adultness"));
					j.setLanguage(xrp.getAttributeValue(null, "language"));
					j.setContent(xrp.getAttributeValue(null, "content"));

					// Add the joke to the list
					jokes.add(j);
				}

			}
			xrp.next();
		}
		xrp.close();
	}

	/**
	 * Display a joke on the screen.
	 * 
	 * @param joke
	 */
	private void displayJoke(Joke joke) {

		final TextView jokeTextView = (TextView) mainActivity
				.findViewById(R.id.joke_text_view);
		// First do some error checking
		if (joke == null) {
			jokeTextView.setText(R.string.shake_me);
			return;
		}

		if (jokeTextView == null) {
			Log.w(mainActivity.getLogTag(), "jokeTextView is null");
			return;
		}

		// There was no adultness or language selected (this shouldn't happen)
		if (getChosenAdultness().size() <= 0 || getChosenLanguage().size() <= 0) {
			jokeTextView.setText(R.string.select_category);
			return;
		}

		jokeTextView.setText(joke.getContent());
		joke.wasShown();
		// Save permanently that this joke was shown
		mainActivity.savePermanently(PREF_JOKE_WAS_SHOWN + joke.getId(), true);

		// Check if the user has unlocked an achievement
		int shownJokes = getNrOfEverShownJokes();
		Log.i(mainActivity.getLogTag(), "shownJokes: " + shownJokes);
		if (shownJokes >= 60) {
			mainActivity.getAchievementManager().unlockAchievement(
					"SEEN_60_JOKES");
		}
		if (shownJokes >= 30) {
			mainActivity.getAchievementManager().unlockAchievement(
					"SEEN_30_JOKES");
		}
		if (shownJokes >= 15) {
			mainActivity.getAchievementManager().unlockAchievement(
					"SEEN_15_JOKES");
		}
		if (shownJokes >= 5) {
			mainActivity.getAchievementManager().unlockAchievement(
					"SEEN_5_JOKES");
		}

	}

	/**
	 * Increase the number of shared jokes by one and store that value
	 * permanently
	 */
	public void incrNrOfSharedJokes() {
		mainActivity
				.savePermanently(PREF_NR_OF_SHARED_JOKES, ++nrOfSharedJokes);

		// Check if an achievement was unlocked
		if (nrOfSharedJokes >= 50) {
			mainActivity.getAchievementManager().unlockAchievement("50_SHARED");
		}
		if (nrOfSharedJokes >= 30) {
			mainActivity.getAchievementManager().unlockAchievement("30_SHARED");
		}
		if (nrOfSharedJokes >= 15) {
			mainActivity.getAchievementManager().unlockAchievement("15_SHARED");
		}
		if (nrOfSharedJokes >= 5) {
			mainActivity.getAchievementManager().unlockAchievement("5_SHARED");
		}
		if (nrOfSharedJokes >= 1) {
			mainActivity.getAchievementManager().unlockAchievement("1_SHARED");
		}
	}

	/**
	 * Increase the number of contributed jokes by one and store that value
	 * permanently
	 */
	public void incrNrOfContributedJokes() {
		mainActivity.savePermanently(PREF_NR_OF_CONTRIBUTED_JOKES,
				++nrOfContributedJokes);

		// Check if an achievement was unlocked
		if (nrOfContributedJokes >= 50) {
			mainActivity.getAchievementManager().unlockAchievement("50_CONTR");
		}
		if (nrOfContributedJokes >= 30) {
			mainActivity.getAchievementManager().unlockAchievement("30_CONTR");
		}
		if (nrOfContributedJokes >= 15) {
			mainActivity.getAchievementManager().unlockAchievement("15_CONTR");
		}
		if (nrOfContributedJokes >= 5) {
			mainActivity.getAchievementManager().unlockAchievement("5_CONTR");
		}
		if (nrOfContributedJokes >= 1) {
			mainActivity.getAchievementManager().unlockAchievement("1_CONTR");
		}
	}

	private int getNrOfEverShownJokes() {
		int nrOfEverShownJokes = 0;
		for (Joke j : jokes) {
			if (j.wasEverShownBefore()) {
				nrOfEverShownJokes++;
			}
		}
		// Log.i(BadumTish.LOGTAG, "Ever shown jokes: " + nrOfEverShownJoke);
		return nrOfEverShownJokes;
	}

	/**
	 * Randomly get a joke from the list of joke.
	 * 
	 * @return A random joke.
	 */
	private Joke getRandomJoke() {

		if (!jokesAvailable()) {
			newRound(); // Maybe all jokes have already been shown
			if (!jokesAvailable()) {// There are still no jokes available
				return null;
			}
		}

		currJokeNr = new Random().nextInt(jokes.size());
		// Try for a while to find a new joke that should be shown
		for (int i = 0; i < 300; i++) {
			if (!jokeIsValid(jokes.get(currJokeNr))) {
				currJokeNr = new Random().nextInt(jokes.size());
			} else {
				break;
			}
		}

		mainActivity.savePermanently("currJokeNr", currJokeNr);

		return jokes.get(currJokeNr);
	}

	/**
	 * Checks whether there is at least one joke for the current preferences.
	 * 
	 * @return Whether there is at least one joke for the current preferences
	 */
	private boolean jokesAvailable() {
		for (Joke j : jokes) {
			if (getChosenAdultness().contains(j.getAdultness())
					&& getChosenLanguage().contains(j.getLanguage())
					&& !j.wasShownThisRound()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if a joke's properties are in accordance with the allowed
	 * categories and it hasn't been show before.
	 * 
	 * @param joke
	 *            The joke to be examined.
	 * @return Whether the joke is valid or not.
	 */
	private boolean jokeIsValid(Joke joke) {
		return getChosenAdultness().contains(joke.getAdultness())
				&& getChosenLanguage().contains(joke.getLanguage())
				&& !joke.wasShownThisRound();
	}

	/**
	 * When the user has seen all jokes, show the again.
	 */
	private void newRound() {
		for (Joke j : jokes) {
			j.setWasShownThisRound(false);
		}
		// Log.i(LOGTAG, "New Round");
	}

	/**
	 * Display the current joke. If the joke nr has its initial value, show
	 * "Shake me" on the screen.
	 */

	public void displayCurrJoke() {

		if (currJokeNr == INITIAL_JOKE_NR) {
			displayJoke(null);
		}

		else if (currJokeNr < jokes.size()) {
			displayJoke(jokes.get(currJokeNr));
		}
	}

	public void showRandomJoke() {
		displayJoke(getRandomJoke());
	}

	/**
	 * Reset the current joke nr so it shows "Shake me" on the joke view.
	 * 
	 * @return
	 */
	public void resetJoke() {
		Log.i(mainActivity.getLogTag(), "Resetting joke");
		currJokeNr = INITIAL_JOKE_NR;
		mainActivity.savePermanently("currJokeNr", currJokeNr);
	}

	public String getCurrJoke() {
		if (currJokeNr == INITIAL_JOKE_NR) {
			return mainActivity.getString(R.string.no_joke_found);
		} else {
			return jokes.get(currJokeNr).getContent();
		}
	}

	public HashSet<String> getChosenLanguage() {
		return chosenLanguage;
	}

	public void setChosenLanguage(HashSet<String> chosenLanguage) {
		this.chosenLanguage = chosenLanguage;
	}

	public HashSet<String> getChosenAdultness() {
		return chosenAdultness;
	}

	public void setChosenAdultness(HashSet<String> chosenAdultness) {
		this.chosenAdultness = chosenAdultness;
	}
}