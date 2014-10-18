package ar.edu.itba.pod.mmxivii.sube.service;

import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import ar.edu.itba.pod.mmxivii.sube.common.CardService;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;

public class CardServiceImpl extends UnicastRemoteObject implements CardService
{
	private static final long serialVersionUID = 2919260533266908792L;
	@Nonnull
	private final CardRegistry cardRegistry;
    private final Operations operations;

	public CardServiceImpl(@Nonnull CardRegistry cardRegistry) throws IOException
	{
		super(0);
		this.cardRegistry = cardRegistry;
        this.operations = new Operations();
	}

	@Override
	public double getCardBalance(@Nonnull UID id) throws RemoteException
	{
		return operations.getCardBalance(id);
	}

	@Override
	public double travel(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
		return operations.addCardOperation(id, description, amount * -1);
	}

	@Override
	public double recharge(@Nonnull UID id, @Nonnull String description, double amount) throws RemoteException
	{
		return operations.addCardOperation(id, description, amount);
	}
}
