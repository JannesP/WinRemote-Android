package nourl.jannespeters.winremote.networking;

/**
 * Created by Jannes Peters on 5/11/2015.
 */
public interface INetworkInterface {
    int MESSAGE_STATUS = 0;

    int MESSAGE_TEST = 123456789;
    int MESSAGE_SHUTDOWN = 1;

    int REQUEST_VOLUME = 500; //get a response with an int 0-100
    int REQUEST_MUTED = 501; //get a response with an int 0-1

    int CHANGE_VOLUME = 1000;
    int CHANGE_MUTE = 1001;

    public enum NETWORK_STATUS {
        OK, FAILED
    }

    /**
     * This method runs in a new Thread and answers to the resultReceiver after the connection finished.
     * Because of that the status information might not come in the right order!
     * @param messageId which should be sent
     * @param resultReceiver the receiver which should get status information
     * @return returns a unique requestId which CAN be saved to compare with the <code>networking.Result</code>
     */
    int sendMessage(int messageId, IResultReceiver resultReceiver);

    /**
     * This method runs in a new Thread and answers to the resultReceiver after the connection finished.
     * Because of that the status information might not come in the right order!
     * @param messageId which should be sent
     * @param resultReceiver the receiver which should get status information
     * @return returns a unique requestId which CAN be saved to compare with the <code>networking.Result</code>
     */
    int requestAnswer(int messageId, IResultReceiver resultReceiver);

    /**
     * This method runs in a new Thread and answers to the resultReceiver after the connection finished.
     * Because of that the status information might not come in the right order!
     * @param messageId which should be sent
     * @param data the data that should be sent in the corresponding message
     * @param resultReceiver the receiver which should get status information
     * @return returns a unique requestId which CAN be saved to compare with the <code>networking.Result</code>
     */
    int sendMessage(int messageId, int data, IResultReceiver resultReceiver);
}
