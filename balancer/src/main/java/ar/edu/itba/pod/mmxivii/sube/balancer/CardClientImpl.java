package ar.edu.itba.pod.mmxivii.sube.balancer;

import ar.edu.itba.pod.mmxivii.sube.common.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.spi.ServiceRegistry;
import java.rmi.ConnectException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.CARD_REGISTRY_BIND;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.checkNotNull;
import static ar.edu.itba.pod.mmxivii.sube.common.Utils.delay;

public class CardClientImpl extends UnicastRemoteObject implements CardClient
{
	private static final long serialVersionUID = 3498345765116694167L;
	private CardRegistry cardRegistry;
	private final CardServiceRegistryImpl cardServiceRegistry;

	public CardClientImpl(@Nonnull CardRegistry cardRegistry, @Nonnull CardServiceRegistryImpl cardServiceRegistry) throws RemoteException
	{
		super();
		this.cardRegistry = cardRegistry;
		this.cardServiceRegistry = cardServiceRegistry;
	}

	@Nonnull
	@Override
	public Card newCard(@Nonnull String cardHolder, @Nonnull String label) throws RemoteException
	{
		delay();
		try {
			return cardRegistry.newCard(cardHolder,label);
		} catch (ConnectException e) {
			try {
				reconnectCardRegistry();
				return cardRegistry.newCard(cardHolder,label);
			} catch (NotBoundException e1) {
				//noinspection ConstantConditions (esto no deberia pasar, hay que cambiar esto en el contrato para avisar)
				return null; // @ToDo cambiar a algo más representativo
			}
		}
	}

	private void reconnectCardRegistry() throws NotBoundException
	{
		cardRegistry = Utils.lookupObject(CARD_REGISTRY_BIND);
	}

	@Nullable
	@Override
	public Card getCard(@Nonnull UID id) throws RemoteException
	{
		delay();
		try {
			return cardRegistry.getCard(checkNotNull(id));
		} catch (ConnectException e) {
			try {
				reconnectCardRegistry();
				return cardRegistry.getCard(checkNotNull(id));
			} catch (NotBoundException e1) {
				return null; // @ToDo cambiar a algo más representativo
			}
		}
	}

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException
	{
		return getCardService().getCardBalance(id);
	}

	@Override
	public double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
		CardService service = null;
		try {
			service = getCardService();
			return service.travel(id, description, amount);
		} catch (NoSuchObjectException nsoe) {
			System.out.println("Error while calling the service. " + nsoe.getMessage());
			nsoe.printStackTrace();
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		} catch (RemoteException re) {
			cardServiceRegistry.unRegisterService(service);
			return this.travel(id, description, amount);
		} catch (StackOverflowError soe) {
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		}
	}

	@Override
	public double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
		// @ToDo catch de excepciones
		CardService service = null;
		try {
			service = getCardService();
			return service.recharge(id, description, amount);
		} catch (NoSuchObjectException nsoe) {
			System.out.println("Error while calling the service. " + nsoe.getMessage());
			nsoe.printStackTrace();
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		} catch (RemoteException re) {
			cardServiceRegistry.unRegisterService(service);
			return this.recharge(id, description, amount);
		} catch (StackOverflowError soe) {
			return CardRegistry.CANNOT_PROCESS_REQUEST;
		}
	}

	private CardService getCardService() throws NoSuchObjectException {
		return cardServiceRegistry.getCardService();
	}
}
