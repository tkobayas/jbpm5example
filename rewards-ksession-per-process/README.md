jbpm5example - rewards-ksession-per-process
============

This is a basic jBPM5 web application, which aims to provide an example usage of:
- Human Task
- Persistence
- LocalTaskService and SyncWSHumanTaskHandler
- ksession per process pattern
- mvn building

This application is an example of 'ksession per process pattern'.

Currently these projects are focusing on JBoss BRMS 5.3.1.

For repository setup, please refer to https://github.com/tkobayas/jbpm5example/blob/master/README.md
* BUT * I have removed the local repository dependency for BRMS jars from this project. There are referenced by <systemPath> now. So you don't have to set up repository.

### Steps to run
- Start BRMS 5.3.1 standalone
- Edit rewards-ksession-per-process/pom.xml to change <brms.library.dir> properties for your environment
- mvn clean package
- cp ear/target/rewards-ksession-per-process-ear-1.0-SNAPSHOT.ear $JBOSS_HOME/server/$PROFILE/deploy
- access to http://localhost:8080/rewards-ksession-per-process/ with a browser
 - [Start Reward Process] is to start a new process
 - [John's Task] is to list John's tasks and approve them
 - [Mary's Task] is to list Mary's tasks and approve them
 
- reward-basic.jmx is a jmeter test plan for this application.
 - You may see PermissionDeniedException or OptimisticLockException under load. It means that a user started a task which is already completed. It's expected because this test plan may cause concurrent accesses to the same task with the same user. (It may happen in real use cases)
 - You may see "Caused by: org.hibernate.exception.ConstraintViolationException: Could not execute JDBC batch update" at the first stage of load test because of concurrent initialization of users (john/mary). Please run with single thread at first.

