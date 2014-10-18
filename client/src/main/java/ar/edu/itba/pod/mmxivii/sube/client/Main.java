package ar.edu.itba.pod.mmxivii.sube.client;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.Card;
import ar.edu.itba.pod.mmxivii.sube.common.CardClient;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.*;

public class Main extends BaseMain
{
	private static final int MIN_CLIENTS = 10;
	private final Map<String, Card> cards = new ConcurrentHashMap<>();
	private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private final Random random = new Random();
	private CardClient cardClient = null;
	private boolean work = true;
	private int creationP = 1;
	private int rechargeP = creationP + 49;
	private int travelP = rechargeP + 50;

	private Main(@Nonnull String[] args) throws NotBoundException
	{
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		cardClient = Utils.lookupObject(CARD_CLIENT_BIND);
		if (cards.isEmpty()) {
			for (int i = 0; i < MIN_CLIENTS; i++) {
				executor.execute(new NewCardTask(cards));
			}
		}
	}

	public static void main(@Nonnull String[] args ) throws Exception
	{
		final Main main = new Main(args);
		main.run();
	}

	private void run() throws RemoteException
	{
		System.out.println("Starting Clients!");
		do {
			int p = random.nextInt(100);

			if (p < creationP) {
				executor.execute(new NewCardTask(cards));
			} else {
				if (cards.keySet().isEmpty()) continue;;
				int keysQty = cards.keySet().size();
				int keyIndex = random.nextInt(keysQty);
				String key = Iterables.get(cards.keySet(), keyIndex);
				final Card card = cards.get(key);
				final double ammount = Math.abs(random.nextDouble());
				if (p < rechargeP) {
					executor.execute(new CardRechargeTask(card, ammount));
				} else if (p < travelP) {
					executor.execute(new CardTravelTask(card, ammount));
				} else throw new Error("Critical error. Not a possible value.");
			}
		} while(work);
//		cardClient.newCard()
	}

	private void shutdown() {
		work = false;
	}

	private class NewCardTask implements Runnable {

		public final String cardHolder;
		public final String cardLabel;
		public final Map<String, Card> cards;

		public NewCardTask(Map<String, Card> cards) {
			this.cardHolder = Utils.randomString(20);
			this.cardLabel = Utils.randomString(20);
			this.cards = cards;
		}

		@Override
		public void run() {
			try {
				final Card card = cardClient.newCard(cardHolder, cardLabel);
				cards.put(cardHolder, card);
			} catch (RemoteException e) {
				System.out.println("Remote exception. Balancer probably died. " + e.getMessage());
				e.printStackTrace();
				Main.this.shutdown();
			}
		}
	}

	private class CardRechargeTask implements Runnable {

		private final Card card;
		private final double ammount;

		private CardRechargeTask(Card card, double ammount) {
			this.card = card;
			this.ammount = ammount;
		}

		@Override
		public void run() {
			synchronized (card) {
				try {
					cardClient.recharge(card.getId(), "recharge", ammount);
				} catch (RemoteException e) {
					System.out.println("Remote exception. Balancer probably died. " + e.getMessage());
					e.printStackTrace();
					Main.this.shutdown();
				}
			}
		}
	}

	private class CardTravelTask implements Runnable {
		private final Card card;
		private final double ammount;

		private CardTravelTask(Card card, double ammount) {
			this.card = card;
			this.ammount = ammount;
		}

		@Override
		public void run() {
			synchronized (card) {
				try {
					cardClient.travel(card.getId(), "travel", ammount);
				} catch (RemoteException e) {
					System.out.println("Remote exception. Balancer probably died. " + e.getMessage());
					e.printStackTrace();
					Main.this.shutdown();
				}
			}
		}
	}

}
