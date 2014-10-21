package ar.edu.itba.pod.mmxivii.sube.service;

import net.sf.ehcache.*;
import net.sf.ehcache.distribution.jgroups.JGroupsCacheReplicatorFactory;
import net.sf.ehcache.event.CacheManagerEventListener;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.MulticastSocket;
import java.rmi.server.UID;

public class Operations {

    private CacheManager cacheManager;
    private final Cache cache;

    public Operations() throws IOException {
        CacheManager cacheManager = CacheManager.create();
        for(String str: cacheManager.getCacheNames()){
            System.out.println(str);
        }
        cache = cacheManager.getCache("sampleCacheAsync");
    }

    public double addCardOperation(@Nonnull UID id, @Nonnull String description, double amount){
        Element elem;
        double amnt;
        if((elem = cache.get(id)) != null){
            System.out.println("la key esta en el cache, valor:"+ (Double)elem.getObjectValue());
           cache.put(new Element(id, (Double) elem.getObjectValue() + amount));
           amnt = (Double)elem.getObjectValue() + amount;
        }else{
            System.out.println("la key no esta en el cache");
            cache.put(new Element(id,amount));
            amnt =  amount;
        }
        return amnt;
    }

    public double getCardBalance(@Nonnull UID id){
        Element elem;
        if((elem = cache.get(id))!= null){
            return (Double)elem.getObjectValue();
        }
        return 0;
    }
}
