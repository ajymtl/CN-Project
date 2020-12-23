import java.nio.ByteBuffer;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PieceMessage {

    private static final String MessageType = "piece";
    static Logger logger = Logger.getLogger(PieceMessage.class.getName());

    public static byte[] preparePieceMessage(int pieceIndex) {
        byte[] puremessage = peerProcess.content.get(pieceIndex);
        return Message.encodeMessage(MessageType, puremessage);
    }

}