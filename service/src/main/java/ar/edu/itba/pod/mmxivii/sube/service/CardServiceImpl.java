package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CardServiceImpl extends UnicastRemoteObject implements CardService
{
	private static final long serialVersionUID = 2919260533266908792L;
	@Nonnull
	private final CardRegistry cardRegistry;
    private Cache cache;

	public CardServiceImpl(@Nonnull CardRegistry cardRegistry) throws IOException
	{
		super(0);
		this.cardRegistry = cardRegistry;
        ConcurrentHashMap<UID, Double> operationMap = new ConcurrentHashMap();
        Synchronization sync = new Synchronization(cardRegistry);
        (new Thread(sync)).start();
        this.cache = new Cache(operationMap, sync);


    }

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException
	{
		return cache.getBalance(id);
	}

	@Override
	public synchronized double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
        Operation op = new Operation(id,description,amount*-1, System.currentTimeMillis());
           Double value;
            if((value = cache.getBalance(id))!= null && value >= (op.getValue()*-1)){
                cache.addOperation(op, true);
                return value + op.getValue();
            }else{
                System.out.println("Not enough cash, cardId: " + id);
            }

        return -1;
	}

	@Override
	public synchronized double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
    {
        Operation op = new Operation(id,description,amount, System.currentTimeMillis());
        if(cache.getBalance(id)+amount > 100d){
            System.out.println("can't recharge that much id: "+ id);
            return -4d;
        }
        cache.addOperation(op, true);
        return cache.getBalance(id);
        }

}
