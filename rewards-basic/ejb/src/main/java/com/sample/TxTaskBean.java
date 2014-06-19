package com.sample;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.drools.runtime.StatefulKnowledgeSession;
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

        try {
            System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
            localTaskService.start(taskId, actorId);
            localTaskService.complete(taskId, actorId, null);
        } catch (RuntimeException re) {
            // BZ1082128 you want to log RuntimeException here just in case
            re.printStackTrace();
            throw re;
        }

        return;
    }

}
