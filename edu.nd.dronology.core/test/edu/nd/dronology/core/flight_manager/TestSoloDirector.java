package edu.nd.dronology.core.flight_manager;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.nd.dronology.core.drones_runtime.ManagedDrone;
import edu.nd.dronology.core.drones_runtime.VirtualDrone;
import edu.nd.dronology.core.utilities.Coordinates;

public class TestSoloDirector {

	SoloDirector testInstance;

	@Before
	public void setUp() throws Exception {
		testInstance = new SoloDirector(new ManagedDrone(new VirtualDrone("abc"), "abc"));
		testInstance.addWayPoint(new Coordinates(20, 20, 40));
		testInstance.addWayPoint(new Coordinates(21, 20, 40));
		testInstance.addWayPoint(new Coordinates(22, 20, 40));
		testInstance.addWayPoint(new Coordinates(23, 20, 40));
		testInstance.addWayPoint(new Coordinates(24, 20, 40));
		
	}

	
	/**
	 * The home location proviied as parameter does not exist in the director
	 */
	@Test(expected = Exception.class)
	public void testReturnHomeWithWrongCoordinates() {
		testInstance.returnHome(new Coordinates(1, 2, 3));

	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testReturnHomeWitNullCoordinates() {
		testInstance.returnHome(null);

	}
	

	/**
	 * home is the first coordinate
	 */
	@Test
	public void testReturnHomeWitProperCoordinates1() {
		testInstance.returnHome(new Coordinates(20, 20, 40));
		assertEquals(1, testInstance.wayPoints.size());
		
		
		List<Coordinates> expected= new ArrayList<>();
		expected.add(new Coordinates(20, 20, 40));
		assertEquals(expected,testInstance.wayPoints);
	}
	
	/**
	 * home is the last coordinate
	 */
	@Test
	public void testReturnHomeWitProperCoordinates2() {
		testInstance.returnHome(new Coordinates(24, 20, 40));
		assertEquals(1, testInstance.wayPoints.size());
		List<Coordinates> expected= new ArrayList<>();
		expected.add(new Coordinates(24, 20, 40));
		assertEquals(expected,testInstance.wayPoints);
	}
	
	
	/**
	 * home is a coordinate somewhere in the middle of the list
	 * 
	 */
	@Test
	public void testReturnHomeWitProperCoordinates3() {
		testInstance.returnHome(new Coordinates(22, 20, 40));
		assertEquals(1, testInstance.wayPoints.size());
		List<Coordinates> expected= new ArrayList<>();
		expected.add(new Coordinates(22, 20, 40));
		assertEquals(expected,testInstance.wayPoints);
	}

}
