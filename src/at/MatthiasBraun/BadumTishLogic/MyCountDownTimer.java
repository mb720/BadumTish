package at.MatthiasBraun.BadumTishLogic;

import android.os.CountDownTimer;
import android.util.Log;

public class MyCountDownTimer extends CountDownTimer {

	private GUImanager caller;

	public MyCountDownTimer(GUImanager caller, long millisInFuture,
			long countDownInterval) {
		super(millisInFuture, countDownInterval);
		this.caller = caller;
		// Log.i(BadumTish.LOGTAG, "MyCountDownTimer called");
	}

	@Override
	public void onFinish() {
		// Steady hand achievement
		caller.getAchievementManager().unlockAchievement("30_SECS_STILL");

	}

	@Override
	public void onTick(long millisUntilFinished) {
		Log.i(caller.getLogTag(), "Countdown: " + millisUntilFinished / 1000);

	}

}
