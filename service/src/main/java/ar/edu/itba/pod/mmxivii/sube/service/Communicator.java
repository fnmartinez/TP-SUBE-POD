package ar.edu.itba.pod.mmxivii.sube.service;

import org.jgroups.*;

import javax.print.attribute.HashPrintServiceAttributeSet;
import java.rmi.server.UID;
import java.util.*;

public class Communicator extends ReceiverAdapter {
    JChannel channel;
    private Set<Address> members;
    private Stack<Operation> operations;
    private Cache cache;
    private HashSet<UID> reported;
    private Address coordinator;

    public Communicator(Cache cache) throws Exception{
        System.setProperty("java.net.preferIPv4Stack", "true");
        channel = new JChannel();
        this.members = new HashSet<>();
        this.cache = cache;
    }

    public void connectToChannel(String nickname, String channel) throws Exception{
        this.channel.setName(nickname);
        this.channel.connect(channel);
        this.channel.setReceiver(this);
        coordinator = this.channel.getView().getMembers().get(0);
        viewAccepted(this.channel.getView());
    }


    public void viewAccepted(View view) {
        members.addAll(view.getMembers());
        members.retainAll(view.getMembers());
        if(coordinator != view.getMembers().get(0)){
            coordinator = view.getMembers().get(0);
        }
        if(coordinator.compareTo(channel.getAddress()) == 0){
            System.out.println("I am the coordinator");
            cache.setCoordinatorStatus(true);
        }

    }

    public void receive(Message msg) {
       if(!msg.getSrc().equals(channel.getAddress())){
           if(msg.getObject() instanceof Operation) {
               System.out.println("new message");
               cache.addOperation((Operation) msg.getObject(), false);
           }else if(msg.getObject() instanceof String){
               String message = (String)msg.getObject();
               if(message.compareTo("#syncCache") == 0){
                       sendMessage(msg.getSrc(), cache.getMap());
               }else if(message.compareTo("syncSync") == 0){
                   sendMessage(msg.getSrc(), cache.getUncommitedOperations());
               }
           }else if(msg.getObject() instanceof Map){
                cache.syncWithMap((Map)msg.getObject());
           }else if(msg.getObject() instanceof List){
               cache.syncSynchronizator((List<Operation>)msg.getObject());
           }
       }
    }

    private void sendMessage(Address addr, Object message){
        Message msg = new Message(addr,this.channel.getAddress(), message);
        try {
            this.channel.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void broadcastMessage(Object message) throws Exception{
        for(Address addr: members){
            this.sendMessage(addr, message);
        }
    }

    private void sendPrivateMessage(String member, Object message) throws Exception{
        for(Address addr: members){
            if(addr.toString().compareTo(member) == 0){
                sendMessage(addr, message);
            }
        }
    }

    public void reportOperation(Operation operation){
        try {
            this.broadcastMessage(operation);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void syncCache(){
        try {
            if(coordinator != this.channel.getAddress()) {
                sendMessage(coordinator, "#syncCache");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncSynchronizator(){
        try{
            if(coordinator != this.channel.getAddress()){
                sendMessage(coordinator, "#syncSync");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
