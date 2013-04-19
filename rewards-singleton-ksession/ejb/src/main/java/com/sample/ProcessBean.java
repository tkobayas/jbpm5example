package com.sample;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class ProcessBean implements ProcessLocal {

    public long startProcess(String recipient) throws Exception {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("recipient", recipient);
        long processInstanceId = JbpmUtil.startProcess("com.sample.rewards-basic", params);
        
        System.out.println("Process started ... : processInstanceId = " + processInstanceId);

        return processInstanceId;
    }
}
