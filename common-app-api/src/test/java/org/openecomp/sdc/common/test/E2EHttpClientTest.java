package org.openecomp.sdc.common.test;

import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.common.http.client.api.HttpExecuteException;
import org.openecomp.sdc.common.http.client.api.HttpRequest;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.openecomp.sdc.common.http.config.HttpClientConfig;
import org.openecomp.sdc.common.http.config.Timeouts;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class E2EHttpClientTest {

    @Ignore
    @Test
    public void testSsl() throws MalformedURLException {

        String url = "https://135.76.210.29:2443/certificate-manager-fe/v1";
//        String url = "http://135.76.123.110:1111//aai/v1/aai/cloud-infrastructure/operational-environments/operational-environment/12345";
        try {
            HttpClientConfig httpClientConfig = new HttpClientConfig(new Timeouts(10000, 5000));

            HttpResponse<String> response = HttpRequest.get(url, httpClientConfig);
            System.out.println(response);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
    @Ignore
    @Test
    public void testSslMutliThread() throws MalformedURLException {

          String url = "https://135.76.210.29:2443/certificate-manager-fe/v1";
//          String url = "http://135.76.210.29:2080/certificate-manager-fe/v1";
          String url2 = "http://135.76.123.110:1111//aai/v1/aai/cloud-infrastructure/operational-environments/operational-environment/12345";

        HttpClientConfig httpClientConfig = new HttpClientConfig(new Timeouts(1000, 5000));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    int count = 10;
                    try {
                        int i = 0;
                        while (i < count) {
                            if(i%2==0) {
                                HttpResponse<String> response = HttpRequest.get(url, httpClientConfig);
                                System.out.println("Thead id=" + Thread.currentThread() + " Count = " + ++i + " " + response);
                            }
                            else {
                                HttpResponse<String> response = HttpRequest.get(url2, httpClientConfig);
                                System.out.println("Thead id=" + Thread.currentThread() + " Count = " + ++i + " " + response);
                            }
                        }
                    }
                    catch (HttpExecuteException e) {
                        e.printStackTrace();
                    }
                }
            };
            executor.execute(worker);
        }

        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        while (!executor.isTerminated())
            ;
    }
}
