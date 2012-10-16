jbpm5example - async-rs
============

This is a jBPM5 web application, which aims to provide an example usage of:
- Asyncronous Task
- Callback Rest Service which resumes the Task
- External Rest Service which represents Async job
- Both Web Services are based on jax-rs

### Steps to run
- Edit async-rs/pom.xml to change <brms.library.dir> properties for your environment
- mvn clean install
- Deploy service-rs/target/async-rs-service-rs-1.0-SNAPSHOT.war
- Deploy ear/target/async-rs-ear-1.0-SNAPSHOT.ear

### Description
The async-rs project is consist of the following sub-projects.

- ejb
 -> The main component that executes jBPM. Only this component depends on jBPM libraries.

- ui-war
 -> Servlet/JSP for the web UI. Just used to start the application.

- callback-rs
 -> A Rest Service which accepts the result from async service and resume the task/process

- ear
 -> Builds an EAR which contains the above modules

- service-rs
 ->  A Rest Service which represents async service. It is independent from the above project.

The important parts are:

- async-rs/ejb/src/main/resources/defaultPackage.AsyncWS.bpmn2
 -> The process definition. It has only custom AsyncWS task node and Script node for simplicity.

- async-rs/ejb/src/main/java/com/sample/ProcessBean.java
 -> The main logic around jBPM execution.

- async-rs/ejb/src/main/java/com/sample/AsyncWSWorkItemHandler.java
 -> The custom WorkItemHandler. You may implement it as you like for your async task node.

- async-rs/callback-rs/src/main/java/com/sample/callback/CallbackRS.java
 -> If you want to use Rest Service for callback, the implementation would be like this. If you want to use JMS, it would be a MessageDrivenBean.

> async-rs/service-rs/src/main/java/com/sample/hello/HelloWorldRS.java
 -> To emulate async behaviour, I created a Thread as a quick hack but of course you will implement the business logic in different way.
