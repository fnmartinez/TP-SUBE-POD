package ar.edu.itba.pod.mmxivii.sube.service;

import java.rmi.server.UID;

public class Report {

    private final UID id;
    private final Long timestamp;

    public Report(UID id, Long timestamp){
        this.id = id;
        this.timestamp = timestamp;
    }

    public UID getId() {
        return id;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
