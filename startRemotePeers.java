import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class startRemotePeers {

    private static final String commandFormat = "ssh %s@%s cd project; java peerProcess %s";
    private static final String peerInfoConfig = "PeerInfo.cfg";
    private static final String userName = "ajmittal";
    private static final String whiteSpaceRegex = "\\s+";

    Logger logger = Logger.getLogger(peerProcess.class.getName());

    public void start() {
        logger.log(Level.INFO,"Starting peer processes ...");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(peerInfoConfig));
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(whiteSpaceRegex);
                String peerId = parts[0];
                String ipAddress = parts[1];
                String command = String.format(commandFormat, userName, ipAddress, peerId);
                logger.log(Level.INFO, "Starting peerProcess with peerId: %s\n", peerId);
                Runtime.getRuntime().exec(command);
                line = reader.readLine();
            }
            logger.log(Level.SEVERE, "Successfully started peer processes.");
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "File not found: PeerInfo.cfg.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading PeerInfo.cfg.");
        } finally {
            logger.log(Level.INFO, "Shutting down ...");
        }
    }

    public static void main(String[] args) {
        startRemotePeers obj = new startRemotePeers();
        obj.start();
    }

}