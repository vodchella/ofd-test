import com.google.protobuf.InvalidProtocolBufferException;
import ofd.proto.Message;

import java.io.*;
import java.net.Socket;

public class OfdTest {

    private static byte[] createRequestPacket(byte[] body) {
        OfdHeader header = new OfdHeader()
                .setVersion((short) 202)
                .setSize(OfdHeader.SIZE + body.length)
                .setToken(647)
                .setId(600)
                .setReqNum((short) 1);
        byte[] headerBytes = header.toByteArray();

        byte[] packet = new byte[OfdHeader.SIZE + body.length];
        System.arraycopy(headerBytes, 0, packet, 0, OfdHeader.SIZE);
        System.arraycopy(body, 0, packet, OfdHeader.SIZE, body.length);

        return packet;
    }

    private static Message.Response createResponse(byte[] responsePacket,
                                                   int responseLength) throws InvalidProtocolBufferException {

        OfdHeader responseHeader = OfdHeader.fromByteArray(responsePacket);
        if (responseLength == responseHeader.getSize()) {
            int bodyLength = responseHeader.getSize() - OfdHeader.SIZE;
            byte[] bodyBytes = new byte[bodyLength];
            System.arraycopy(responsePacket, OfdHeader.SIZE, bodyBytes, 0, bodyLength);
            return Message.Response.parseFrom(bodyBytes);
        }
        System.out.println("Invalid packet size");
        return null;
    }

    private static ReadStreamBytesResult readStreamBytes(DataInputStream in) throws IOException {
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

    private static Message.Response sendOfdRequest(Message.Request request) throws IOException {
        byte[] requestPacket = createRequestPacket(request.toByteArray());

        Socket socket = new Socket("ofd.t-cloud.kz", 21001);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out.write(requestPacket);
        out.flush();

        ReadStreamBytesResult response = readStreamBytes(in);
        return createResponse(response.getBytes(), response.getLength());
    }

    public static void main(String[] args) throws IOException {
        Message.Response response = sendOfdRequest(
                Message.Request.newBuilder()
                        .setCommand(Message.CommandTypeEnum.COMMAND_INFO)
                        .build()
        );
        if (response != null) {
            System.out.printf("Result: %s%n", response.getResult().getResultText());
        }

        System.out.println("Ofd test!");
    }
}
