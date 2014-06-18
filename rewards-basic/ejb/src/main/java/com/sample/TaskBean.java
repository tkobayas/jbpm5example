package com.sample;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
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
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

@Stateless
public class TaskBean implements TaskLocal {

    private static KnowledgeBase kbase;

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    @EJB
    private TxTaskLocal txTaskBean;
    
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        kbase = readKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        LocalTaskService localTaskService = getTaskService(ksession);

        List<TaskSummary> list = txTaskBean.retrieveTaskList(actorId, ksession, localTaskService);

        ksession.dispose();
        localTaskService.dispose();

        return list;
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void approveTask(String actorId, long taskId) throws Exception {

        kbase = readKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        LocalTaskService localTaskService = getTaskService(ksession);

        try {
            txTaskBean.approveTask(actorId, taskId, ksession, localTaskService);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ksession.dispose();
            localTaskService.dispose();
        }

        return;
    }

    private StatefulKnowledgeSession createKnowledgeSession() {
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);

        StatefulKnowledgeSession ksession = JPAKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);

        new JPAWorkingMemoryDbLogger(ksession);

        return ksession;
    }

    private LocalTaskService getTaskService(StatefulKnowledgeSession ksession) {

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

        SyncWSHumanTaskHandler humanTaskHandler = new SyncWSHumanTaskHandler(localTaskService, ksession);
        humanTaskHandler.setLocal(true);
        humanTaskHandler.connect();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        return localTaskService;
    }

    private KnowledgeBase readKnowledgeBase() throws Exception {

        if (kbase != null) {
            return kbase;
        }

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("rewards-basic.bpmn"), ResourceType.BPMN2);
        return kbuilder.newKnowledgeBase();
    }

}
