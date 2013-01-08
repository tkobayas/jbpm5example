package com.sample;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.service.local.LocalTaskService;

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
