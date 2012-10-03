package com.sample;


import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
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

@Stateless
public class ProcessBean implements ProcessLocal {

    private static KnowledgeBase kbase;

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    public long startProcess() throws Exception {

        // load up the knowledge base
        kbase = readKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession();

        // start a new process instance
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("myVariable", "John");
        ProcessInstance processInstance = ksession.startProcess(
                "defaultPackage.AsyncWS", params);

        long processInstanceId = processInstance.getId();

        System.out.println("Process started ... : processInstanceId = "
                + processInstanceId);

        return processInstanceId;
    }

    private StatefulKnowledgeSession createKnowledgeSession() {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService
                .newStatefulKnowledgeSession(kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        AsyncWSWorkItemHandler handler = new AsyncWSWorkItemHandler();
        handler.setKsession(ksession);
        
        ksession.getWorkItemManager().registerWorkItemHandler("AsyncWS", handler);

        return ksession;
    }

    private static KnowledgeBase readKnowledgeBase() throws Exception {
        
        if (kbase != null) {
            return kbase;
        }
        
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("defaultPackage.AsyncWS.bpmn2"),
                ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    public void resumeAsyncTask(int ksessionId, long workItemId,
            Map<String, Object> results) throws Exception {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        StatefulKnowledgeSession ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(ksessionId, kbase, null, env);
        new JPAWorkingMemoryDbLogger(ksession);
        
        System.out.println("You got response from Async Web Service : " + results.get("ResponseValue1"));
        System.out.println("Now process is resuming...");
        
        ksession.getWorkItemManager().completeWorkItem(workItemId, results);
    }

}
