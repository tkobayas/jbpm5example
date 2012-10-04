package com.sample;

import java.net.MalformedURLException;
import java.net.URL;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;

import com.sample.hello.HelloWorld;
import com.sample.hello.HelloWorldImplService;

public class AsyncWSWorkItemHandler implements WorkItemHandler {
    
    private StatefulKnowledgeSession ksession;

    public void setKsession(StatefulKnowledgeSession ksession) {
        this.ksession = ksession;
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        System.out.println("AsyncWSWorkItemHandler : executeWorkItem");
        
        // you can pick up parameters from the TaskNode definition (I don't use it this time)
        String wsName = (String)workItem.getParameter("WSName");
        
        // you can pick up process variables
        ProcessInstance processInstance = ksession.getProcessInstance(workItem.getProcessInstanceId());
        String var1 = (String)((WorkflowProcessInstanceImpl)processInstance).getVariable("var1");
        
        // Important values to continue async execution
        int ksessionId = ksession.getId();
        long workItemId = workItem.getId();
        
        // call Web Service and accept immediate ack
        URL wsdlUrl = null;
        try {
            wsdlUrl = new URL("http://127.0.0.1:8080/hello/hello?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HelloWorldImplService service = new HelloWorldImplService(wsdlUrl);
        HelloWorld port = service.getHelloWorldImplPort();
        String ack = port.sayHello(var1, ksessionId, workItemId);
        
        System.out.println("AsyncWSWorkItemHandler : " + ack);
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        System.out.println("abortWorkItem");
    }
}