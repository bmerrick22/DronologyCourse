package edu.nd.dronology.gstation.python.connector.service;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.nd.dronology.core.DronologyConstants;
import edu.nd.dronology.gstation.python.connector.GroundStationException;
import edu.nd.dronology.gstation.python.connector.GroundstationConnector;
import edu.nd.dronology.gstation.python.connector.connect.IncommingGroundstationConnectionServer;
import edu.nd.dronology.gstation.python.connector.messages.ConnectionRequestMessage;
import edu.nd.dronology.services.core.base.AbstractServiceInstance;
import edu.nd.dronology.util.NamedThreadFactory;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class DroneConnectorServiceInstance extends AbstractServiceInstance implements IDroneConnectorServiceInstance {
	ExecutorService connectionExecutor = Executors.newFixedThreadPool(DronologyConstants.MAX_GROUNDSTATIONS,
			new NamedThreadFactory("Connection-Socket-Threads"));

	private static final ILogger LOGGER = LoggerProvider.getLogger(DroneConnectorServiceInstance.class);

	static final transient Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls()
			.setDateFormat(DateFormat.LONG).setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES)
			.setVersion(1.0).serializeSpecialFloatingPointValues().create();

	private IncommingGroundstationConnectionServer server;
	private Map<String, Future> activeConnections = new HashMap<>();

	public DroneConnectorServiceInstance() {
		super("DRONECONNECTOR");
	}

	@Override
	protected Class<?> getServiceClass() {
		return DroneConnectorService.class;
	}

	@Override
	protected int getOrder() {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	protected String getPropertyPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doStartService() throws Exception {
		server = new IncommingGroundstationConnectionServer();
		servicesExecutor.submit(server);
	}

	@Override
	protected void doStopService() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeConnection(String connectionId) {
		if (activeConnections.containsKey(connectionId)) {
			LOGGER.info("Removing connection!" + connectionId);
			Future<?> conn = activeConnections.remove(connectionId);
			conn.cancel(true);
		} else {
			LOGGER.warn("Connection with id " + connectionId + " not found");
		}
	}

	@Override
	public void handleConnection(GroundstationConnector connectionHandler) {
		if (activeConnections.size() >= DronologyConstants.MAX_GROUNDSTATIONS) {
			LOGGER.warn("Connection Limit reached - no new parallel connections can be added!");
			return;
		}

		Future<?> future = connectionExecutor.submit(connectionHandler);
	}

	@Override
	public void registerConnection(GroundstationConnector connector, ConnectionRequestMessage msg)
			throws GroundStationException {
		LOGGER.info("Connection requested by groundstation '" + msg.getUavid() + "'");
		String groundstationId = msg.getUavid();
		if (activeConnections.containsKey(groundstationId)) {
			throw new GroundStationException("Groundstation already registered! " + groundstationId);
		}
		activeConnections.put(msg.getUavid(), null);
	}

}
