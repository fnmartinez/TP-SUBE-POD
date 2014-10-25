package ar.edu.itba.pod.mmxivii.sube.service;


import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.Map;
import java.util.Queue;

public class Synchronization implements Runnable{

    Queue<Operation> operations;
    private final CardRegistry cardRegistry;

    public Synchronization(Queue<Operation> operations, CardRegistry cardRegistry){
        this.operations = operations;
        this.cardRegistry = cardRegistry;
    }

    @Override
    public void run() {
        if(operations.isEmpty()){
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else{
            Operation op = operations.poll();
            try {
                cardRegistry.addCardOperation(op.getId(),op.getDescription(),op.getValue());
            } catch (RemoteException e) {
                System.out.println("Server returned error, will try again later");
                operations.add(op);
            }
        }
    }
}
