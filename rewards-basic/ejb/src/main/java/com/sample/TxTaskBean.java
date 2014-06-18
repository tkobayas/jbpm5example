package com.sample;

import java.util.List;

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
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TxTaskBean implements TxTaskLocal {

    public List<TaskSummary> retrieveTaskList(String actorId, StatefulKnowledgeSession ksession,
            LocalTaskService localTaskService) throws Exception {

        List<TaskSummary> list = localTaskService.getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }

        return list;
    }

    public void approveTask(String actorId, long taskId, StatefulKnowledgeSession ksession,
            LocalTaskService localTaskService) throws Exception {

        System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
        localTaskService.start(taskId, actorId);
        localTaskService.complete(taskId, actorId, null);

        return;
    }

}
