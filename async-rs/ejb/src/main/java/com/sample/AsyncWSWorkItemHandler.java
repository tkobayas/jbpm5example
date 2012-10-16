package com.sample;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;

public class AsyncWSWorkItemHandler implements WorkItemHandler {

    private StatefulKnowledgeSession ksession;

    public void setKsession(StatefulKnowledgeSession ksession) {
        this.ksession = ksession;
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        System.out.println("AsyncWSWorkItemHandler : executeWorkItem");

        // you can pick up parameters from the TaskNode definition (I don't use
        // it this time)
        String wsName = (String) workItem.getParameter("WSName");

        // you can pick up process variables
        ProcessInstance processInstance = ksession.getProcessInstance(workItem
                .getProcessInstanceId());
        String var1 = (String) ((WorkflowProcessInstanceImpl) processInstance)
                .getVariable("var1");

        // Important values to continue async execution
        int ksessionId = ksession.getId();
        long workItemId = workItem.getId();

        // call Web Service and accept immediate ack
        int responseCode = -1;
        try {
            URL url = new URL(
                    "http://127.0.0.1:8080/async-rs-hello/sayHello/"
                            + ksessionId + "/" + workItemId
                            + "?toWhom=" + var1);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.connect();
            responseCode = conn.getResponseCode();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("AsyncWSWorkItemHandler : responseCode = "
                + responseCode);
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        System.out.println("abortWorkItem");
    }
}