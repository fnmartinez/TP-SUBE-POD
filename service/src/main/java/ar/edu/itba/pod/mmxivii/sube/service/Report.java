package ar.edu.itba.pod.mmxivii.sube.service;

import java.io.Serializable;
import java.rmi.server.UID;

public class Report extends Operation implements Serializable{

    private static final long serialVersionUID = 7516442295622716147L;
    private boolean synched;

    public Report(Operation op){
        super(op.getId(), op.getDescription(), op.getValue(), op.getTimestamp());
        this.synched = true;
    }

    public boolean isSynched() {
        return synched;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
