import ofd.proto.Message;

import java.io.*;
import java.net.Socket;

public class OfdConnector {

    private final int id;
    private int token;
    private short reqNum;

    private static class ReadStreamBytesResult {

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

    private static class CreateResponseResult {

        private OfdHeader header;
        private Message.Response response;

        public OfdHeader getHeader() {
            return header;
        }

        public CreateResponseResult setHeader(OfdHeader header) {
            this.header = header;
            return this;
        }

        public Message.Response getResponse() {
            return response;
        }

        public CreateResponseResult setResponse(Message.Response response) {
            this.response = response;
            return this;
        }

    }

    private byte[] createRequestPacket(byte[] body) {
        final short ofdProtocolVersion = 202;
        OfdHeader header = new OfdHeader()
                .setVersion(ofdProtocolVersion)
                .setSize(OfdHeader.SIZE + body.length)
                .setToken(this.token)
                .setId(this.id)
                .setReqNum(this.reqNum);
        byte[] headerBytes = header.toByteArray();

        byte[] packet = new byte[OfdHeader.SIZE + body.length];
        System.arraycopy(headerBytes, 0, packet, 0, OfdHeader.SIZE);
        System.arraycopy(body, 0, packet, OfdHeader.SIZE, body.length);

        return packet;
    }

    private CreateResponseResult createResponse(byte[] responsePacket,
                                                int responseLength) throws Exception {

        OfdHeader responseHeader = OfdHeader.fromByteArray(responsePacket);
        if (responseLength == responseHeader.getSize()) {
            int bodyLength = responseHeader.getSize() - OfdHeader.SIZE;
            byte[] bodyBytes = new byte[bodyLength];
            System.arraycopy(responsePacket, OfdHeader.SIZE, bodyBytes, 0, bodyLength);

            return new CreateResponseResult()
                    .setHeader(responseHeader)
                    .setResponse(Message.Response.parseFrom(bodyBytes));
        }
        throw new Exception("Invalid packet size");
    }

    private ReadStreamBytesResult readStreamBytes(DataInputStream in) throws IOException {
        int bytesRead;
        int totalBytesRead = 0;
        int resultBufferSize = 4096;
        final int bufferSize = 4096;
        byte[] resultBytes = new byte[resultBufferSize];
        byte[] buffer = new byte[bufferSize];
        while ((bytesRead = in.read(buffer, 0, bufferSize)) != -1) {
            if ((totalBytesRead + bytesRead) > resultBufferSize) {
                resultBufferSize *= 2;
                byte[] newResponseBuffer = new byte[resultBufferSize];
                System.arraycopy(resultBytes, 0, newResponseBuffer, 0, totalBytesRead);
                resultBytes = newResponseBuffer;
            }
            System.arraycopy(buffer, 0, resultBytes, totalBytesRead, bytesRead);
            totalBytesRead += bytesRead;
            if (bytesRead < bufferSize) {
                break;
            }
        }
        return new ReadStreamBytesResult()
                .setLength(totalBytesRead)
                .setBytes(resultBytes);
    }

    private synchronized void updateInternalsFromHeader(OfdHeader header) {
        this.token = header.getToken();
        if (this.reqNum == Short.MAX_VALUE) {
            this.reqNum = 1;
        } else {
            this.reqNum += 1;
        }
    }

    private ReadStreamBytesResult getOfdResponseBytes(byte[] requestPacket) throws IOException {
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            Socket socket = new Socket("ofd.t-cloud.kz", 21001);
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out.write(requestPacket);
            out.flush();
            return readStreamBytes(in);
        } finally {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        }
    }

    public Message.Response request(Message.Request request) throws Exception {
        byte[] requestPacket = createRequestPacket(request.toByteArray());
        ReadStreamBytesResult response = getOfdResponseBytes(requestPacket);
        CreateResponseResult result = createResponse(response.getBytes(), response.getLength());
        updateInternalsFromHeader(result.getHeader());
        return result.getResponse();
    }

    public OfdConnector(int ofdId, int ofdToken) {
        this.id = ofdId;
        this.token = ofdToken;
        this.reqNum = 1;
    }

    public int getToken() {
        return this.token;
    }

}
