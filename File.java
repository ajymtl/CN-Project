import java.io.FileOutputStream;

public class File {

    private final String fileName;
    private final Integer chunks;
    private Object[] chunkList;

    public File(String fileName, Integer chunks) {
        this.fileName = fileName;
        this.chunks = chunks;
        this.chunkList = new byte[chunks][];
    }

    public void insertPiece(Integer pieceIndex, byte[] pieceArray) {
        this.chunkList[pieceIndex] = pieceArray;
    }

    public void dumpCompletedFile() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(this.fileName)) {
            for (Object byteArray : this.chunkList) {
                fos.write((byte[]) byteArray);
            }
        }
    }

}