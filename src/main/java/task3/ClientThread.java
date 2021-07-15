package task3;

import helper.Pair;

import java.io.*;
import java.net.Socket;

public class ClientThread extends Thread{
    private Socket socket;
    private String mask;
    private int depth;
    private DataOutputStream out;
    private BufferedReader in;
    public ClientThread(Socket clientSocket){
        socket = clientSocket;
    }
    public void run(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("connected client: " + socket.getInetAddress().toString());
            out.writeUTF("Please provide depth" + System.lineSeparator());
            out.flush();
            do {
                try {
                    depth = Integer.parseInt(in.readLine());
                } catch (NumberFormatException e) {
                    out.writeUTF("Please provide a number" + System.lineSeparator());
                    continue;
                }
                break;
            }while(true);
            out.writeUTF("Please provide mask" + System.lineSeparator());
            mask = in.readLine();
            System.out.println(depth + " " + mask);
            Main.addRequest(socket, new Pair<>(depth, mask));

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
