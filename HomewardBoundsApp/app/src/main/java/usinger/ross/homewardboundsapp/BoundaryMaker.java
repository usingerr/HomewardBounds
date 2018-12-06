package usinger.ross.homewardboundsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class BoundaryMaker extends AppCompatActivity {
    String stopCommand = "^C";
    String username = "pi";
    String pwd = "ottoGoodBoy";
    String hubIP = "192.168.1.208"; // at home (on Dylan's Network)
    String collarIP = "192.168.1.214"; // at home (on Dylan's Network)
    // String hubIP = "192.168.43.111" // on Bahaabahaaba
    //String collarIP = "192.168.43.116 // on Bahaabahaaba
    int port = 22;
    int roomCount;
    boolean isRestricting = false;
    boolean isPermitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boundary_maker_layout);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        //editor.putInt("roomCount", newHighScore);
        //editor.commit();
        roomCount = sharedPref.getInt("roomcount", 0);

        final Button RestrictButton = (Button) findViewById(R.id.RestrictButton);
        RestrictButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //editor.putInt("roomCount", roomCount);
                //editor.apply();
                isRestricting = !isRestricting;

                if (isRestricting) {
                    roomCount++;
                    RestrictButton.setText(R.string.end_restrict_button);

                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                Log.v("Hub", "RestrictButton setOnClickListener is called; trying to begin restricting");

                                String discoverableCommand = "sudo hciconfig hci0 piscan";
                                String serverCommand = "sudo python /home/pi/Desktop/training.py YES " + roomCount;
                                String clientCommand = "sudo python /home/pi/Desktop/trainingAndRealtimeClient.py";

                                executeSSHcommand(username, pwd, hubIP, port, discoverableCommand);
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                executeSSHcommand(username, pwd, hubIP, port, serverCommand);
                                try {
                                    Thread.sleep(5000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                executeSSHcommand(username, pwd, collarIP, port, clientCommand);

                            } catch (Exception e) {

                                Log.v("Hub", "Error:");
                                e.printStackTrace();

                            }

                            return null;

                        }

                    }.execute(1);

                } else if (!isRestricting) {
                    RestrictButton.setText(R.string.begin_restrict_button);

                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                Log.v("Hub", "RestrictButton setOnClickListener is called; trying to end restricting");

                                executeSSHcommand(username, pwd, hubIP, port, stopCommand);
                                executeSSHcommand(username, pwd, collarIP, port, stopCommand);

                            } catch (Exception e) {
                                Log.v("Hub", "Error:");
                                e.printStackTrace();
                            }

                            return null;

                        }

                    }.execute(1);
                }
            }
        });

        final Button PermitButton = (Button) findViewById(R.id.PermitButton);
        PermitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                isPermitting = !isPermitting;

                if (isPermitting) {
                    PermitButton.setText(R.string.end_permit_button);

                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {

                                Log.v("MyApp", "PermitButton OnClickListener is called; trying to begin permitting");

                                String discoverableCommand = "sudo hciconfig hci0 piscan";
                                String serverCommand = "sudo python /home/pi/Desktop/training.py NO 1";
                                String clientCommand = "sudo python /home/pi/Desktop/trainingAndRealtime.py";

                                executeSSHcommand(username, pwd, hubIP, port, discoverableCommand);
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                executeSSHcommand(username, pwd, hubIP, port, serverCommand);
                                try {
                                    Thread.sleep(5000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                executeSSHcommand(username, pwd, collarIP, port, clientCommand);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return null;

                        }

                    }.execute(1);

                } else if (!isPermitting) {
                    PermitButton.setText(R.string.begin_permit_button);

                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            try {
                                Log.v("MyApp", "PermitButton OnClickListener is called; trying to end permitting");

                                executeSSHcommand(username, pwd, hubIP, port, stopCommand);
                                executeSSHcommand(username, pwd, collarIP, port, stopCommand);

                                String trainingCommand = "cd home/pi/Documents/hwb/; sudo java -cp target/hwb-1.0.SNAPSHOT.jar com.HomewardBounds.app.App " + roomCount;

                                executeSSHcommand(username, pwd, hubIP, port, trainingCommand);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return null;

                        }

                    }.execute(1);
                }
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
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

            channelExec.setCommand(command);
            channelExec.connect();

            Log.v("SSH", "channelExec connected successfully");

            channelExec.disconnect();
            session.disconnect();

            Log.v("SSH", "channelExec disconnected successfully");
        } catch (JSchException e) {
            Log.v("SSH", "JSchException error: " + e.getMessage());
        }
    }
}

