import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.omg.IOP.Encoding;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;

public class SocketHandler {
    private Socket socket;
    private BufferedReader bufferedReader;
    private OnDataListener onDataListener;

    private SocketHandler(Socket socket) throws IOException {
        this.socket = socket;
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String line = bufferedReader.readLine();
                        if (line != null) {
                            String data = URLDecoder.decode(line, "UTF-8");
                            if (onDataListener != null) {
                                onDataListener.onData(new Data(data));
                            }
                        } else {
                            System.err.println("Connection was interrupted");
                            System.exit(1);
                        }
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        JSONObject init = new JSONObject();
        init.put("type", "init");
        init.put("mac", getMACAddress());
        init.put("os", System.getProperty("os.name")    );
        socket.getOutputStream().write(init.toString().getBytes());
    }

    public static SocketHandler connect(String host, int port) throws IOException {
        return new SocketHandler(new Socket(host, port));
    }

    @FunctionalInterface
    public interface OnDataListener {
        void onData(Data data);
    }

    public void write(String data) throws IOException {
        socket.getOutputStream().write(data.getBytes());
    }

    public SocketHandler.OnDataListener getOnDataListener() {
        return onDataListener;
    }

    public void setOnDataListener(SocketHandler.OnDataListener onDataListener) {
        this.onDataListener = onDataListener;
    }

    public static String getMACAddress() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            return sb.toString();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
            return null;
        }
    }
}
