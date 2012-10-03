package com.sample;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;

import com.sample.hello.HelloWorld;
import com.sample.hello.HelloWorldService;

public class AsyncWSWorkItemHandler implements WorkItemHandler {
    
    private StatefulKnowledgeSession ksession;

    public void setKsession(StatefulKnowledgeSession ksession) {
        this.ksession = ksession;
    }

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        System.out.println("executeWorkItem");
        
        // you can pick up parameters from the TaskNode definition (I don't use it this time)
        String wsName = (String)workItem.getParameter("WSName");
        
        // you can pick up process variables
        ProcessInstance processInstance = ksession.getProcessInstance(workItem.getProcessInstanceId());
        String myVariable = (String)((WorkflowProcessInstanceImpl)processInstance).getVariable("myVariable");
        
        // Important values to continue async execution
        int ksessionId = ksession.getId();
        long workItemId = workItem.getId();
        
        // call Web Service
        HelloWorldService service = new HelloWorldService();
        HelloWorld port = service.getHelloWorldPort();
        String ack = port.sayHello(myVariable, ksessionId, workItemId);
        
        System.out.println(ack);
    }

    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        System.out.println("abortWorkItem");
    }
}