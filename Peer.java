public class Peer {

    //configured from PeerInfo.cfg
    public final int id;
    public final String ip;
    public final int port;
    public final Boolean hasFile;

    //only to be set for myself
    public Integer preferredNeighborCount;
    public Integer unchokingInteval;
    public Integer optimisticUnchokingInterval;
    public String fileName;
    public Integer fileSize;
    public Integer pieceSize;

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Boolean getHasFile() {
        return hasFile;
    }

    public Integer getPreferredNeighborCount() {
        return preferredNeighborCount;
    }

    public void setPreferredNeighborCount(Integer preferredNeighborCount) {
        this.preferredNeighborCount = preferredNeighborCount;
    }

    public Integer getUnchokingInteval() {
        return unchokingInteval;
    }

    public void setUnchokingInteval(Integer unchokingInteval) {
        this.unchokingInteval = unchokingInteval;
    }

    public Integer getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public void setOptimisticUnchokingInterval(Integer optimisticUnchokingInterval) {
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(Integer pieceSize) {
        this.pieceSize = pieceSize;
    }

    public Peer(int id, String ip, int port, Boolean hasFile) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.hasFile = hasFile;
    }

}