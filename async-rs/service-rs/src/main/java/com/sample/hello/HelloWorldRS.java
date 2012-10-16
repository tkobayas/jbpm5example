package com.sample.hello;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/")
public class HelloWorldRS {

    @POST
    @Path("/sayHello/{ksessionId}/{workItemId}")
    @Produces("text/plain")
    public String sayHello(@PathParam("ksessionId") final int ksessionId,
            @PathParam("workItemId") final long workItemId,
            @QueryParam("toWhom") final String toWhom) {

        System.out.println("HelloWorldRS : sayHello is invoked");

        // This is really ugly implementation to emulate async behaviour. You
        // should be able to implement this in other place
        new Thread() {
            public void run() {
                // This may take long time
                System.out.println("HelloWorldRS : working...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // you may use Map if you want to return multiple values
                String result = String.format("Hello, %s!", toWhom);
                
                System.out.println("HelloWorldRS : " + result);

                // invoke callback rs
                int responseCode = -1;
                try {
                    URL url = new URL(
                            "http://127.0.0.1:8080/async-rs-callback/resumeAsyncTask/"
                                    + ksessionId + "/" + workItemId
                                    + "?result=" + URLEncoder.encode(result, "UTF-8"));
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setRequestMethod("POST");
                    conn.connect();
                    responseCode = conn.getResponseCode();
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                System.out
                        .println("HelloWorldRS : callback notification has finished. responseCode = "
                                + responseCode);
            }
        }.start();

        // ack
        return "ack";
    }

}
