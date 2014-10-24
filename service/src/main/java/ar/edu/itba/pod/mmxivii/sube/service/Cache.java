package ar.edu.itba.pod.mmxivii.sube.service;

import java.rmi.server.UID;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

    private ConcurrentHashMap<UID, Double> cardStatus;
    private Communicator communicator;

    public Cache(ConcurrentHashMap<UID,Double> cardStatus){
        this.cardStatus=cardStatus;
        try {
            this.communicator = new Communicator(this);
            communicator.connectToChannel("test"+ new Random().nextInt(), "clusterGroup");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void addOperation(Operation operation, boolean sync){
        addCredit(operation.getId(),operation.getValue());
        if(sync){
            communicator.reportOperation(operation);
        }
     }

    private void addCredit(UID id, double value){
        if (cardStatus.containsKey(id)) {
            Double credit = cardStatus.get(id);
            cardStatus.replace(id, value, value + value);
            System.out.println("existing card operation: " + value);
        } else {
            cardStatus.put(id, value);
            System.out.println("new card operation: "+ value);
        }
    }

    public Map<UID, Double> getMap(){
        return cardStatus;
    }

    public Double getBalance(UID uid){
        if(cardStatus.containsKey(uid)){
            return cardStatus.get(uid);
        }
        return null;
    }

    public void syncWithMap(Map<UID,Double> map){
        for(UID uid: map.keySet()){
            addCredit(uid, map.get(uid));
        }
    }
}
