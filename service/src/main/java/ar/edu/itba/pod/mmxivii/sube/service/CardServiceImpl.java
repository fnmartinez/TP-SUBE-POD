package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class CardServiceImpl extends UnicastRemoteObject implements CardService
{
	private static final long serialVersionUID = 2919260533266908792L;
	@Nonnull
	private final CardRegistry cardRegistry;
    private Cache cache;
    private Communicator communicator;

	public CardServiceImpl(@Nonnull CardRegistry cardRegistry) throws IOException
	{
		super(0);
		this.cardRegistry = cardRegistry;
        ConcurrentHashMap<UID, Double> operationMap = new ConcurrentHashMap();
        this.cache = new Cache(operationMap);

    }

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException
	{
		return cache.getBalance(id);
	}

	@Override
	public synchronized double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
        Operation op = new Operation(id,description,amount*-1);
           Double value;
            if((value = cache.getBalance(id))!= null && value >= (op.getValue()*-1)){
                cache.addOperation(op, true);
                return value + op.getValue();
            }else{
                System.out.println("Not enough cash");
            }

        return -1;
	}

	@Override
	public synchronized double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
    {
        Operation op = new Operation(id,description,amount);
            cache.addOperation(op, true);
            return cache.getBalance(id);
        }

}
