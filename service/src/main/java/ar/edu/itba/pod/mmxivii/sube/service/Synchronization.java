package ar.edu.itba.pod.mmxivii.sube.service;


import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Synchronization implements Runnable{

    private final ConcurrentLinkedQueue<Operation> operations;
    private final CardRegistry cardRegistry;
    private boolean coordinator = false;
    private Communicator communicator;
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public Synchronization(CardRegistry cardRegistry){
        this.operations = new ConcurrentLinkedQueue<>();
        this.cardRegistry = cardRegistry;
    }

    public void setCommunicator(Communicator communicator){
        this.communicator = communicator;
    }

    public List<Operation> getUncommitedOperations(){
        List<Operation> uncommitedOperations = new LinkedList<>();
        synchronized (operations){
            uncommitedOperations.addAll(operations);
        }
        return uncommitedOperations;
    }

    public void addOperation(Operation op){
        synchronized (operations) {
           operations.add(op);
        }
    }

    public void syncOperations(Report report){
        synchronized (operations){
            operations.remove(report);
        }
    }

    public void setCoordinator(boolean bool){
        this.coordinator = bool;
    }

    @Override
    public void run() {
        while(true) {
            if (!coordinator || operations.isEmpty()) {
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Operation op;
                synchronized (operations) {
                    op = operations.poll();
                }
                try {
                    double value = cardRegistry.addCardOperation(op.getId(), op.getDescription(), op.getValue());
                    communicator.reportSynchronization(new Report(op));
                    logger.info("Operation Reported id: "+ op.getId() + " value: " + value + " timestamp: "+ op.getTimestamp());
                } catch (RemoteException e) {
                    logger.info("Server returned error, will try again later");
                    this.addOperation(op);
                }
            }
        }
    }
}
