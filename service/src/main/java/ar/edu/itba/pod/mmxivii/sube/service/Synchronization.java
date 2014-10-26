package ar.edu.itba.pod.mmxivii.sube.service;


import ar.edu.itba.pod.mmxivii.sube.common.CardRegistry;
import com.javafx.tools.doclets.formats.html.SourceToHTMLConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UID;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Synchronization implements Runnable{

    private final ConcurrentLinkedQueue<Node> operations;
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
            for(Node node: operations){
                uncommitedOperations.addAll(node.operations);
            }
        }
        return uncommitedOperations;
    }

    public void addOperation(Operation op){
        synchronized (operations) {
           for(Node node: operations){
               if(node.id == op.getId()){
                   node.addOperation(op);
                   return;
               }
           }
           operations.add(new Node(op));
        }
    }

    public void syncOperations(Report report){
        synchronized (operations){
            for(Node node: operations){
                if(node.id == report.getId()){
                    if(node.syncUpToTimestamp(report.getTimestamp()) == 0){
                        operations.remove(node);
                    }
                }
            }
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
                Node node;
                synchronized (operations) {
                    node = operations.poll();
                }
                Operation op = node.mergeOperations();
                try {
                    double value = cardRegistry.addCardOperation(op.getId(), op.getDescription(), op.getValue());
                    logger.info("Operation Reported id: "+ op.getId() + " value: " + value);
                    communicator.reportSynchronization(new Report(op.getId(), op.getTimestamp()));
                } catch (RemoteException e) {
                    logger.info("Server returned error, will try again later");
                    this.addOperation(op);
                }
            }
        }
    }

    private class Node{
        private SortedSet<Operation> operations = Collections.synchronizedSortedSet(new TreeSet<Operation>(new Comparator<Operation>() {
            @Override
            public int compare(Operation o1, Operation o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        }));

        private UID id;

        public Node(Operation op){
            this.id = op.getId();
            operations.add(op);
        }

        public void addOperation(Operation op){
            operations.add(op);
        }

        public Operation mergeOperations(){
            Double sum = 0d;
            for(Operation op: operations){
                sum += op.getValue();
            }
            return new Operation(operations.last().getId(), "merged", sum, operations.last().getTimestamp());
        }

        public int syncUpToTimestamp(Long epoch){
            for(Operation op: operations){
                if(op.getTimestamp().compareTo(epoch) <= 0){
                    operations.remove(operations.first());
                }else{
                    return operations.size();
                }
            }
            return operations.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (!id.equals(node.id)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
