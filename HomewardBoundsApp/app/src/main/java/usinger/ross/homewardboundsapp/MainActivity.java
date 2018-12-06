package usinger.ross.homewardboundsapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {
    String stopCommand = "^C";
    String username = "pi";
    String pwd = "ottoGoodBoy";
    String hubIP = "192.168.1.208"; // at home (on Dylan's Network)
    String collarIP = "192.168.1.214"; // at home (on Dylan's Network)
    // String hubIP = "192.168.43.111" // on Bahaabahaaba
    //String collarIP = "192.168.43.116 // on Bahaabahaaba
    int port = 22;
    boolean roomCountStarted;
    int roomCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putInt("roomCount", newHighScore);
        //editor.commit();

        if (!roomCountStarted) {
            roomCount = 0;
            editor.putInt("roomCount", roomCount);
            roomCountStarted = true;
            editor.commit();
        }

        final Button BeginTrackingButton = (Button) findViewById(R.id.TrackingButton);
        BeginTrackingButton.setOnClickListener(new View.OnClickListener() {
            boolean isTracking = false;

            @Override
            public void onClick(View view) {

                if (!isTracking) {
                    isTracking = true;
                    BeginTrackingButton.setText(R.string.end_tracking_button);
                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {

                                Log.v("BeginTracking", "BeginTrackingButton OnClickListener is called; trying to begin tracking");


                                String trackingCommandServer = "sudo python /home/pi/Desktop/realtime.py";
                                String trackingCommandClient = "sudo python /home/pi/Desktop/trainingAndRealtimeClient.py";
                                String trackingCommandJava = "java -cp /home/pi/Documents/project/hwb/target/hub-1.0-SNAPSHOT.jar com.HomewardBounds.app.realTime " + roomCount;

                                executeSSHcommand(username, pwd, hubIP, port, trackingCommandServer);

                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                executeSSHcommand(username, pwd, collarIP, port, trackingCommandClient);

                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                //executeSSHcommand(username, pwd, hubIP, port, trackingCommandJava);
                                sendCommandReceiveOutput(trackingCommandJava);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return null;

                        }

                    }.execute(1);

                } else if (isTracking)

                {
                    isTracking = false;
                    BeginTrackingButton.setText(R.string.begin_tracking_button);
                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            Log.v("BeginTracking", "BeginTrackingButton OnClickListener is called; trying to end tracking");

                            executeSSHcommand(username, pwd, hubIP, port, stopCommand);
                            executeSSHcommand(username, pwd, collarIP, port, stopCommand);

                            return null;

                        }

                    }.execute(1);

                }

            }

        });

        Button BoundaryActivityButton = (Button) findViewById(R.id.BoundaryActivityButton);
        BoundaryActivityButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, BoundaryMaker.class);

                Bundle bundle = new Bundle();
                bundle.putInt("roomCount", roomCount);

                intent.putExtras(bundle);
                startActivity(intent);

            }

        });

        Button ResetButton = (Button) findViewById(R.id.ResetButton);
        ResetButton.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {

                            Log.v("ResetButton", "ResetButton OnClickListener is called");

                            String removeModelsCommand = "sudo rm /home/pi/Documents/rooms/*";
                            String removeArffsCommand = "sudo rm /home/pi/Documents/project/hwb/rooms/*";

                            executeSSHcommand(username, pwd, hubIP, port, stopCommand);
                            executeSSHcommand(username, pwd, hubIP, port, removeModelsCommand);
                            executeSSHcommand(username, pwd, hubIP, port, removeArffsCommand);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;

                    }

                }.execute(1);

            }
        });

    }

    public void executeSSHcommand(String username, String password, String host, int port, String command) {

        Log.v("SSH", "executeSSHcommand() is called");

        try {

            // session
            JSch jSch = new JSch();
            Session session = jSch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            //session.setTimeout(10000); maybe don't leave this commented out?
            session.connect();

            // channel
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            channel.setCommand(command);
            channel.connect();

            Log.v("SSH", "channelExec connected successfully");

            channel.disconnect();
            session.disconnect();

            Log.v("SSH", "channelExec disconnected successfully");

        } catch (JSchException e) {
            Log.v("SSH", "JSchException error: " + e.getMessage());
        }
    }

    public void sendCommandReceiveOutput(String command) throws JSchException, IOException {

        JSch jSch = new JSch();
        Session session = jSch.getSession(username, hubIP, 22);
        session.setPassword(pwd);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        TextView tv = (TextView) findViewById(R.id.trackingView);

        InputStream in = channel.getInputStream();
        System.out.println("Opening...");

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.connect();

        byte[] bytes = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(bytes, 0, 1024);

                if (i < 0) {
                    break;
                }

                Log.v("myApp", new String(bytes, 0, i));
            }

            if (channel.isClosed()) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        channel.disconnect();
        session.disconnect();

    }

    public void refresh() {          //refresh is onClick name given to the button
        onRestart();
    }

    @Override
    protected void onRestart() {

        // TODO Auto-generated method stub
        super.onRestart();
        Intent i = new Intent(this, MainActivity.class);  //your class
        startActivity(i);
        finish();

    }
}
