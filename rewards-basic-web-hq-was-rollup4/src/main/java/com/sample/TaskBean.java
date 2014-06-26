package com.sample;

import java.util.List;

import org.jbpm.task.query.TaskSummary;

public class TaskBean {

    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        List<TaskSummary> list = JbpmUtil.getReservedTaskList(actorId);

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }

        return list;
    }

    public void approveTask(String actorId, long taskId) throws Exception {
        
        System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);

        JbpmUtil.startAndCompleteTask(actorId, taskId);
    }

}
