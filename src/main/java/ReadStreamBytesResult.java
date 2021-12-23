public class ReadStreamBytesResult {

    private int length;
    private byte[] bytes;

    public int getLength() {
        return length;
    }

    public ReadStreamBytesResult setLength(int length) {
        this.length = length;
        return this;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public ReadStreamBytesResult setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }

}
