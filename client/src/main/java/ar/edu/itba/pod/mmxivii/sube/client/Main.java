package ar.edu.itba.pod.mmxivii.sube.client;

import ar.edu.itba.pod.mmxivii.sube.common.BaseMain;
import ar.edu.itba.pod.mmxivii.sube.common.Card;
import ar.edu.itba.pod.mmxivii.sube.common.CardClient;
import ar.edu.itba.pod.mmxivii.sube.common.Utils;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.io.EOFException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static ar.edu.itba.pod.mmxivii.sube.common.Utils.*;

public class Main extends BaseMain
{
	private static final int MIN_CLIENTS = 10;
	private static final int MAX_CLIENTS = 100;
	private static final int MAX_INTENTS = 10;
	private static final AtomicInteger INTENTS = new AtomicInteger();
	private final Map<String, Card> cards = new ConcurrentHashMap<>();
	private final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private final Random random = new Random();
	private final Logger logger = LoggerFactory.getLogger(Main.class);
	private CardClient cardClient = null;
	private boolean work = true;
	private int creationP = 1;
	private int rechargeP = creationP + 49;
	private int travelP = rechargeP + 50;

	private Main(@Nonnull String[] args) throws NotBoundException, InterruptedException {
		super(args, DEFAULT_CLIENT_OPTIONS);
		getRegistry();
		cardClient = Utils.lookupObject(CARD_CLIENT_BIND);
		Thread.sleep(1000);
	}

	public static void main(@Nonnull String[] args ) throws Exception
	{
		final Main main = new Main(args);
		main.run();
	}

	private void run() throws RemoteException
	{
		logger.info("Starting Clients!");
		if (cards.isEmpty()) {
			for (int i = 0; i < MIN_CLIENTS; i++) {
				new NewCardTask(cards).run();
			}
		}
		logger.info("Finished creating initial clients");
		do {
			int p = random.nextInt(100);
			if (p < creationP && cards.keySet().size() < MAX_CLIENTS) {
				new NewCardTask(cards).run();
			} else {
				if (cards.keySet().isEmpty()) continue;;
				int keysQty = cards.keySet().size();
				int keyIndex = random.nextInt(keysQty);
				String key = Iterables.get(cards.keySet(), keyIndex);
				final Card card = cards.get(key);
				final double ammount = createAmmount();
				if (p < rechargeP) {
					executor.execute(new CardRechargeTask(card, ammount));
				} else if (p < travelP) {
					executor.execute(new CardTravelTask(card, ammount));
				} else throw new Error("Critical error. Not a possible value.");
			}
		} while(work);
	}

	private double createAmmount() {
		double integer = random.nextInt(100);
		double decimal = random.nextInt(100) / 100;
		return integer + decimal;
	}

	private void shutdown() {
		work = false;
	}

	private class NewCardTask extends CardTask {

		public final String cardHolder;
		public final String cardLabel;
		public final Map<String, Card> cards;

		public NewCardTask(Map<String, Card> cards) {
			this.cardHolder = Utils.randomString(20);
			this.cardLabel = Utils.randomString(20);
			this.cards = cards;
		}

		@Override
		protected void action() throws RemoteException {
			logger.info("Creating new card! cardHolder: " + cardHolder + " cardLabel: " + cardLabel);
			final Card card = cardClient.newCard(cardHolder, cardLabel);
			cards.put(cardHolder, card);
		}
	}

	private class CardRechargeTask extends CardTask {

		private final Card card;
		private final double ammount;

		private CardRechargeTask(Card card, double ammount) {
			this.card = card;
			this.ammount = ammount;
		}

		@Override
		protected void action() throws RemoteException {
			synchronized (card) {
				logger.info("Recharging! cardHolder: " + card.getCardHolder() + " cardLabel: " + card.getLabel() + " ammount: " + ammount);
				cardClient.recharge(card.getId(), "recharge", ammount);
			}
		}
	}

	private class CardTravelTask extends CardTask {
		private final Card card;
		private final double ammount;

		private CardTravelTask(Card card, double ammount) {
			this.card = card;
			this.ammount = ammount;
		}

		@Override
		protected void action() throws RemoteException {
			synchronized (card) {
				logger.info("Travelling! cardHolder: " + card.getCardHolder() + " cardLabel: " + card.getLabel() + " ammount: " + ammount);
				cardClient.travel(card.getId(), "travel", ammount);
			}
		}
	}

	private abstract class CardTask implements Runnable {

		@Override
		public  void run() {
			try {
				action();
				Main.INTENTS.set(0);
			} catch (RemoteException e) {
				logger.error("Remote exception. Balancer probably died. " + e.getMessage());
				e.printStackTrace();
				if (Main.INTENTS.incrementAndGet() == MAX_INTENTS) {
					logger.info("Max itents to communicate with balancer reached. Balancer is dead, we're shutting down.");
					Main.this.shutdown();
				}
			}
		}

		protected abstract void action() throws RemoteException;
	}

}
