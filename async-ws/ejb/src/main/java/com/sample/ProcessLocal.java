package com.sample;

import java.util.Map;

import javax.ejb.Local;

@Local
public interface ProcessLocal
{
    public long startProcess() throws Exception;
    
    public void resumeAsyncTask(int ksessionId, long workItemId,
            Map<String, Object> results) throws Exception;
}
