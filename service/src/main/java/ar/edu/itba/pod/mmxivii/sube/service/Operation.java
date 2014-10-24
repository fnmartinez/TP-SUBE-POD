package ar.edu.itba.pod.mmxivii.sube.service;

import java.io.Serializable;
import java.rmi.server.UID;

public class Operation implements Serializable{
    private UID id;
    private String description;
    private double value;
    private static final long serialVersionUID = 7526472295622776147L;

    public Operation(UID id, String description, Double value){
        this.id = id;
        this.description = description;
        this.value = value;
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
}
