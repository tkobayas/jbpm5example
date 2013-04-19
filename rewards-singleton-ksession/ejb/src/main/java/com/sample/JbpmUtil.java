package com.sample;

import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import javax.transaction.RollbackException;
import javax.transaction.Status;
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
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.DefaultUserGroupCallbackImpl;
import org.jbpm.task.service.PermissionDeniedException;
import org.jbpm.task.service.UserGroupCallbackManager;
import org.jbpm.task.service.local.LocalTaskService;

public class JbpmUtil {

    // singleton
    private static StatefulKnowledgeSession ksession;

    // singleton
    private static LocalTaskService localTaskService;

    // singleton
    private static KnowledgeBase kbase;

    // singleton
    private static UserTransaction ut;

    // This util creates a singleton ksession in static block. You may move this
    // to more appropriate init block.
    static {
        try {
            // Use this when you want to ignore user existence issues
            UserGroupCallbackManager.getInstance().setCallback(new DefaultUserGroupCallbackImpl());

            InitialContext ic = new InitialContext();
            ut = (UserTransaction) ic.lookup("UserTransaction");

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

    public static synchronized long startProcess(String processId, Map<String, Object> params) throws Exception {

        long processInstanceId = -1;

        try {
            ut.begin();

            ProcessInstance processInstance = ksession.startProcess(processId, params);

            processInstanceId = processInstance.getId();

            ut.commit();
        } catch (Exception e) {
            e.printStackTrace();
            ut.rollback();
            throw e;
        } finally {
            // don't dispose
        }

        return processInstanceId;
    }

    public static synchronized List<TaskSummary> getReservedTaskList(String actorId) {
        List<TaskSummary> list = null;

        list = localTaskService.getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        return list;
    }

    public static synchronized void startAndCompleteTask(String actorId, long taskId) throws Exception {

        try {
            ut.begin();

            localTaskService.start(taskId, actorId);
            localTaskService.complete(taskId, actorId, null);

            // Thread.sleep(10000); // To test OptimisticLockException

            ut.commit();
        } catch (RollbackException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof OptimisticLockException) {
                // Concurrent access to the same process instance
                throw new ProcessOperationException("The same process instance has likely been accessed concurrently",
                        e);
            }
            throw new RuntimeException(e);
        } catch (PermissionDeniedException e) {
            e.printStackTrace();
            // Transaction might be already rolled back by
            // TaskServiceSession
            if (ut.getStatus() == Status.STATUS_ACTIVE) {
                ut.rollback();
            }
            // Probably the task has already been started by other users
            throw new ProcessOperationException("The task (id = " + taskId
                    + ") has likely been started by other users ", e);
        } catch (Exception e) {
            e.printStackTrace();
            // Transaction might be already rolled back by
            // TaskServiceSession
            if (ut.getStatus() == Status.STATUS_ACTIVE) {
                ut.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            // don't dispose
        }
    }
}
