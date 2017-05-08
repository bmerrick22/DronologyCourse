package edu.nd.dronology.core.gui_middleware;

import java.util.List;

import edu.nd.dronology.core.drones_runtime.PhysicalDroneFleetFactory;
import edu.nd.dronology.core.drones_runtime.VirtualDroneFleetFactory;
import edu.nd.dronology.core.fleet_manager.DroneFleetFactory;

public class DronologySetupDronesAccessPoint {

	private static volatile DronologySetupDronesAccessPoint INSTANCE = null;

	protected DronologySetupDronesAccessPoint() {

	}

	/**
	 * Return an instance of DronologyAccessPoint
	 * 
	 * @return
	 */
	public static DronologySetupDronesAccessPoint getInstance() {
		if (INSTANCE == null) {
			synchronized (DronologySetupDronesAccessPoint.class) {
				if (INSTANCE == null) {
					INSTANCE = new DronologySetupDronesAccessPoint();
				}
			}
		}
		return INSTANCE;
	}

	/**
	 * Placeholder We will change this interface For now -- an arraylist of strings containing: DroneID, DroneType, Longitude, Latitude, Altitude is expected.
	 * 
	 * @param newDrones,
	 *          physical=true (virtual=false)
	 */
	public void initializeDrones(List<String[]> newDrones, boolean physical) {
		DroneFleetFactory droneFleetFactory;
		if (physical) {
			droneFleetFactory = PhysicalDroneFleetFactory.getInstance();
		} else {
			droneFleetFactory = VirtualDroneFleetFactory.getInstance();
		}
		for (String[] newDrone : newDrones) {
			String droneID = newDrone[0];
			String droneType = newDrone[1];
			long latitude = Long.parseLong(newDrone[2]);
			long longitude = Long.parseLong(newDrone[3]);
			int altitude = Integer.parseInt(newDrone[4]);
			droneFleetFactory.initializeDrone(droneID, droneType, latitude, longitude, altitude);
		}

	}

}
