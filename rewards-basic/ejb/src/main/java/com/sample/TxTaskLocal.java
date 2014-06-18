package com.sample;

import java.util.List;

import javax.ejb.Local;

import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

@Local
public interface TxTaskLocal {
    public List<TaskSummary> retrieveTaskList(String actorId, StatefulKnowledgeSession ksession, LocalTaskService localTaskService) throws Exception;

    public void approveTask(String actorId, long taskId, StatefulKnowledgeSession ksession, LocalTaskService localTaskService) throws Exception;
}
