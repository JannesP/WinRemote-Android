package nourl.jannespeters.winremote.networking;

/**
 * Created by Jannes Peters on 6/5/2015.
 */
public class Result {
    private int messageId;
    private int requestId;
    private INetworkInterface.NETWORK_STATUS networkStatus;
    private byte[] result;

    public Result(int messageId, int requestId, INetworkInterface.NETWORK_STATUS networkStatus, byte[] result) {
        this.messageId = messageId;
        this.requestId = requestId;
        this.networkStatus = networkStatus;
        this.result = result;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getRequestId() {
        return requestId;
    }

    public INetworkInterface.NETWORK_STATUS getNetworkStatus() {
        return networkStatus;
    }

    public byte[] getResult() {
        return result;
    }
}
