package at.MatthiasBraun.BadumTishLogic;

import java.util.HashSet;
import java.util.Set;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * This class detects when the phone has been shaken and notifies its listeners.
 * 
 * @author Matthias Braun
 * 
 */
public class Shaker implements SensorEventListener  {

	// The time after the next shake action can be executed
	private static final int GUARD_INTERVALL = 2000;

	// What acceleration difference would we assume as a rapid movement
	private float shakeSensitivity = 5.0f;

	// Here we store the current values of acceleration, one for each axis
	private float xAccel;
	private float yAccel;
	private float zAccel;

	// And here the previous ones
	private float xPreviousAccel;
	private float yPreviousAccel;
	private float zPreviousAccel;

	// Used to suppress the first shaking
	private boolean firstUpdate = true;

	// Has a shaking motion been started (one direction)
	private boolean shakeInitiated = false;

	// Store the time the last shake action was executed
	private long timeOfLastShake;

	// The set of listeners interested in shake events
	private Set<OnShakeListener> listeners;

	/**
	 * The private constructor. Shaker is a singleton.
	 */
	private Shaker() {
	}

	/**
	 * Initialize the shaker.
	 * 
	 * @param sensorManager
	 */
	public void init(SensorManager sensorManager) {

		// Let the shaker know about the accelerometer values
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);
		
		listeners = new HashSet<OnShakeListener>();
	}



	/**
	 * If the values of acceleration have changed on at least two axes, we are
	 * probably in a shake motion
	 * 
	 * @return
	 */
	private boolean isAccelerationChanged() {
		float deltaX = Math.abs(xPreviousAccel - xAccel);
		float deltaY = Math.abs(yPreviousAccel - yAccel);
		float deltaZ = Math.abs(zPreviousAccel - zAccel);
		return (deltaX > shakeSensitivity && deltaY > shakeSensitivity)
				|| (deltaX > shakeSensitivity && deltaZ > shakeSensitivity)
				|| (deltaY > shakeSensitivity && deltaZ > shakeSensitivity);
	}

	/**
	 * Store the acceleration values given by the sensor
	 */
	private void updateAccelParameters(float xNewAccel, float yNewAccel,
			float zNewAccel) {
		// We have to suppress the first change of acceleration, it results from
		// first values being initialized with 0
		if (firstUpdate) {
			xPreviousAccel = xNewAccel;
			yPreviousAccel = yNewAccel;
			zPreviousAccel = zNewAccel;
			firstUpdate = false;
		} else {
			xPreviousAccel = xAccel;
			yPreviousAccel = yAccel;
			zPreviousAccel = zAccel;
		}
		xAccel = xNewAccel;
		yAccel = yNewAccel;
		zAccel = zNewAccel;
	}

	/**
	 * Do this when the phone has been shaken.
	 */
	private void executeShakeAction() {
		// Wait a little until the next shake action is accepted
		long curTime = System.currentTimeMillis();
		if ((curTime - timeOfLastShake) > GUARD_INTERVALL) {
			//Log.i(listener.getLogTag(), "executeShakeAction()");
			notifyObservers();
			timeOfLastShake = curTime;
		}
	}

	/**
	 * Notify all listeners that the device was shaken.
	 */
	private void notifyObservers() {
		for(OnShakeListener l : listeners){
			l.onShake();
		}
		
	}
	
	/**
	 * Add a listener to the set of listeners.
	 * @param l The new listener.
	 */
	public void addListener(OnShakeListener l){
		listeners.add(l);
	}

	public float getSensitivity() {
		return shakeSensitivity;
	}

	public void setSensitivity(float s) {
		shakeSensitivity = s;
	}

	private static class SingletonHolder {
		public static final Shaker INSTANCE = new Shaker();
	}

	public static Shaker getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {
		updateAccelParameters(event.values[0], event.values[1], event.values[2]);
		if ((!shakeInitiated) && isAccelerationChanged()) {
			shakeInitiated = true;
		} else if ((shakeInitiated) && isAccelerationChanged()) {
			executeShakeAction();
		} else if ((shakeInitiated) && (!isAccelerationChanged())) {
			shakeInitiated = false;
		}

	}

}
