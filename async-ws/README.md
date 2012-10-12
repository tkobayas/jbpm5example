jbpm5example - async-ws
============

This is a jBPM5 web application, which aims to provide an example usage of:
- Asyncronous Task
- Callback Web Service which resumes the Task
- External Web Service which represents Async job
- Both Web Services are based on jax-ws

service-ws and callback-ws construct cyclic dependency which may annoy you in maven build. I'll surely recreate this with jax-rs.

### Steps to run
- Edit async-ws/pom.xml and async-ws/service-ws/pom.xml to change <jbossas.home> and <brms.library.dir> properties for your environment
- cd service-ws
- mvn clean install
- cd ..
- mvn clean install
- cd service-ws
- Edit pom.xml like the following:

    <dependency>
      <groupId>org.example</groupId>
      <artifactId>async-ws-callback-ws</artifactId>
      <version>1.0-SNAPSHOT</version>

      <!-- for the first build -->
      <!-- <scope>system</scope> -->
      <!-- <systemPath>${basedir}/lib/async-ws-callback-ws-1.0-SNAPSHOT-classes.jar</systemPath> -->

      <!-- since the second build -->
      <classifier>classes</classifier>

    </dependency>

- mvn clean install
- Deploy target/async-ws-service-ws-1.0-SNAPSHOT.war
- cd ..
- Deploy ear/target/async-ws-ear-1.0-SNAPSHOT.ear

### Description
The async-ws project is consist of the following sub-projects.

- ejb
 -> The main component that executes jBPM. Only this component depends on jBPM libraries.

- ui-war
 -> Servlet/JSP for the web UI. Just used to start the application.

- callback-ws
 -> A Web Service which accepts the result from async service and resume the task/process

- ear
 -> Builds an EAR which contains the above modules

- service-ws
 ->  A Web Service which represents async service. It is independent from the above project except callback-ws client.

The important parts are:

- async-ws/ejb/src/main/resources/defaultPackage.AsyncWS.bpmn2
 -> The process definition. It has only custom AsyncWS task node and Script node for simplicity. You don't need Signal this time.

- async-ws/ejb/src/main/java/com/sample/ProcessBean.java
 -> The main logic around jBPM execution.

- async-ws/ejb/src/main/java/com/sample/AsyncWSWorkItemHandler.java
 -> The custom WorkItemHandler. You may implement it as you like for your async task node.

- async-ws/callback-ws/src/main/java/com/sample/callback/CallbackImpl.java
 -> Not very special one. If you want to use Web Service for callback, the implementation would be like this. If you want to use JMS, it would be a MessageDrivenBean.

> async-ws/service-ws/src/main/java/com/sample/hello/HelloWorldImpl.java
 -> To emulate async behaviour, I created a Thread as a quick hack but of course you will implement the business logic in different way.
