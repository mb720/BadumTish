/**
 * 
 */
package at.MatthiasBraun.BadumTishData;

/**
 * @author Matthias Braun
 * 
 */
public class Achievement {

	private String name;
	private String description;
	private int points;
//	String key;
	private boolean isUnlocked;

	@Override
	public String toString() {

		return "Achievement: " + getName() + " description: "
				+ getDescription() + " unlocked: " + isUnlocked();
	}

	public boolean isUnlocked() {
		return isUnlocked;
	}

	public void setUnlocked(boolean isUnlocked) {
		this.isUnlocked = isUnlocked;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
