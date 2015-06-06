package nourl.jannespeters.winremote;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import nourl.jannespeters.winremote.networking.INetworkInterface;
import nourl.jannespeters.winremote.networking.IResultReceiver;
import nourl.jannespeters.winremote.networking.Result;
import nourl.jannespeters.winremote.networking.TcpSender;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class MainActivity extends Activity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, IResultReceiver, SeekBar.OnSeekBarChangeListener {

    TextView tvStatus;
    private TcpSender tcpInterface;
    private long lastVolumeTcpChangeRequested = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.buttonSendTestPackage).setOnClickListener(this);
        findViewById(R.id.buttonSendShutdown).setOnClickListener(this);
        findViewById(R.id.buttonRequestVolume).setOnClickListener(this);
        ((SeekBar)findViewById(R.id.seekBarRemoteVolume)).setMax(100);
        ((SeekBar)findViewById(R.id.seekBarRemoteVolume)).setOnSeekBarChangeListener(this);
        tvStatus = (TextView) findViewById(R.id.textViewStatus);

        setUpTcpConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSendTestPackage:
                if (tcpInterface == null) return;
                tcpInterface.sendMessage(INetworkInterface.MESSAGE_TEST, MainActivity.this);
                break;
            case R.id.buttonSendShutdown:
                if (tcpInterface == null) return;
                tcpInterface.sendMessage(INetworkInterface.MESSAGE_SHUTDOWN, MainActivity.this);
                break;
            case R.id.buttonRequestVolume:
                if (tcpInterface == null) return;
                ((TextView)findViewById(R.id.textViewRemoteVolume)).setText("Remote Volume: REQUESTING ...");
                ((TextView)findViewById(R.id.textViewRemoteMuted)).setText("Remote Muted: REQUESTING ...");
                tcpInterface.requestAnswer(INetworkInterface.REQUEST_VOLUME, MainActivity.this);
                tcpInterface.requestAnswer(INetworkInterface.REQUEST_MUTED, MainActivity.this);
                break;
            default:
                Log.e("WARNING", "Got input event from the id: " + v.getId() + " which is not implemented!");
                break;
        }
    }

    private void setUpTcpConnection() {
        String addr = getDefaultSharedPreferences(getApplicationContext()).getString("pref_tcp_ip", "192.168.188.20");
        int port = Integer.valueOf(getDefaultSharedPreferences(getApplicationContext()).getString("pref_tcp_port", "5556"));
        int timeout = Integer.valueOf(getDefaultSharedPreferences(getApplicationContext()).getString("pref_tcp_timeout", "1000"));

        try {
            tcpInterface = new TcpSender(addr, port);
            tcpInterface.setTimeout(timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_tcp_ip") || key.equals("pref_tcp_port") || key.equals("pref_tcp_timeout")) {
            setUpTcpConnection();
        }
    }

    @Override
    public void receiveResult(Result result) {
        if (result.getNetworkStatus() == INetworkInterface.NETWORK_STATUS.OK) {
            switch (result.getMessageId()) {
                case INetworkInterface.REQUEST_VOLUME:
                    int volume = Util.readIntFromByteArray(result.getResult(), 0);
                    ((SeekBar)findViewById(R.id.seekBarRemoteVolume)).setProgress(volume);
                    ((TextView)findViewById(R.id.textViewRemoteVolume)).setText("Remote Volume: " + String.valueOf(volume));
                    break;
                case INetworkInterface.REQUEST_MUTED:
                    boolean isMuted = Util.readIntFromByteArray(result.getResult(), 0) != 0;    //0 = false : 1 = true
                    ((TextView)findViewById(R.id.textViewRemoteMuted)).setText("Remote Muted: " + String.valueOf(isMuted));
                    break;
            }
        } else {
            switch (result.getMessageId()) {
                case INetworkInterface.REQUEST_VOLUME:
                    ((TextView)findViewById(R.id.textViewRemoteVolume)).setText("Remote Volume: ERROR");
                    break;
                case INetworkInterface.REQUEST_MUTED:
                    ((TextView)findViewById(R.id.textViewRemoteMuted)).setText("Remote Muted: ERROR");
                    break;
            }
        }
        switch (result.getMessageId()) {
            case INetworkInterface.MESSAGE_STATUS:

                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.seekBarRemoteVolume:
                if (tcpInterface == null) return;
                if ((SystemClock.elapsedRealtime() - lastVolumeTcpChangeRequested > 100) && fromUser) {
                    lastVolumeTcpChangeRequested = SystemClock.elapsedRealtime();
                    tcpInterface.sendMessage(INetworkInterface.CHANGE_VOLUME, seekBar.getProgress(), this);
                }
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (tcpInterface == null) return;
        ((TextView)findViewById(R.id.textViewRemoteVolume)).setText("Remote Volume: REQUESTING ...");
        tcpInterface.requestAnswer(INetworkInterface.REQUEST_VOLUME, MainActivity.this);
    }
}
