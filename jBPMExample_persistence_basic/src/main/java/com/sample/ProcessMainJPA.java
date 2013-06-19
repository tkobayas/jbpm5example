package com.sample;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.KnowledgeBase;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.DefaultUserGroupCallbackImpl;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.UserGroupCallbackManager;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.test.JBPMHelper;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * This is a sample file to launch a process.
 */
public class ProcessMainJPA {

    private static EntityManagerFactory emf;

    public static final void main(String[] args) throws Exception {
        
        setup();
        
        // load up the knowledge base
        KnowledgeBase kbase = readKnowledgeBase();
        
        // create ksession/localTaskService
        StatefulKnowledgeSession ksession = newStatefulKnowledgeSession(kbase);
        LocalTaskService localTaskService = getTaskServiceAndRegisterHumanTaskHandler(ksession);
        
        // start a new process instance
        Map<String,Object> params = new HashMap<String, Object>();
        ProcessInstance pi = ksession.startProcess("com.sample.bpmn.hello", params);
        System.out.println("A process instance started : pid = " + pi.getId());

        // ------------
        {
            List<TaskSummary> list = localTaskService.getTasksAssignedAsPotentialOwner("john", "en-UK");
            for (TaskSummary taskSummary : list) {
                System.out.println("john starts a task : taskId = " + taskSummary.getId());
                localTaskService.start(taskSummary.getId(), "john");
                localTaskService.complete(taskSummary.getId(), "john", null);
            }
        }

        // -----------
        {
            List<TaskSummary> list = localTaskService.getTasksAssignedAsPotentialOwner("mary", "en-UK");
            for (TaskSummary taskSummary : list) {
                System.out.println("mary starts a task : taskId = " + taskSummary.getId());
                localTaskService.start(taskSummary.getId(), "mary");
                localTaskService.complete(taskSummary.getId(), "mary", null);
            }
        }

        // -----------
        ksession.dispose();
        
        System.exit(0);
    }

    private static KnowledgeBase readKnowledgeBase() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("sample.bpmn"), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

    private static void setup() {
        // for H2
        JBPMHelper.startH2Server();
        JBPMHelper.setupDataSource();
        
        // for your DB
//        setupDataSource();
        
        UserGroupCallbackManager.getInstance().setCallback(new DefaultUserGroupCallbackImpl());
        
        Map<String, String> map = new HashMap<String, String>();
        emf = Persistence.createEntityManagerFactory("org.jbpm.persistence.jpa", map);
    }
    
    public static PoolingDataSource setupDataSource() {
        // Please edit here when you want to use your database
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "mysql");
        pds.getDriverProperties().put("password", "mysql");
        pds.getDriverProperties().put("url", "jdbc:mysql://localhost:3306/testbrms531");
        pds.getDriverProperties().put("driverClassName", "com.mysql.jdbc.Driver");
        pds.init();
        return pds;
    }

    public static StatefulKnowledgeSession newStatefulKnowledgeSession(KnowledgeBase kbase) {
        return loadStatefulKnowledgeSession(kbase, -1);
    }

    public static StatefulKnowledgeSession loadStatefulKnowledgeSession(KnowledgeBase kbase, int sessionId) {
        StatefulKnowledgeSession ksession;
        Environment env = EnvironmentFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
        env.set(EnvironmentName.TRANSACTION_MANAGER, TransactionManagerServices.getTransactionManager());

        if (sessionId == -1) {
            ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        } else {
            ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
        }

        return ksession;
    }

    private static LocalTaskService getTaskServiceAndRegisterHumanTaskHandler(StatefulKnowledgeSession ksession) {
        TaskService taskeService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        LocalTaskService localTaskService = new LocalTaskService(taskeService);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(localTaskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);
        return localTaskService;
    }
}