package edu.nd.dronology.core.drones_runtime;

import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.RateLimiter;

import edu.nd.dronology.core.air_traffic_control.DroneSeparationMonitor;
import edu.nd.dronology.core.flight_manager.IFlightDirector;
import edu.nd.dronology.core.flight_manager.SoloDirector;
import edu.nd.dronology.core.utilities.Coordinates;
import edu.nd.dronology.core.zone_manager.FlightZoneException;
import edu.nd.dronology.util.NamedThreadFactory;
import edu.nd.dronology.util.NullUtil;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

/**
 * Creates a Managed drone.
 * 
 * @author Jane Cleland-Huang
 * @version 0.01
 */
public class ManagedDrone extends Observable implements Runnable {

	private static final ILogger LOGGER = LoggerProvider.getLogger(ManagedDrone.class);

	private static final int MAX_DRONES = 20;

	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(MAX_DRONES,
			new NamedThreadFactory("ManagedDrone"));

	private final IDrone drone; // Controls primitive flight commands for drone
	private DroneSeparationMonitor safetyMgr;

	private DroneFlightModeState droneState;
	private DroneSafetyModeState droneSafetyState;
	private boolean missionCompleted = false;
	private Coordinates targetCoordinates = null;

	// private Thread thread;
	private static final int NORMAL_SLEEP = 1;
	private int currentSleep = NORMAL_SLEEP;

	// Flight plan
	private IFlightDirector flightDirector = null; // Each drone can be assigned a single flight plan.
	private int targetAltitude = 0;
	private RateLimiter LIMITER = RateLimiter.create(1000);

	/**
	 * Constructs drone
	 * 
	 * @param drone
	 * @param drnName
	 */
	public ManagedDrone(IDrone drone) {
		NullUtil.checkNull(drone);
		this.drone = drone;// specify
		droneState = new DroneFlightModeState();
		droneSafetyState = new DroneSafetyModeState();
		drone.getDroneStatus().setStatus(droneState.getStatus());
		this.flightDirector = new SoloDirector(this); // Don't really want to create it here.
		// thread = new Thread(this);
	}

	/**
	 * Assigns a flight directive to the managed drone
	 * 
	 * @param flightDirective
	 */
	public void assignFlight(IFlightDirector flightDirective) {
		this.flightDirector = flightDirective;
		this.flightDirector.addWayPoint(drone.getBaseCoordinates()); // Currently must always return home.
	}

	/**
	 * Removes an assigned flight
	 */
	public void unassignFlight() {
		flightDirector = null; // DANGER. NEEDS FIXING. CANNOT UNASSIGN FLIGHT WITHOUT RETURNING TO BASE!!!
		LOGGER.warn("Unassigned DRONE: " + getDroneName());
	}

	/**
	 * @return latitude of current drone position
	 */
	public long getLatitude() {
		return drone.getLatitude(); // currentPosition.getLatitude();
	}

	/**
	 * 
	 * @return longitude of current drone position
	 */
	public long getLongitude() {
		return drone.getLongitude();
	}

	public void returnToHome() {
		flightDirector.returnHome(drone.getBaseCoordinates());
		getFlightSafetyModeState().setSafetyModeToNormal();

	}

	/**
	 * 
	 * @return Altitude of current drone position
	 */
	public int getAltitude() {
		return drone.getAltitude();
	}

	/**
	 * 
	 * @param targetAltitude
	 *          Sets target altitude for takeoff
	 */
	public void setTargetAltitude(int targetAltitude) {
		this.targetAltitude = targetAltitude;
	}

	/**
	 * Controls takeoff of drone
	 * 
	 * @throws FlightZoneException
	 */
	public void takeOff() throws FlightZoneException {
		missionCompleted = false;
		if (targetAltitude == 0) {
			throw new FlightZoneException("Target Altitude is 0");
		}
		LOGGER.info(getDroneName() + " ==> Taking off");
		droneState.setModeToTakingOff();
		drone.getDroneStatus().setStatus(droneState.getStatus()); // A bit ugly, but this was added to keep state for GUI middleware updated.
		drone.takeOff(targetAltitude);
		droneState.setModeToFlying();
		drone.getDroneStatus().setStatus(droneState.getStatus());
	}

	/**
	 * Delegates flyto behavior to virtual or physical drone
	 * 
	 * @param targetCoordinates
	 */
	public void flyTo(Coordinates targetCoordinates) {
		drone.flyTo(targetCoordinates);
	}

	/**
	 * Gets current coordinates from virtual or physical drone
	 * 
	 * @return current coordinates
	 */
	public Coordinates getCoordinates() {
		return drone.getCoordinates();
	}

	public void start() {
		// thread.start();
		LOGGER.info("Starting Drone '" + drone.getDroneName() + "'");
		EXECUTOR_SERVICE.submit(this);
	}

	@Override
	public void run() {

		while (true) {// && j < 500){
			// Drone has been temporarily halted. Reset to normal mode once sleep is completed.
			LIMITER.acquire();
			setSleep(NORMAL_SLEEP);
			if (droneSafetyState.isSafetyModeHalted()) {
				droneSafetyState.setSafetyModeToNormal();
			}

			// Drone currently is assigned a flight directive.
			if (flightDirector != null && droneState.isFlying()) {
				targetCoordinates = flightDirector.flyToNextPoint();

				// Move the drone. Returns FALSE if it cannot move because it has reached destination
				if (!drone.move(10))
					flightDirector.clearCurrentWayPoint();
				// Check for end of flight
				checkForEndOfFlight();

				// Check for takeoff conditions
				checkForTakeOff();

				// Set check voltage
				drone.setVoltageCheckPoint();
			}
		}
	}

	private void setSleep(int normalSleep) {
		if (currentSleep == NORMAL_SLEEP) {
			return;
		}
		currentSleep = NORMAL_SLEEP;
		LIMITER.setRate(currentSleep * 1000);
		LOGGER.info("Permits set to " + (currentSleep * 1000));
	}

	// Check for end of flight. Land if conditions are satisfied
	private boolean checkForEndOfFlight() {
		if (flightDirector != null && flightDirector.readyToLand())
			return false; // it should have returned here.
		if (droneState.isLanding())
			return false;
		if (droneState.isOnGround())
			return false;

		// Otherwise
		try {
			land();
		} catch (FlightZoneException e) {
			LOGGER.error(getDroneName() + " is not able to land!", e);
		}
		return true;
	}

	// Check for takeoff. Takeoff if conditions are satisfied.
	private boolean checkForTakeOff() {

		if (flightDirector != null && flightDirector.readyToTakeOff())
			return false;
		if (droneState.isTakingOff())
			return false;
		if (safetyMgr == null) // Sometimes caused at startup by race conditions
			return false;
		if (!safetyMgr.permittedToTakeOff(this))
			return false;

		LOGGER.info("Passed takeoff test");
		// Otherwise
		try {
			takeOff();
		} catch (FlightZoneException e) {
			LOGGER.error(getDroneName() + " is not able to takeoff!", e);
		}
		return true;
	}

	/**
	 * 
	 * @return unique drone ID
	 */
	public String getDroneName() {
		return drone.getDroneName();
	}

	/**
	 * 
	 * @return target coordinates
	 */
	public Coordinates getTargetCoordinates() {
		return targetCoordinates;
	}

	/**
	 * 
	 * @return current flight directive assigned to the managed drone
	 */
	public IFlightDirector getFlightDirective() {
		return flightDirector;
	}

	/**
	 * Land the drone. Delegate land functions to virtual or physical drone
	 * 
	 * @throws FlightZoneException
	 */
	public void land() throws FlightZoneException {
		if (!droneState.isLanding() || !droneState.isOnGround()) {
			droneState.setModeToLanding();
			drone.getDroneStatus().setStatus(droneState.getStatus());
			drone.land();
			droneState.setModeToOnGround();
			drone.getDroneStatus().setStatus(droneState.getStatus());
			unassignFlight();
		}
	}

	/**
	 * Temporarily Halt
	 * 
	 * @param seconds
	 */
	public void haltInPlace(int seconds) {
		// currentSleep = seconds * 1000;
		setSleep(seconds / 1000);
		droneSafetyState.setSafetyModeToHalted();
	}

	/**
	 * 
	 * return current flight mode state
	 * 
	 * @return droneState
	 */
	public DroneFlightModeState getFlightModeState() {
		return droneState;
	}

	/**
	 * 
	 * @return current safety mode state
	 */
	public DroneSafetyModeState getFlightSafetyModeState() {
		return droneSafetyState;
	}

	/**
	 * Set mission completed status
	 */
	public void setMissionCompleted() {
		missionCompleted = true;
	}

	/**
	 * 
	 * @return mission status
	 */
	public boolean missionInProgress() {
		return !missionCompleted;
	}

	/**
	 * Retrieve battery status from drone
	 * 
	 * @return remaining voltage
	 */
	public double getBatteryStatus() {
		return drone.getBatteryStatus();
	}

	public Coordinates getBaseCoordinates() {
		return drone.getBaseCoordinates();
	}

}
