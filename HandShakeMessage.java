import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HandShakeMessage {

    private static final String Encoding = "UTF8";
    public static final Integer MessageLength = 32; // bytes
    private static final Integer PeerIdSize = 4;
    private static final Integer ZeroPaddingSize = 10;
    private static final String Header = "P2PFILESHARINGPROJ";

    static Logger logger = Logger.getLogger(HandShakeMessage.class.getName());

    public static byte[] prepareHandshakeMessage (Integer myId) throws Exception {
        byte[] handshakeMessage = new byte[MessageLength];
        byte[] headerInBytes = Header.getBytes(Encoding);
        byte[] zeroStringInBytes = ByteBuffer.allocate(ZeroPaddingSize).putInt(0).array();
        byte[] myIdInBytes = ByteBuffer.allocate(PeerIdSize).putInt(myId).array();
        ByteBuffer buffer = ByteBuffer.wrap(handshakeMessage);
        buffer.put(headerInBytes);
        buffer.put(zeroStringInBytes);
        buffer.put(myIdInBytes);
        return buffer.array();
    }

    public static Integer decodeHandshakeMessage (byte[] byteArray) throws Exception {
        byte[] headerInBytes = Arrays.copyOfRange(byteArray, 0, 18);
        String header = new String(headerInBytes, Encoding);
        if (!header.equals(Header)) {
            logger.log(Level.SEVERE, "Handshake Header Wrong. Unauthorized.");
            throw new Exception();
        }
        byte[] peerIdInBytes = Arrays.copyOfRange(byteArray, 28, 32);
        return ByteBuffer.wrap(peerIdInBytes).getInt();
        //return Integer.parseInt(new String(peerIdInBytes, Encoding));
    }

}