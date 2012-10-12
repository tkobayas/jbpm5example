jbpm5example - rewards-basic
============

This is a basic jBPM5 web application, which aims to provide an example usage of:
- Human Task
- Persistence
- LocalTaskService and SyncWSHumanTaskHandler
- mvn building

Currently these projects are focusing on JBoss BRMS 5.3.0. I may add some branches for other versions (e.g. jBPM 5.3.0)

For repository setup, please refer to https://github.com/tkobayas/jbpm5example/blob/master/README.md
* BUT * I have removed the local repository dependency for BRMS jars from this project. There are referenced by <systemPath> now.

### Steps to run
- Edit $JBOSS_HOME/server/$PROFILE/deploy/jbpm-human-task.war/WEB-INF/web.xml to add sample users/groups for Human Task Service

```
      <init-param>
        <param-name>load.users</param-name>
        <!-- <param-value>classpath:/org/jbpm/task/servlet/DefaultUsers.mvel</param-value> -->
        <param-value>classpath:/org/jbpm/task/servlet/SampleUsers.mvel</param-value>
      </init-param>
      <!-- use classpath:/org/jbpm/task/servlet/SampleGroups.mvel to configure sample users for demo purpose-->
      <init-param>
        <param-name>load.groups</param-name>
        <!-- <param-value></param-value> -->
        <param-value>classpath:/org/jbpm/task/servlet/SampleGroups.mvel</param-value>
      </init-param>
```

- Start BRMS 5.3.0 standalone
- Edit rewards-basic/pom.xml to change <jbossas.home> and <brms.library.dir> properties for your environment
- mvn clean package
- cp ear/target/rewards-basic-ear-1.0-SNAPSHOT.ear $JBOSS_HOME/server/$PROFILE/deploy
- access to http://localhost:8080/rewards-basic/ with a browser
 - [Start Reward Process] is to start a new process
 - [John's Task] is to list John's tasks and approve them
 - [Mary's Task] is to list Mary's tasks and approve them
