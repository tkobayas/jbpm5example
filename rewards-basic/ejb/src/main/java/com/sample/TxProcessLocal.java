package com.sample;

import javax.ejb.Local;

import org.drools.runtime.StatefulKnowledgeSession;

@Local
public interface TxProcessLocal {
    public long startProcess(String recipient, StatefulKnowledgeSession ksession) throws Exception;
}
