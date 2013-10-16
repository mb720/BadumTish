/**
 * 
 */
package at.MatthiasBraun.BadumTishData;


/**
 * @author Matthias Braun
 * 
 */
public class Joke {

	 private String content;
	 private String language;
	 private String adultness;
	 private int id;
	 private boolean wasShownThisRound;
	 private boolean wasEverShownBefore;

	public void wasShown() {
		setWasShownThisRound(true);

		// If the joke was shown before do nothing
		if (wasEverShownBefore()) {
			return;
		}
		// Remember for this session that this joke was shown
		else {
			setWasEverShownBefore(true);
		}
	}

	public String toString() {

		String s = "JOKE: " + getContent() + " Adultness: " + getAdultness()
				+ " Language: " + getLanguage();
		return s;

	}

	public boolean wasEverShownBefore() {
		return wasEverShownBefore;
	}

	public void setWasEverShownBefore(boolean wasEverShownBefore) {
		this.wasEverShownBefore = wasEverShownBefore;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAdultness() {
		return adultness;
	}

	public void setAdultness(String adultness) {
		this.adultness = adultness;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean wasShownThisRound() {
		return wasShownThisRound;
	}

	public void setWasShownThisRound(boolean wasShownThisRound) {
		this.wasShownThisRound = wasShownThisRound;
	}

}
