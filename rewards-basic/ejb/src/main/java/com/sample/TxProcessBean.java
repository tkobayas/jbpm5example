package com.sample;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TxProcessBean implements TxProcessLocal {

    public long startProcess(String recipient, StatefulKnowledgeSession ksession) throws Exception {

        long processInstanceId;
        // start a new process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recipient", recipient);
        ProcessInstance processInstance = ksession.startProcess("com.sample.rewards-basic", params);

        processInstanceId = processInstance.getId();

        System.out.println("Process started ... : processInstanceId = " + processInstanceId);

        return processInstanceId;
    }

}
