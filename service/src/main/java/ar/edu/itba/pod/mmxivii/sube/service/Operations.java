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
        cache = cacheManager.getCache("sampleCacheAsync");


    }

    public double addCardOperation(@Nonnull UID id, @Nonnull String description, double amount){
        Element elem;
        if((elem = cache.get(id)) != null){
           cache.put(new Element(id, (Double) elem.getObjectValue() - amount));
           return (Double)elem.getObjectValue() + amount;
        }else{
            cache.put(new Element(id,amount));
            return amount;
        }
    }

    public double getCardBalance(@Nonnull UID id){
        Element elem;
        if((elem = cache.get(id))!= null){
            return (Double)elem.getObjectValue();
        }
        return 0;
    }
}
