package nourl.jannespeters.winremote;

/**
 * Created by Jannes Peters on 6/5/2015.
 */
public class Util {
    public static int readIntFromByteArray(byte[] input, int startIndex) {
        int result = 0;

        for (int i = 3; i >= 0; i--)
        {
            int unsignedByte = input[startIndex + i] & 0xFF; //in java you have to cast the signed bytes into unsigned because otherwise it will cast it to the negative int eg -12 != 244
            result |= (unsignedByte << (8 * i));
        }

        return result;
    }
}
