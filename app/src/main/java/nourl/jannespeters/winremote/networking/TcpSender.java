package nourl.jannespeters.winremote.networking;

import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import nourl.jannespeters.winremote.Util;

/**
 * Created by Jannes Peters on 5/8/2015.
 */
public class TcpSender implements INetworkInterface {

    private InetSocketAddress inetSocketAddress = null;
    private int timeout = 1000;
    private int currentRequestId = Integer.MIN_VALUE;

    public TcpSender(String ip, int port) throws IOException {
        inetSocketAddress = new InetSocketAddress(ip, port);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int requestAnswer(final int messageId, final IResultReceiver resultReceiver) {
        final int requestId = getNewRequestId();
        final int connectionTimeout = this.timeout;
        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean failed = false;
                Socket socket = new Socket();
                OutputStream writer = null;

                try {   //create connection
                    socket.connect(inetSocketAddress, 1000);
                    writer = socket.getOutputStream();
                } catch (IOException e) {
                    failed = true;
                    e.printStackTrace();
                }

                if (writer != null) {   //send message
                    byte[] message = createStandardMessage(messageId);
                    try {
                        writer.write(message);
                        writer.flush();
                    } catch (IOException e) {
                        failed = true;
                        e.printStackTrace();
                    }
                }

                byte[] buffer = new byte[32];
                int res = -1;
                if (!failed) {
                    //start receiving
                    try {
                        InputStream inputStream = socket.getInputStream();
                        long startTime = SystemClock.elapsedRealtime();
                        do {
                            res = inputStream.read(buffer, 0, 32);
                            if (res == -1 && ((SystemClock.elapsedRealtime() - startTime) >= connectionTimeout)) {
                                break;
                            } else {
                                Log.d("GOTTEN DATA: " + messageId, arrayString(buffer));
                            }
                        } while (res == -1);
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        failed = true;
                    }
                }
                Result result;
                if (!failed) {
                    result = new Result(Util.readIntFromByteArray(buffer, 0), requestId, res == -1 ? NETWORK_STATUS.FAILED : NETWORK_STATUS.OK, extractMessage(buffer));
                }
                else {
                    result = new Result(messageId, requestId, NETWORK_STATUS.FAILED, null);
                }
                final Result fResult = result;
                resultReceiver.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultReceiver.receiveResult(fResult);
                    }
                });
            }
        });
        connectionThread.start();

        return requestId;
    }

    private String arrayString(byte[] array) {
        String res = "";
        for (byte b : array) {
            res += String.valueOf((int)b) + ", ";
        }
        return res;
    }

    @Override
    public int sendMessage(int messageId, IResultReceiver resultReceiver) {
        return sendMessage(messageId, 0, resultReceiver);
    }

    @Override
    public int sendMessage(final int messageId, final int data, final IResultReceiver resultReceiver) {
        final byte[] message = createStandardMessage(messageId, data);
        Log.d("SENT DATA: " + messageId, arrayString(message));
        return sendByteArray(message, resultReceiver);
    }

    private int sendByteArray(final byte[] data, final IResultReceiver resultReceiver) {
        final int requestId = getNewRequestId();
        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean failed = false;
                Socket socket = new Socket();
                OutputStream writer = null;
                try {
                    socket.connect(inetSocketAddress, 1000);
                    writer = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    failed = true;
                }
                try {
                    if (writer != null) {
                        writer.write(data);
                        writer.flush();
                        writer.close();
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    failed = true;
                }

                final Result fResult = new Result(0, requestId, failed ? NETWORK_STATUS.FAILED : NETWORK_STATUS.OK, null);
                resultReceiver.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultReceiver.receiveResult(fResult);
                    }
                });
            }
        });
        connectionThread.start();
        return requestId;
    }

    private int getNewRequestId() {
        if (currentRequestId == Integer.MAX_VALUE) {
            currentRequestId = Integer.MIN_VALUE;
        }
        return currentRequestId++;
    }

    private static byte[] createStandardMessage(int messageId) {
        byte[] message = new byte[32];
        byte[] byteMessageId = createBytesFromInt(messageId);
        System.arraycopy(byteMessageId, 0, message, 0, byteMessageId.length);
        return message;
    }

    private static byte[] createStandardMessage(int messageId, int data) {
        byte[] message = createStandardMessage(messageId);
        byte[] byteData = createBytesFromInt(data);
        System.arraycopy(byteData, 0, message, 4, byteData.length);
        return message;
    }

    private static byte[] createBytesFromInt(int num) {
        byte[] byteNum = new byte[4];

        for (int i = 0; i < byteNum.length; i++)
        {
            byte x = (byte)(num >> (8 * i) & 0xFF);
            byteNum[i] = x;
        }

        return byteNum;
    }

    private static byte[] extractMessage(byte[] tcpMessage) {
        byte[] message = new byte[tcpMessage.length - 4];
        System.arraycopy(tcpMessage, 4, message, 0, message.length);
        return message;
    }

}
