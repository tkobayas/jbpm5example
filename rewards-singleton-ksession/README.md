jbpm5example - rewards-singleton-ksession
============

This is a basic jBPM5 web application, which aims to provide an example usage of:
- Human Task
- Persistence
- LocalTaskService and SyncWSHumanTaskHandler
- singleton ksession pattern
- mvn building

This application is an example of 'singleton ksession pattern'. As LocalTaskService is not thread-safe (while ksession is thread-safe), each method is required to be synchronized. I have encapsulated the logic into JbpmUtil class.

### Steps to run
- Start BRMS 5.3.1 standalone
- Edit rewards-singleton-ksession/pom.xml to change <brms.library.dir> properties for your environment
- mvn clean package
- cp ear/target/rewards-singleton-ksession-ear-1.0-SNAPSHOT.ear $JBOSS_HOME/server/$PROFILE/deploy
- access to http://localhost:8080/rewards-singleton-ksession/ with a browser
 - [Start Reward Process] is to start a new process
 - [John's Task] is to list John's tasks and approve them
 - [Mary's Task] is to list Mary's tasks and approve them
 
- rewards-singleton-ksession.jmx is a jmeter test plan for this application.
 - You may see PermissionDeniedException or OptimisticLockException under load. It means that a user started a task which is already completed. It's expected because this test plan may cause concurrent accesses to the same task with the same user. (It may happen in real use cases)
