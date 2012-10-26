package com.sample;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
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
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TaskBean implements TaskLocal {

    private static KnowledgeBase kbase;

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    @Resource
    private UserTransaction ut;
    
    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        kbase = readKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        TaskService localTaskService = getTaskService(ksession);

        List<TaskSummary> list = localTaskService
                .getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }
        
        ksession.dispose();

        return list;
    }

    public void approveTask(String actorId, long taskId) throws Exception {

        kbase = readKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        TaskService localTaskService = getTaskService(ksession);

        ut.begin();

        try {
            System.out.println("approveTask (taskId = " + taskId + ") by "
                    + actorId);
            localTaskService.start(taskId, actorId);
            localTaskService.complete(taskId, actorId, null);
            
            ut.commit();
        } catch (Exception e) {
            e.printStackTrace();
            ut.rollback();
            throw e;
        } finally {
            ksession.dispose();
        }


        return;
    }

    private StatefulKnowledgeSession createKnowledgeSession() {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService
                .newStatefulKnowledgeSession(kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        return ksession;
    }

    private TaskService getTaskService(StatefulKnowledgeSession ksession) {

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(
                emf, SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(
                localTaskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                humanTaskHandler);

        return localTaskService;
    }

    private KnowledgeBase readKnowledgeBase() throws Exception {

        if (kbase != null) {
            return kbase;
        }

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder();
        kbuilder.add(
                ResourceFactory.newClassPathResource("rewards-basic.bpmn"),
                ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

}
