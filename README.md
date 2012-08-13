jbpm5example
============

jBPM5 example projects

Currently these projects are focusing on JBoss BRMS 5.3.0. I may add some branches for other versions (e.g. jBPM 5.3.0)

### BRMS 5.3.0 repository setup
As JBoss BRMS 5.3.0 libraries are not hosted in public repositories, please follow the steps to install jars into your local repository in order to run mvn successfully.
- extract the brms-p-5.3.0.GA-deployable.zip
- cd brms-p-5.3.0.GA-deployable/jboss-brms-engine/binaries/
- Run the following commands:

```
mvn install:install-file -Dfile=knowledge-api-5.3.0.BRMS.jar -DgroupId=org.drools -DartifactId=knowledge-api -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=drools-core-5.3.0.BRMS.jar -DgroupId=org.drools -DartifactId=drools-core -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=drools-compiler-5.3.0.BRMS.jar -DgroupId=org.drools -DartifactId=drools-compiler -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=drools-decisiontables-5.3.0.BRMS.jar -DgroupId=org.drools -DartifactId=drools-decisiontables -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=drools-templates-5.3.0.BRMS.jar -DgroupId=org.drools -DartifactId=drools-templates -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=drools-persistence-jpa-5.3.0.BRMS.jar -DgroupId=org.drools -DartifactId=drools-persistence-jpa -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-flow-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-flow -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-flow-builder-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-flow-builder -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-bam-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-bam -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-bpmn2-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-bpmn2 -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-human-task-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-human-task -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-persistence-jpa-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-persistence-jpa -Dversion=5.3.0.BRMS -Dpackaging=jar
mvn install:install-file -Dfile=jbpm-workitems-5.3.0.BRMS.jar -DgroupId=org.jbpm -DartifactId=jbpm-workitems -Dversion=5.3.0.BRMS -Dpackaging=jar
```

