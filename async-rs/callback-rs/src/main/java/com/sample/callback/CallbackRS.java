package com.sample.callback;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.sample.ProcessLocal;

@Path("/")
public class CallbackRS {

    private static ProcessLocal processService;

    @POST
    @Path("/resumeAsyncTask/{ksessionId}/{workItemId}")
    @Produces("text/plain")
    public String resumeAsyncTask(@PathParam("ksessionId") int ksessionId,
            @PathParam("workItemId") long workItemId,
            @QueryParam("result") String result) {

        System.out.println("CallbackRS : " + result);
        
        ProcessLocal processService = getProcessService();

        Map<String, Object> results = new HashMap<String, Object>();
        results.put("ResponseValue1", result);

        try {
            System.out.println(processService);
            processService.resumeAsyncTask(ksessionId, workItemId, results);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "ok";
    }

    private ProcessLocal getProcessService() {
        if (processService == null) {
            try {
                InitialContext ctx = new InitialContext();
                processService = (ProcessLocal) ctx
                        .lookup("jnp://127.0.0.1:1099/async-rs-ear-1.0-SNAPSHOT/ProcessBean/local");
            } catch (NamingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return processService;
    }
}
