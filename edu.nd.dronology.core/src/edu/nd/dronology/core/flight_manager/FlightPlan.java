package edu.nd.dronology.core.flight_manager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.nd.dronology.core.drones_runtime.ManagedDrone;
import edu.nd.dronology.core.utilities.Coordinates;
import edu.nd.dronology.core.zone_manager.FlightZoneException;

/**
 * Stores flight information including its waypoints and current status.
 * 
 * @author Jane Cleland-Huang
 * @version 0.1
 *
 */
public class FlightPlan {
	private static int flightNumber = 0;
	private String flightID;

	private List<Coordinates> wayPoints;
	private Coordinates startLocation;
	private Coordinates endLocation;

	private enum Status {
		PLANNED, FLYING, COMPLETED;
		
		@Override
		public String toString() {
      return name().charAt(0) + name().substring(1).toLowerCase();
  }
		
		
	}

	private Status status;
	private ManagedDrone drone = null;

	public Date startTime;
	public Date endTime;
	DateFormat df = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Loads flight information and assigns a flight ID. ID's are generated automatically and are unique in each run of the simulation.
	 * 
	 * @param start
	 *          Starting coordinates
	 * @param wayPoints
	 */
	public FlightPlan(Coordinates start, ArrayList<Coordinates> wayPoints) {
		this.wayPoints = wayPoints;
		this.startLocation = start;
		if (wayPoints.size() > 0) {
			this.endLocation = wayPoints.get(wayPoints.size() - 1);
		} else {
			endLocation = startLocation;
		}
		this.flightID = "DF-" + Integer.toString(++flightNumber);
		status = Status.PLANNED;
	}

	/**
	 * 
	 * @return flight ID
	 */
	public String getFlightID() {
		return flightID;
	}

	/**
	 * 
	 * @return Starting Coordinates
	 */
	public Coordinates getStartLocation() {
		return startLocation;
	}

	/**
	 * 
	 * @return Ending Coordinates
	 */
	public Coordinates getEndLocation() {
		return endLocation;
	}

	/**
	 * Returns the drone assigned to the flight plan. Will return null if no drone is yet assigned.
	 * 
	 * @return iDrone
	 */
	public ManagedDrone getAssignedDrone() {
		return drone;
	}

	public void clearAssignedDrone() {
		drone = null;
	}

	/**
	 * 
	 * @param drone
	 * @return true if drone is currently flying, false otherwise.
	 * @throws FlightZoneException
	 */
	public boolean setStatusToFlying(ManagedDrone drone) throws FlightZoneException {
		if (status == Status.PLANNED) {
			status = Status.FLYING;
			startTime = new Date();
			this.drone = drone;
			return true;
		} else
			throw new FlightZoneException("Only currently planned flights can have their status changed to flying");
	}

	/**
	 * Sets flightplan status to completed when called.
	 * 
	 * @return true
	 * @throws FlightZoneException
	 */
	public boolean setStatusToCompleted() throws FlightZoneException {
		if (status == Status.FLYING) {
			status = Status.COMPLETED;
			endTime = new Date();
			return true; // success (may add real check here later)
		} else
			throw new FlightZoneException("Only currently flying flights can have their status changed to completed");
	}

	/**
	 * Returns current flightplan status (Planned, Flying, Completed)
	 * 
	 * @return status
	 */
	public String getStatus() {

		return status.toString();

	}

	@Override
	public String toString() {
		return flightID + "\n" + getStartLocation() + " - " + getEndLocation() + "\n" + getStatus();
	}

	/**
	 * Returns way points
	 * 
	 * @return List<Coordinates>
	 */
	public List<Coordinates> getWayPoints() {
		return Collections.unmodifiableList(wayPoints);
	}

	/**
	 * Returns total number of waypoints in flight plan
	 * 
	 * @return int
	 */
	public int getNumberWayPoints() {
		return wayPoints.size();
	}

	/**
	 * Returns start time of flight.
	 * 
	 * @return date object
	 */
	public Date getStartTime() {
		return startTime;
	}

	/**
	 * REturns end time of flight.
	 * 
	 * @return date object
	 */
	public Date getEndTime() {
		return endTime;
	}
}
