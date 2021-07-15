package task3;

import helper.Pair;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {
    private static String rootPath;
    private static BlockingDeque<Pair<Socket, String>> resultsQueue;
    private static BlockingDeque<Pair<Socket, Pair<Integer, String>>> requestsQueue;
    private static int port;
    private static ServerSocket socket;
    public Main(int port, String rootPath){
        this.rootPath = rootPath;
        this.port = port;
        resultsQueue = new LinkedBlockingDeque<>();
        requestsQueue = new LinkedBlockingDeque<>();
    }
    public static void addRequest(Socket socket, Pair<Integer, String> request){
        requestsQueue.add(new Pair<>(socket, request));
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=2){
            throw new IllegalArgumentException("Wrong number of arguments! \nUsage: [port] [rootPath] .");
        }
        int port = Integer.parseInt(args[0]);
        String rootPath = args[1];
        Main main = new Main(port, rootPath);
        main.startServer().start();
        main.handleRequests();
    }
    private Thread startServer(){
        return new Thread(()-> {
            try {
                socket = new ServerSocket(port);

                while (!Thread.currentThread().isInterrupted()) {
                    Socket client = socket.accept();
                    ClientThread clientThread = new ClientThread(client);
                    clientThread.start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            }
        );
    }
    private void handleRequests(){
        printResults().start();
        while(true){
            Pair<Socket, Pair<Integer, String>> request;
            if((request=requestsQueue.poll())!=null) {
                searchFilesIterative(request.getKey(), request.getValue().getKey(), request.getValue().getValue());
            }
        }
    }
    private static Thread printResults(){
        return new Thread(()->{
            Pair<Socket, String> result;
            while(!Thread.currentThread().isInterrupted()){
                if((result=resultsQueue.poll())!=null) {
                    if(result.getValue().isEmpty()){
                        try {
                            result.getKey().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    DataOutputStream outputStream;
                    try {
                        outputStream = new DataOutputStream(result.getKey().getOutputStream());
                        String strToSend = result.getValue();
                        System.out.println(strToSend);
                        outputStream.writeBytes(strToSend + System.lineSeparator());
                        outputStream.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    public static void searchFilesIterative(Socket socket, int maxDepth, String mask){

        File root = new File(rootPath);
        Deque<Pair<File, Integer>> fileStack = new ArrayDeque<>();
        fileStack.push(new Pair<>(root, 0));
        while(!fileStack.isEmpty()){
            Pair current = fileStack.pop();
            File currentFile = (File) current.getKey();
            int currentDepth = (int) current.getValue();
            File[] files = currentFile.listFiles();
            for(File file: files){
                if(file.isDirectory()){
                    if (currentDepth < maxDepth) fileStack.push(new Pair<>(file, currentDepth+1));
                }
                if (currentDepth+1 == maxDepth && file.getName().contains(mask)){
                    resultsQueue.add(new Pair<>(socket, file.getAbsolutePath() + (file.isDirectory() ? "\\": "")));
                }
            }
        }
        resultsQueue.add(new Pair<>(socket,""));
    }
}
