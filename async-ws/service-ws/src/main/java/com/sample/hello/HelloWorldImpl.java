package com.sample.hello;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jws.WebMethod;
import javax.jws.WebService;

import com.sample.callback.Callback;
import com.sample.callback.CallbackImplService;

@WebService
public class HelloWorldImpl implements HelloWorld {

    @WebMethod
    public String sayHello(final String toWhom, final int ksessionId, final long workItemId) {
        
        System.out.println("HelloWorldImpl : sayHello is invoked");
        
        // This is really ugly implementation to emulate async behaviour. You should be able to implement this in other place
        new Thread() {
            public void run() {
                // This may take long time
                System.out.println("HelloWorldImpl : working...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // you may use Map if you want to return multiple values 
                String result = String.format("Hello, %s!", toWhom);
                
                // invoke callback ws
                URL wsdlUrl = null;
                try {
                    wsdlUrl = new URL("http://127.0.0.1:8080/async-ws-callback/callback?wsdl");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                CallbackImplService service = new CallbackImplService(wsdlUrl);
                Callback port = service.getCallbackImplPort();
                port.resumeAsyncTask(ksessionId, workItemId, result);
                
                System.out.println("HelloWorldImpl : callback notification has finished");
            }
        }.start();
        
        // ack
        return "ack";
    }
    
    
}
