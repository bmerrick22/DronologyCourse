package edu.nd.dronology.core.air_traffic_control;

import edu.nd.dronology.core.util.Coordinate;

/**
 * Performs basic drone trigonometry
 * 
 * @author Jane
 * @version 0.1
 *
 */
public class PointDelta {
	private static long computeLatitudeDelta(Coordinate currentPosition, Coordinate targetPosition) {
		return currentPosition.getLatitude() - targetPosition.getLatitude();
	}

	private static long computeLongitudeDelta(Coordinate currentPosition, Coordinate targetPosition) {
		return currentPosition.getLongitude() - targetPosition.getLongitude();
	}

	/**
	 * 
	 * @param currentPosition
	 * @param targetPosition
	 * @return
	 */
	public static double computeAngle(Coordinate currentPosition, Coordinate targetPosition) {
		double height = (computeLatitudeDelta(currentPosition, targetPosition)); // opposite
		double hypotenuse = getRemainingDistance(currentPosition, targetPosition);
		double sinTheta = height / hypotenuse;
		double theta = Math.asin(sinTheta) * 180 / Math.PI;
		return theta;
	}

	/**
	 * 
	 * @param currentPosition
	 * @param targetPosition
	 * @return
	 */
	public static long getRemainingDistance(Coordinate currentPosition, Coordinate targetPosition) {
		return (long) Math.sqrt(Math.pow(computeLongitudeDelta(currentPosition, targetPosition), 2)
				+ Math.pow(computeLatitudeDelta(currentPosition, targetPosition), 2));
	}
}
