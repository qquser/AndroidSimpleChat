package com.example.admin.myapplication3;

import android.os.Bundle;
import com.rabbitmq.client.*;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import android.text.method.ScrollingMovementMethod;

import android.os.Handler;
import android.os.Message;

import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity3 extends AppCompatActivity {

    Subscriber subscriber = new Subscriber();
    Publisher publisher = new Publisher();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        publisher.publishThread.interrupt();
        subscriber.subscribeThread.interrupt();
    }

    private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    void publishMessage(String message) {
        try {
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    ConnectionFactory factory= new ConnectionFactory();
    private void setupConnectionFactory() {
        String uri = "localhost";
        try {
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
        } catch (Exception  e1) {
            e1.printStackTrace();
        }
    }

    void setupPubButton() {
        Button button = (Button) findViewById(R.id.publish);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText et = (EditText) findViewById(R.id.text);
                publishMessage(et.getText().toString());
                et.setText("");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setupConnectionFactory();
        publisher.publishToAMQP("Лебедь", factory, queue);
        setupPubButton();

        final Handler incomingMessageHandler = new Handler() {
           @Override
            public void handleMessage(Message msg) {
                String message = msg.getData().getString("msg");
                TextView tv = (TextView) findViewById(R.id.textView);
                Date now = new Date();
                tv.setMovementMethod(new ScrollingMovementMethod());
                SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss");
                tv.append(ft.format(now) + ' ' + message + '\n');
            }
        };
        TextView mTextView = (TextView) findViewById(R.id.textView);
        subscriber.subscribe(incomingMessageHandler, factory, mTextView);
    }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity3, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
