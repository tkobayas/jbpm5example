package com.sample;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.UserTransaction;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.jbpm.task.TaskService;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ProcessBean implements ProcessLocal {

    @Resource
    private UserTransaction ut;

    public long startProcess(String recipient) throws Exception {

        StatefulKnowledgeSession ksession = JbpmUtil.getKnowledgeSession();

        TaskService localTaskService = JbpmUtil.getLocalTaskService();

        long processInstanceId = -1;
        
        synchronized (localTaskService) {

            try {
                ut.begin();

                // start a new process instance
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("recipient", recipient);
                ProcessInstance processInstance = ksession.startProcess("com.sample.rewards-basic", params);

                processInstanceId = processInstance.getId();

                System.out.println("Process started ... : processInstanceId = " + processInstanceId);

                ut.commit();
            } catch (Exception e) {
                e.printStackTrace();
                ut.rollback();
                throw e;
            } finally {
                // don't dispose
            }
        }

        return processInstanceId;
    }
}
