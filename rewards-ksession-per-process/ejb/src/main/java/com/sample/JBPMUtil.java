package com.sample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

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
import org.jbpm.task.service.DefaultUserGroupCallbackImpl;
import org.jbpm.task.service.UserGroupCallbackManager;
import org.jbpm.task.service.local.LocalTaskService;

/**
 * 
 * Static jBPM util. Making sure to use the same ksession & localTaskService for
 * the same process instance.
 * 
 */
public class JBPMUtil {

    private static Map<Long, Integer> processSessionIdMap = new HashMap<Long, Integer>();
    private static Map<Integer, LocalTaskService> sessionIdLocalTaskServiceMap = new HashMap<Integer, LocalTaskService>();

    private static Set<Integer> inUseSessionIdMap = new HashSet<Integer>();

    private static KnowledgeBase kbase;

    static {
        // Use this when you want to ignore user existence issues
        UserGroupCallbackManager.getInstance().setCallback(new DefaultUserGroupCallbackImpl());
    }

    public static int getSessionIdByProcessInstanceId(long processInstanceId) {
        System.out.println(processInstanceId);
        int sessionId = processSessionIdMap.get(processInstanceId);
        return sessionId;
    }

    public static void putSessionId(long processInstanceId, int sessionId) {
        processSessionIdMap.put(processInstanceId, sessionId);
    }

    public static StatefulKnowledgeSession createKnowledgeSession(EntityManagerFactory emf) throws Exception {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(readKnowledgeBase(), null,
                env);

        new JPAWorkingMemoryDbLogger(ksession);

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(localTaskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        sessionIdLocalTaskServiceMap.put(ksession.getId(), localTaskService);

        inUseSessionIdMap.add(ksession.getId());

        return ksession;
    }

    public static StatefulKnowledgeSession getSession(int sessionId, EntityManagerFactory emf) throws Exception {
        for (int i = 0; i < 5; i++) {
            synchronized (inUseSessionIdMap) {
                if (!inUseSessionIdMap.contains(sessionId)) {
                    StatefulKnowledgeSession ksession = loadKnowledgeSession(sessionId, emf);
                    inUseSessionIdMap.add(sessionId);
                    return ksession;
                }
                inUseSessionIdMap.wait();
            }
        }

        throw new ProcessOperationException("Cannot aquire ksession: sessionId = " + sessionId);
    }

    public static StatefulKnowledgeSession loadKnowledgeSession(int sessionId, EntityManagerFactory emf)
            throws Exception {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId,
                readKnowledgeBase(), null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(localTaskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        sessionIdLocalTaskServiceMap.put(ksession.getId(), localTaskService);

        return ksession;
    }

    /**
     * returns a localTaskService which is tied to a ksession
     */
    public static LocalTaskService getLocalTaskServiceBySessionId(int sessionId) {

        LocalTaskService localTaskService = sessionIdLocalTaskServiceMap.get(sessionId);

        if (localTaskService == null) {
            throw new RuntimeException("sessionIdLocalTaskServiceMap doesn't contain localTaskService for " + sessionId
                    + ". Call loadKnowledgeSession() first");
        }

        return localTaskService;
    }

    /**
     * creates a localTaskService which is not tied to a ksession
     */
    public static LocalTaskService createLocalTaskService(EntityManagerFactory emf) {

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

        return localTaskService;
    }

    public static KnowledgeBase readKnowledgeBase() throws Exception {

        if (kbase != null) {
            return kbase;
        }

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("rewards-basic.bpmn"), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    public static void disposeSession(StatefulKnowledgeSession ksession) {
        synchronized (inUseSessionIdMap) {
            int sessionId = ksession.getId();
            ksession.dispose();
            inUseSessionIdMap.remove(sessionId);
            inUseSessionIdMap.notifyAll();
        }
    }
}
