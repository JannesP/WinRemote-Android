package nourl.jannespeters.winremote.networking;

/**
 * Created by Jannes Peters on 6/5/2015.
 */
public interface IResultReceiver {
    void runOnUiThread(Runnable runnable);
    void receiveResult(Result result);
}
