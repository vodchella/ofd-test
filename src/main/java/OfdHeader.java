public class OfdHeader {

    public static final int SIZE = 18;
    private static final short APP_CODE = (short) 0x81A2;

    private short appCode = APP_CODE;
    private short version;
    private int size;
    private int id;
    private int token;
    private short reqNum;

    public static OfdHeader fromByteArray(byte[] bytes) {
        OfdHeader result = new OfdHeader();

        result.appCode = (short) (bytes[1] & 0xff);
        result.appCode = (short) ((result.appCode << 8) + (bytes[0] & 0xff));

        result.version = (short) (bytes[3] & 0xff);
        result.version = (short) ((result.version << 8) + (bytes[2] & 0xff));

        result.size = (short) (bytes[7] & 0xff);
        result.size = (short) ((result.size << 8) + (bytes[6] & 0xff));
        result.size = (short) ((result.size << 8) + (bytes[5] & 0xff));
        result.size = (short) ((result.size << 8) + (bytes[4] & 0xff));

        result.id = (short) (bytes[11] & 0xff);
        result.id = (short) ((result.id << 8) + (bytes[10] & 0xff));
        result.id = (short) ((result.id << 8) + (bytes[9] & 0xff));
        result.id = (short) ((result.id << 8) + (bytes[8] & 0xff));

        result.token = (short) (bytes[15] & 0xff);
        result.token = (short) ((result.token << 8) + (bytes[14] & 0xff));
        result.token = (short) ((result.token << 8) + (bytes[13] & 0xff));
        result.token = (short) ((result.token << 8) + (bytes[12] & 0xff));

        result.reqNum = (short) (bytes[17] & 0xff);
        result.reqNum = (short) ((result.reqNum << 8) + (bytes[16] & 0xff));

        return result;
    }

    public byte[] toByteArray() {
        byte[] result = new byte[SIZE];

        result[0] = (byte) (this.appCode);
        result[1] = (byte) (this.appCode >> 8);

        result[2] = (byte) (this.version);
        result[3] = (byte) (this.version >> 8);

        result[4] = (byte) (this.size);
        result[5] = (byte) (this.size >> 8);
        result[6] = (byte) (this.size >> 16);
        result[7] = (byte) (this.size >> 24);

        result[8] = (byte) (this.id);
        result[9] = (byte) (this.id >> 8);
        result[10] = (byte) (this.id >> 16);
        result[11] = (byte) (this.id >> 24);

        result[12] = (byte) (this.token);
        result[13] = (byte) (this.token >> 8);
        result[14] = (byte) (this.token >> 16);
        result[15] = (byte) (this.token >> 24);

        result[16] = (byte) (this.reqNum);
        result[17] = (byte) (this.reqNum >> 8);

        return result;
    }

    public short getAppCode() {
        return appCode;
    }

    public short getVersion() {
        return version;
    }

    public OfdHeader setVersion(short version) {
        this.version = version;
        return this;
    }

    public int getSize() {
        return size;
    }

    public OfdHeader setSize(int size) {
        this.size = size;
        return this;
    }

    public int getId() {
        return id;
    }

    public OfdHeader setId(int id) {
        this.id = id;
        return this;
    }

    public int getToken() {
        return token;
    }

    public OfdHeader setToken(int token) {
        this.token = token;
        return this;
    }

    public short getReqNum() {
        return reqNum;
    }

    public OfdHeader setReqNum(short reqNum) {
        this.reqNum = reqNum;
        return this;
    }

}
