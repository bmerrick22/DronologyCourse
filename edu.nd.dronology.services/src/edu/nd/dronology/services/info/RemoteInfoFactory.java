package edu.nd.dronology.services.info;

import java.util.List;

import edu.nd.dronology.core.flight.Flights;
import edu.nd.dronology.core.flight.IFlightPlan;
import edu.nd.dronology.core.util.Coordinate;
import edu.nd.dronology.services.core.info.FlightInfo;
import edu.nd.dronology.services.core.info.FlightPlanInfo;

public class RemoteInfoFactory {

	public static FlightInfo createFlightInfo(Flights flights) {

		FlightInfo info = new FlightInfo("FlightInfo", "FlightInfo");

		for (IFlightPlan flt : flights.getCurrentFlights()) {
			FlightPlanInfo fpl = createPlanInfo(flt);
			info.addCurrentFlight(fpl);

		}

		for (IFlightPlan flt : flights.getAwaitingTakeOffFlights()) {
			FlightPlanInfo fpl = createPlanInfo(flt);
			info.addAwaitingTakeoff(fpl);
		}

		for (IFlightPlan flt : flights.getCompletedFlights()) {
			FlightPlanInfo fpl = createPlanInfo(flt);
			info.addCompleted(fpl);
		}

		for (IFlightPlan flt : flights.getPendingFlights()) {
			FlightPlanInfo fpl = createPlanInfo(flt);
			info.addPending(fpl);
		}

		return info;

	}

	private static FlightPlanInfo createPlanInfo(IFlightPlan flt) {
		FlightPlanInfo flightPlanInfo = new FlightPlanInfo(flt.getFlightID(), flt.getFlightID());
		String droneId = flt.getAssignedDrone() != null ? flt.getAssignedDrone().getDroneName() : "--";
		List<Coordinate> waypoints = flt.getWayPoints();
		Coordinate start = flt.getStartLocation();
		long startTime = flt.getStartTime();
		long endTime = flt.getEndTime();

		flightPlanInfo.setDroneId(droneId);
		flightPlanInfo.setWaypoints(waypoints);
		flightPlanInfo.setStartLocation(start);
		flightPlanInfo.setStartTime(startTime);
		flightPlanInfo.setEndTime(endTime);

		return flightPlanInfo;
	}

}
