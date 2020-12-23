import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {

    private static final Integer MessageLengthSize = 4;
    private static final Integer MessageTypeSize = 1;
    static Logger logger = Logger.getLogger(Message.class.getName());

    public static Map<String, Integer> MessageTypeMap;
    static {
        MessageTypeMap = new HashMap<>();
        MessageTypeMap.put("choke", 0);
        MessageTypeMap.put("unchoke", 1);
        MessageTypeMap.put("interested", 2);
        MessageTypeMap.put("not interested", 3);
        MessageTypeMap.put("have", 4);
        MessageTypeMap.put("bitfield", 5);
        MessageTypeMap.put("request", 6);
        MessageTypeMap.put("piece", 7);
    }

    public static List<String> NoPayLoadTypes = Arrays.asList("choke", "unchoke", "interested", "not interested");
    public static List<String> PieceIndexTypes = Arrays.asList("have", "request");

    public static byte[] intToByteArray(Integer length, Integer value) {
        return ByteBuffer.allocate(length).putInt(value).array();
    }

    public static Integer convertByteArrayToInt(byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray); // big-endian by default
        return buffer.getInt();
    }

    public static Integer extractMessageLength(byte[] byteArray) {
        return convertByteArrayToInt(Arrays.copyOfRange(byteArray, 0, 4));
    }

    public static String extractMessageType(byte byt) {
        Integer index = 0;
        for (int i=0; i<8; i++) {
            if ((byt & (1 << (7-i))) != 0) {
                index = i;
            }
        }
        for (String key: MessageTypeMap.keySet()) {
            if (MessageTypeMap.get(key) == index) {
                return key;
            }
        }
        return "";
    }

    public static String extractMessageType(byte[] byteArray) throws Exception {
        byte[] bytes = Arrays.copyOfRange(byteArray, 4, 5);
        Integer index = 0;
        for (int i=0; i<8; i++) {
            if ((bytes[0] & (1 << (7-i))) != 0) {
            //if (bytes[0][i]&1 == 1) {
                index = i;
            }
        }
        for (String key: MessageTypeMap.keySet()) {
            if (MessageTypeMap.get(key) == index) {
                return key;
            }
        }
        logger.log(Level.SEVERE, "Unexpected Message Type obtained.");
        throw new Exception();
    }

    public static byte[] encodeMessage(String messageType, byte[] message) {
        Integer messageLength = (message == null) ? MessageTypeSize : message.length + MessageTypeSize;
        byte[] messageLengthInBytes = intToByteArray(MessageLengthSize, messageLength);
        Integer finalMessageLength = messageLength + MessageLengthSize;
        StringBuilder messageTypeString = new StringBuilder("00000000");
        messageTypeString.setCharAt(MessageTypeMap.get(messageType), '1');
        byte messageTypeByte = Byte.parseByte(messageTypeString.toString(), 2);
        byte[] fullMessage = new byte[finalMessageLength];
        ByteBuffer buffer = ByteBuffer.wrap(fullMessage);
        buffer.put(messageLengthInBytes);
        buffer.put(messageTypeByte);
        if (message != null) {
            buffer.put(message);
        }
        return buffer.array();
    }

    public static byte[] encodeMessageWithoutPayload(String messageType) {
        return encodeMessage(messageType, null);
    }

    public static Integer decodePieceIndex(byte[] byteArray) {
        return Message.extractMessageLength(Arrays.copyOfRange(byteArray, 5, 9));
    }

    public static byte[] decodePiece(byte[] byteArray) {
        return Arrays.copyOfRange(byteArray, 10, byteArray.length - 1);
    }

}