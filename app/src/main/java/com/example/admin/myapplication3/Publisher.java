package com.example.admin.myapplication3;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.BlockingDeque;

public class Publisher {
    public static Thread publishThread;
    public void publishToAMQP(final String name, final ConnectionFactory factory,  final BlockingDeque<String> queue)
    {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //while(true) {
                try {
                    Connection connection = factory.newConnection();
                    Channel ch = connection.createChannel();
                    ch.confirmSelect();
                    ch.exchangeDeclare("chatexchange", "fanout");

                    while (true) {
                        String message =name+ ": " + queue.takeFirst();
                        try{
                            ch.basicPublish("chatexchange", "", null, message.getBytes());
                            ch.waitForConfirmsOrDie();
                        } catch (Exception e){
                            queue.putFirst(message);
                            throw e;
                        }
                    }
                } catch (InterruptedException e) {
                } catch (Exception e) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {

                    }
                }
            }
            //}
        });
        publishThread.start();
    }
}
