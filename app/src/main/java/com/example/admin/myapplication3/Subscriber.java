package com.example.admin.myapplication3;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class Subscriber {
    public static Thread subscribeThread;
    void subscribe(final Handler handler, final ConnectionFactory factory, final TextView mTextView)
    {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();
                    channel.exchangeDeclare("chatexchange", "fanout");
                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, "chatexchange", "");

                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
                            String message = new String(body, "UTF-8");
                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);


                            scrollToEndTextView(mTextView);
                        }
                    };
                    channel.basicConsume(queueName, true, consumer);

                } catch (Exception e1) {
                    try {
                        Thread.sleep(5000); //sleep and then try again
                    } catch (InterruptedException e) {
                    }
                }
            }
            // }
        });
        subscribeThread.start();
    }

    private void scrollToEndTextView(final TextView mTextView)
    {
        final int scrollAmount = mTextView.getLayout().getLineTop(mTextView.getLineCount()) - mTextView.getHeight();

        if (scrollAmount > 0)
            mTextView.scrollTo(0, scrollAmount);
        else
            mTextView.scrollTo(0, 0);
    }
}
