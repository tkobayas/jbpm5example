package com.sample;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
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
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.PeopleAssignments;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.User;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.local.LocalTaskService;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class TaskBean implements TaskLocal {

    private static KnowledgeBase kbase;

    @PersistenceUnit(unitName = "org.jbpm.persistence.jpa")
    private EntityManagerFactory emf;

    @Resource
    private UserTransaction ut;

    public List<TaskSummary> retrieveTaskList(String actorId) throws Exception {

        kbase = readKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession();
        LocalTaskService localTaskService = getTaskService(ksession);

        List<TaskSummary> list = localTaskService.getTasksAssignedAsPotentialOwner(actorId, "en-UK");

        System.out.println("retrieveTaskList by " + actorId);
        for (TaskSummary task : list) {
            System.out.println(" task.getId() = " + task.getId());
        }

        ksession.dispose();

        return list;
    }

    public void approveTask(String actorId, long taskId) throws Exception {

        kbase = readKnowledgeBase();
        StatefulKnowledgeSession ksession = null;

        try {
            ut.begin();
            
            ksession = createKnowledgeSession();
            LocalTaskService localTaskService = getTaskService(ksession);
            System.out.println("approveTask (taskId = " + taskId + ") by " + actorId);
            
            Task task = localTaskService.getTask(taskId);
            
            if (task.getTaskData().getStatus() == Status.Ready) {
                // Claim first
                localTaskService.claim(taskId, actorId);
            }
            
            localTaskService.start(taskId, actorId);
            localTaskService.complete(taskId, actorId, null);

            ut.commit();
        } catch (Exception e) {
            e.printStackTrace();
            ut.rollback();
            throw e;
        } finally {
            if (ksession != null) {
                ksession.dispose();
            }
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
    
    private LocalTaskService getTaskService() {
        
        // Use this method when you don't need ksession

        org.jbpm.task.service.TaskService taskService = new org.jbpm.task.service.TaskService(emf,
                SystemEventListenerFactory.getSystemEventListener());

        LocalTaskService localTaskService = new LocalTaskService(taskService);

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

    public void setNewPotentialOwners(long taskId) throws Exception {
 
        try {
            ut.begin();
            
            LocalTaskService localTaskService = getTaskService();
            Task task = localTaskService.getTask(taskId);
            List<OrganizationalEntity> newPotentialOwners = new ArrayList<OrganizationalEntity>();
            PeopleAssignments pa = task.getPeopleAssignments();
            System.out.println("setNewPotentialOwners : old = " + pa.getPotentialOwners());
            //newPotentialOwners.add(new User("Jabba Hutt"));
            newPotentialOwners.add(new User("mary"));
            pa.setPotentialOwners(newPotentialOwners);
            
            if (newPotentialOwners.size() == 1) {
                // if there is a single potential owner, assign and set status to Reserved
                OrganizationalEntity potentialOwner = newPotentialOwners.get(0);
                // if there is a single potential user owner, assign and set status to Reserved
                if (potentialOwner instanceof User) {
                    task.getTaskData().setActualOwner((User) potentialOwner);

                    task.getTaskData().setStatus(Status.Reserved);
                }
                //If there is a group set as potentialOwners, set the status to Ready ??
                if (potentialOwner instanceof Group) {

                    task.getTaskData().setStatus(Status.Ready);
                }
            } else if (newPotentialOwners.size() > 1) {
                // multiple potential owners, so set to Ready so one can claim.
                task.getTaskData().setStatus(Status.Ready);
            } else {
                throw new RuntimeException("You should have potential owners");
            }
            
            System.out.println("setNewPotentialOwners : new = " + pa.getPotentialOwners());

            ut.commit();
            System.out.println("--- committed ---");
        } catch (Exception e) {
            e.printStackTrace();
            ut.rollback();
            throw e;
        }
        
        {
            LocalTaskService localTaskService2 = getTaskService();
            System.out.println("--- querying ---");
            Task task = localTaskService2.getTask(taskId);
            System.out.println("task.getTaskData().getStatus() = " + task.getTaskData().getStatus());
            System.out.println("setNewPotentialOwners : confirm = " + task.getPeopleAssignments().getPotentialOwners());
        }

        return;
    }
}