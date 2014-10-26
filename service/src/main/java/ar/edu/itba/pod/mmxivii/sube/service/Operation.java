package ar.edu.itba.pod.mmxivii.sube.service;

import java.io.Serializable;
import java.rmi.server.UID;

public class Operation implements Serializable{
    private final UID id;
    private final String description;
    private final double value;
    private static final long serialVersionUID = 7526472295622776147L;
    private final Long timestamp;

    public Operation(UID id, String description, Double value, Long timestamp){
        this.id = id;
        this.description = description;
        this.value = value;
        this.timestamp = timestamp;
    }

    public UID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getValue() {
        return value;
    }

    public Long getTimestamp(){
        return timestamp;
    }
}
