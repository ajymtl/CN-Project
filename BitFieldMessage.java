import java.nio.ByteBuffer;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitFieldMessage {

    private static final String MessageType = "bitfield";
    static Logger logger = Logger.getLogger(BitFieldMessage.class.getName());

    public static Integer calculateLength(Integer chunks) {
        Integer bytesForChunks = chunks/8;
        if (chunks%8 != 0) {
            bytesForChunks = bytesForChunks + 1;
        }
        return bytesForChunks;
    }

    private static Boolean[] getBitFieldArray(byte[] byteArray) {
        Boolean[] bitFieldArray = new Boolean[byteArray.length*8];
        for (int i=0; i<byteArray.length; i++) {
            for (int j=0; j<8; j++) {
                bitFieldArray[(i*8)+j] = ((byteArray[0] & (1 << (7-j))) != 0) ? true : false;
            }
        }
        return bitFieldArray;
    }

    public static byte[] prepareBitFieldMessage(Boolean hasFile, Integer chunks) {
        Integer pureMessageLength = calculateLength(chunks); //int for messageLength + int for messageType + bytesForChunks
        String bitfield = new String(new char[chunks]).replace("\0", (hasFile) ? "1" : "0"); // preparing bitfield string
        String zeroes = new String(new char[8 - (chunks%8)]).replace("\0", "0"); // preparing zeroes to append
        bitfield = bitfield.concat(zeroes);
        ByteBuffer buffer = ByteBuffer.allocate(pureMessageLength);
        for (int i=0; i<pureMessageLength; i++) {
            byte aByte = Byte.parseByte(bitfield.substring(i*8, (i+1)*8-1), 2);
            buffer.put(aByte);
        }
        byte[] messageBytes = buffer.array();
        return Message.encodeMessage(MessageType, messageBytes);
    }

    public static Boolean[] decodeBitFieldMessage (byte[] byteArray) throws Exception {
        Integer messageLength = Message.extractMessageLength(byteArray);
        String messageType = Message.extractMessageType(byteArray);
        if (messageType != "bitfield") {
            logger.log(Level.SEVERE, "Unknown message received. Should have received - bitfield");
        } else if (messageLength < 2) {
            logger.log(Level.SEVERE, "Unknown length message of bitfield type.");
        }
        byte[] bitFieldInBytes = Arrays.copyOfRange(byteArray, 5, byteArray.length);
        return getBitFieldArray(bitFieldInBytes);
    }

}