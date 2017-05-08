package edu.nd.dronology.services.remote;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.nd.dronology.services.core.api.ServiceInfo;
import edu.nd.dronology.services.core.base.AbstractServerService;
import edu.nd.dronology.services.core.remote.IDroneEquipmentRemoteService;
import edu.nd.dronology.services.core.remote.IDroneSetupRemoteService;
import edu.nd.dronology.services.core.remote.IFlightManagerRemoteService;
import edu.nd.dronology.services.core.remote.IFlightPathRemoteService;
import edu.nd.dronology.services.core.remote.IRemoteManager;
import edu.nd.dronology.services.core.remote.IRemoteServiceListener;
import edu.nd.dronology.services.core.remote.IRemoteableService;
import edu.nd.dronology.services.core.remote.RemoteInfo;
import edu.nd.dronology.services.core.util.DronologyServiceException;
import edu.nd.dronology.services.facades.DroneEquipmentServiceRemoteFacade;
import edu.nd.dronology.services.facades.DroneSetupServiceRemoteFacade;
import edu.nd.dronology.services.facades.FlightManagerServiceRemoteFacade;
import edu.nd.dronology.services.facades.FlightPathServiceRemoteFacade;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class RemoteManager implements IRemoteManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2718289978864805774L;

	private static final ILogger LOGGER = LoggerProvider.getLogger(RemoteManager.class);

	private static RemoteManager instance;

	/**
	 * @return The singleton instance of the RemoteManager.
	 */
	public static IRemoteManager getInstance() {
		if (instance == null) {
			instance = new RemoteManager();
		}
		return instance;
	}

	@Override
	public Object getService(Class<?> service) throws RemoteException, DronologyServiceException {
		if (!IRemoteableService.class.isAssignableFrom(service)) {
			throw new DronologyServiceException("Invalid service requested - Valid services extend "
					+ IRemoteableService.class.getCanonicalName());
		}

		
		if (service.equals(IFlightPathRemoteService.class)) {
			return FlightPathServiceRemoteFacade.getInstance();
		}
		
		
		if (service.equals(IFlightManagerRemoteService.class)) {
			return FlightManagerServiceRemoteFacade.getInstance();
		}
		
		if (service.equals(IDroneSetupRemoteService.class)) {
			return DroneSetupServiceRemoteFacade.getInstance();
		}
		
		if (service.equals(IDroneEquipmentRemoteService.class)) {
			return DroneEquipmentServiceRemoteFacade.getInstance();
		}
		
		

		throw new DronologyServiceException("Service" + service.getCanonicalName() + " not found!");

	}

	@Override
	public void addServiceListener(IRemoteServiceListener processListener) throws RemoteException {
		AbstractServerService.addUniversialServiceListener(new RemoteServerProcessListenerAdapter(processListener));

	}

	@Override
	public void removeServiceListener(IRemoteServiceListener processListener) throws RemoteException {
		AbstractServerService.removeUniversialServiceListener(new RemoteServerProcessListenerAdapter(processListener));

	}

	@Override
	public List<ServiceInfo> getServices() throws RemoteException, DronologyServiceException {

		List<ServiceInfo> allServices = new ArrayList<>();

		allServices.addAll(getCoreServices());
//		allServices.addAll(getExtensionServices());

		return Collections.unmodifiableList(allServices);

	}

	@Override
	public List<ServiceInfo> getCoreServices() throws RemoteException, DronologyServiceException {
		return AbstractServerService.getCoreServices();
	}
	@Override
	public List<ServiceInfo> getAllServices() throws RemoteException, DronologyServiceException {
		return AbstractServerService.getServiceInfos();
	}
	@Override
	public List<ServiceInfo> getFileServices() throws RemoteException, DronologyServiceException {
		return AbstractServerService.getFileServiceInfos();
	}


//
//	@Override
//	public void contributeService(ServiceInfo info) throws RemoteException, DistributionException {
//		ServiceOrchestrator.registerService(info);
//	}
//
//	@Override
//	public void removeService(ServiceInfo info) throws RemoteException, DistributionException {
//		ServiceOrchestrator.unregisterService(info);
//
//	}



	@Override
	public void register(RemoteInfo rInfo) throws RemoteException {
		RemoteService.getInstance().register(rInfo);

	}

	@Override
	public void unregister(RemoteInfo rInfo) throws RemoteException {
		RemoteService.getInstance().unregister(rInfo);
	}

	

	@Override
	public void initialize() throws RemoteException, DronologyServiceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tearDown() throws RemoteException, DronologyServiceException {
		// TODO Auto-generated method stub
		
	}

}
