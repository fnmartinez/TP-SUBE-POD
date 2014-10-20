package ar.edu.itba.pod.mmxivii.sube.balancer;

import ar.edu.itba.pod.mmxivii.sube.common.CardService;
import ar.edu.itba.pod.mmxivii.sube.common.CardServiceRegistry;

import javax.annotation.Nonnull;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CardServiceRegistryImpl extends UnicastRemoteObject implements CardServiceRegistry
{
	private static final long serialVersionUID = 2473638728674152366L;
	private final AtomicInteger index = new AtomicInteger(0);
	private final List<CardService> serviceList = Collections.synchronizedList(new ArrayList<CardService>());

	protected CardServiceRegistryImpl() throws RemoteException {}

	@Override
	public void registerService(@Nonnull CardService service) throws RemoteException
	{
		serviceList.add(service);
	}

	@Override
	public void unRegisterService(@Nonnull CardService service) throws RemoteException
	{
		index.decrementAndGet();
		serviceList.remove(service);
	}

	@Override
	public Collection<CardService> getServices() throws RemoteException
	{
		return serviceList;
	}

	CardService getCardService() throws NoSuchObjectException {
		if (serviceList.isEmpty()) throw new NoSuchObjectException("No more services in list");
		int selected = index.getAndIncrement();
		return serviceList.get(selected % serviceList.size());
	}
}
