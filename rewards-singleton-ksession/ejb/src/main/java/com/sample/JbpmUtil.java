package com.sample;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.service.local.LocalTaskService;

public class JbpmUtil {

    // singleton
    private static StatefulKnowledgeSession ksession;

    // singleton
    private static LocalTaskService localTaskService;

    // singleton
    private static KnowledgeBase kbase;

    // This util creates a singleton ksession in static block. You may move this
    // to more appropriate init block.
    static {
        try {
            KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kbuilder.add(ResourceFactory.newClassPathResource("rewards-basic.bpmn"), ResourceType.BPMN2);
            kbase = kbuilder.newKnowledgeBase();

            // This class needs to be loaded outside of a transaction
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa");

            Environment env = KnowledgeBaseFactory.newEnvironment();
            env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

            ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);

            new JPAWorkingMemoryDbLogger(ksession);

            org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf,
                    SystemEventListenerFactory.getSystemEventListener());

            localTaskService = new LocalTaskService(taskService);

            SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(localTaskService, ksession);
            humanTaskHandler.setLocal(true);
            humanTaskHandler.connect();
            ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static StatefulKnowledgeSession getKnowledgeSession() {
        return ksession;
    }

    public static LocalTaskService getLocalTaskService() {
        return localTaskService;
    }

}
