package com.sample.callback;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebService;

import com.sample.ProcessLocal;

@WebService
public class CallbackImpl implements Callback {
    
    @EJB
    private ProcessLocal processService;

    @WebMethod
    public void resumeAsyncTask(int ksessionId, long workItemId,
            String result) {
        
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("ResponseValue1", result);
        
        try {
            processService.resumeAsyncTask(ksessionId, workItemId, results);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return;
    }
}
