package com.sample;

import java.util.List;

import javax.ejb.Local;

import org.jbpm.task.query.TaskSummary;


@Local
public interface TaskLocal
{
    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception;
    public void approveTask(String actorId, long taskId) throws Exception;
    public void approveAllTasks() throws Exception;
}
