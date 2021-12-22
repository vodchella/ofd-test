import com.google.protobuf.InvalidProtocolBufferException;
import ofd.proto.Message;

import java.io.*;
import java.net.Socket;

public class OfdTest {

    private static byte[] createRequestPacket(byte[] body) {
        OfdHeader header = new OfdHeader()
                .setVersion((short) 202)
                .setSize(OfdHeader.SIZE + body.length)
                .setToken(868)
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

        byte[] responseBytes = new byte[8192];
        int bytesRead = in.read(responseBytes);
        return createResponse(responseBytes, bytesRead);
    }

    public static void main(String[] args) throws IOException {
        Message.Response response = sendOfdRequest(
                Message.Request.newBuilder()
                        .setCommand(Message.CommandTypeEnum.COMMAND_SYSTEM)
                        .build()
        );
        if (response != null) {
            System.out.printf("Result: %s%n", response.getResult().getResultText());
        }

        System.out.println("Ofd test!");
    }
}
