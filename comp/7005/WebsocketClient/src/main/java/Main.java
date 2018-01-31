//import com.google.gson.Gson;
//import com.neovisionaries.ws.client.WebSocket;
//import com.neovisionaries.ws.client.WebSocketAdapter;
//import com.neovisionaries.ws.client.WebSocketFactory;
//import Command;
//import Message;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.UUID;
//
//import static Command.CONNECT;
//import static Command.UPLOAD;
//
///**
// * Created by danielklein on 2017-09-20.
// */
//public class Main {
//
//    private final static String URL = "ws://localhost:8080/connect";
//
//    public static void main (String[] args) throws Exception {
//        for (int i = 0; i < 10; i++) {
//            String filename = UUID.randomUUID().toString() + ((i % 2 == 0) ? ".txt" : ".bat");
//            File dir = new File("out");
//            if(!dir.exists()) {
//                dir.mkdir();
//            }
////            File file = new File(filename);
////            file.createNewFile();
//            Path path = Paths.get("out/" + filename);
//            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
//                for(int j = 0; j < 10; j++) {
//                    writer.write(UUID.randomUUID().toString() + "\n");
//                }
//            }
//            Message message = new Message();
//            message.setCommand(CONNECT);
//            message.setMessage("ws://localhost:7006/connect");
//            Gson gson = new Gson();
//
//            WebSocket web1 = new WebSocketFactory()
//                    .createSocket("ws://localhost:7005/connect")
//                    .addListener(new WebSocketAdapter() {
//                        @Override
//                        public void onTextMessage(WebSocket ws, String message) {
//                            // Received a response. Print the received message.
//                            System.out.println(message);
//                        }
//                    })
//                    .connect()
//                    .sendText(gson.toJson(message));
//            Thread.sleep(1000);
//
//            Message message1 = new Message();
//            message1.setCommand(Command.DOWNLOAD);
//            message1.setMessage(filename);
//
//            new WebSocketFactory()
//                    .createSocket("ws://localhost:8080/file")
//                    .addListener(new WebSocketAdapter() {
//                        @Override
//                        public void onTextMessage(WebSocket ws, String message) {
//                            // Received a response. Print the received message.
//                            System.out.println(message);
//                        }
//
//                        @Override
//                        public void onBinaryMessage(WebSocket websocket, byte[] binary) {
//                            File dir = new File("inc");
//                            if(!dir.exists()) {
//                                dir.mkdir();
//                            }
//                            File file = new File("inc" + "/" + filename);
//                            try {
//                                file.createNewFile();
//                                FileOutputStream out = new FileOutputStream(file);
//                                out.write(binary);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    })
//                    .connect()
//                    .sendBinary(Files.readAllBytes(path)).sendText(gson.toJson(message1))
//                    .sendClose();
//
//                    Thread.sleep(1000);
//                    web1.sendClose();
//
//        }
//    }
//}
