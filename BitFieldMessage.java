import java.nio.ByteBuffer;
import java.util.Arrays;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitFieldMessage {

    private static final String MessageType = "bitfield";
    static Logger logger = Logger.getLogger(BitFieldMessage.class.getName());

    public static Integer calculateLength(Integer chunks) {
        Integer bytesForChunks = chunks/8 + 1;
        return bytesForChunks;
    }

    private static Boolean[] getBitFieldArray(byte[] byteArray) {
        BigInteger temp;
        temp = new BigInteger(byteArray);
        String bitFieldString = temp.toString(2);
        Boolean[] bitFieldArray = new Boolean[bitFieldString.length()];
        for (int i=0; i< bitFieldString.length(); i++) {
            bitFieldArray[i] = bitFieldString.charAt(i) == '0' ? false : true;
        }
        return bitFieldArray;
    }

    public static byte[] prepareBitFieldMessage(Boolean hasFile, Integer chunks) {
        Integer pureMessageLength = calculateLength(chunks); //int for messageLength + int for messageType + bytesForChunks
        byte[] byteArray = new byte[pureMessageLength];
        String bitfield = new String(new char[chunks]).replace("\0", (hasFile) ? "1" : "0"); // preparing bitfield string
        String zeroes = new String(new char[8 - (chunks%8)]).replace("\0", "0"); // preparing zeroes to append
        bitfield.concat(zeroes);
        Integer bitfieldBinary = Integer.parseInt(bitfield, 2);
        ByteBuffer buffer = ByteBuffer.allocate(pureMessageLength).putInt(bitfieldBinary);
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
        byte[] bitFieldInBytes = Arrays.copyOfRange(byteArray, 5, byteArray.length-1);
        return getBitFieldArray(bitFieldInBytes);
    }

}