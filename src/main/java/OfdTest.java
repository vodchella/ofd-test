import ofd.proto.Message;

public class OfdTest {

    public static void main(String[] args) throws Exception {
        OfdConnector ofd = new OfdConnector(600, 647);
        Message.Response response = ofd.request(
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
