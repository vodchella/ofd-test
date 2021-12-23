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

    private static Message.Response sendOfdRequest(Message.Request request) throws IOException {
        byte[] requestPacket = createRequestPacket(request.toByteArray());

        Socket socket = new Socket("ofd.t-cloud.kz", 21001);
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out.write(requestPacket);
        out.flush();

        int bytesRead;
        int totalBytesRead = 0;
        int responseSize = 8192;
        final int bufferSize = 4096;
        byte[] responseBytes = new byte[responseSize];
        byte[] buffer = new byte[bufferSize];
        while ((bytesRead = in.read(buffer, 0, bufferSize)) != -1) {
            if (bytesRead < bufferSize) {
                totalBytesRead = bytesRead;
                responseBytes = buffer;
                break;
            }
            if ((totalBytesRead + bytesRead) > responseSize) {
                responseSize *= 2;
                byte[] newResponseBuffer = new byte[responseSize];
                System.arraycopy(responseBytes, 0, newResponseBuffer, 0, totalBytesRead);
                responseBytes = newResponseBuffer;
            }
            System.arraycopy(buffer, 0, responseBytes, totalBytesRead, bytesRead);
            totalBytesRead += bytesRead;
        }

        return createResponse(responseBytes, totalBytesRead);
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
