package edu.nd.dronology.services.core.persistence;

import com.thoughtworks.xstream.XStream;

import edu.nd.dronology.core.util.Coordinate;
import edu.nd.dronology.services.core.items.DroneSpecification;
import edu.nd.dronology.services.core.items.FlightRoute;

public class DronologyPersistenceUtil {

	private static final String ROUTE_ALIAS = "FlightRoute";
	private static final String COORDINATE_ALIAS = "Coordinate";
	private static final String SPEC_ALIAS = "UAVSpecification";
	
	public static void preprocessStream(XStream xstream) {

		xstream.alias(ROUTE_ALIAS, FlightRoute.class);
		xstream.alias(COORDINATE_ALIAS, Coordinate.class);
		
		
		xstream.alias(SPEC_ALIAS, DroneSpecification.class);

	}

}
