package com.sample.hello;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.sample.callback.Callback;
import com.sample.callback.CallbackService;

@WebService
public class HelloWorldImpl implements HelloWorld {

    @WebMethod
    public String sayHello(final String toWhom, final int ksessionId, final long workItemId) {
        
        System.out.println("sayHello is invoked");
        
        // This is really ugly implementation to emulate async behaviour. You should be able to implement this in other place
        new Thread() {
            public void run() {
                // This may take long time
                System.out.println("working...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // you may use Map if you want to return multiple values 
                String result = String.format("Hello, %s!", toWhom);
                
                CallbackService service = new CallbackService();
                Callback port = service.getCallbackPort();
                port.resumeAsyncTask(ksessionId, workItemId, result);
                
                System.out.println("callback notification has finished");
            }
        }.start();
        
        // ack
        return "ack";
    }
    
    
}
