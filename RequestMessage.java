import java.nio.ByteBuffer;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestMessage {

    private static final String MessageType = "request";
    static Logger logger = Logger.getLogger(PieceMessage.class.getName());

    public static byte[] prepareRequestMessage(int pieceIndex) {
        byte[] puremessage = Message.intToByteArray(4, pieceIndex);
        return Message.encodeMessage(MessageType, puremessage);
    }

}