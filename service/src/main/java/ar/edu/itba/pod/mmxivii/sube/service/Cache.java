package ar.edu.itba.pod.mmxivii.sube.service;

import net.sf.ehcache.concurrent.Sync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.server.UID;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private ConcurrentHashMap<UID, Double> cardStatus;
    private Communicator communicator;
    private Synchronization synchronization;
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public Cache(ConcurrentHashMap<UID,Double> cardStatus, Synchronization sync){
        this.cardStatus=cardStatus;
        try {
            this.communicator = new Communicator(this, sync);
            this.synchronization = sync;
            communicator.connectToChannel("test"+ new Random().nextInt(), "clusterGroup");
            communicator.syncCache();
            synchronization.setCommunicator(communicator);
            communicator.syncSynchronizator();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addOperation(Operation operation, boolean sync){
        synchronized (cardStatus) {
            addCredit(operation.getId(), operation.getValue());
        }
        synchronized (synchronization) {
            synchronization.addOperation(operation);
        }
        if(sync){
            communicator.reportOperation(operation);
        }
        logger.info("Operation id: " + operation.getId() + " value: " + operation.getValue() + " timestamp: "+ operation.getTimestamp());
     }

    public void setCoordinatorStatus(Boolean bool){
        synchronization.setCoordinator(bool);
    }

    private void addCredit(UID id, double value){
        if (cardStatus.containsKey(id)) {
            Double credit = cardStatus.get(id);
            cardStatus.replace(id, credit, credit + value);

        } else {
            cardStatus.put(id, value);
        }
    }

    public Map<UID, Double> getMap(){
        return cardStatus;
    }

    public Double getBalance(UID uid){
        synchronized (cardStatus) {
            if (cardStatus.containsKey(uid)) {
                return cardStatus.get(uid);
            }
        }
        return 0d;
    }

    public void syncWithMap(Map<UID,Double> map){
        synchronized (cardStatus) {
            for (UID uid : map.keySet()) {
                logger.info("Starting credit id: " + uid + " value: "+ map.get(uid));
                addCredit(uid, map.get(uid));
            }
        }
    }

    public List<Operation> getUncommitedOperations(){
        synchronized (synchronization) {
            return synchronization.getUncommitedOperations();
        }
    }

    public void syncSynchronizator(List<Operation> operations){
        synchronized (synchronization) {
            for (Operation op : operations) {
                synchronization.addOperation(op);
            }
        }
    }
}
