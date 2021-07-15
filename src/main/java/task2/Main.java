package task2;

import helper.Pair;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {
    private String rootPath;
    private int maxDepth;
    private String mask;
    private BlockingDeque<String> resultsQueue;
    public Main(String rootPath, int maxDepth, String mask){
        this.rootPath = rootPath;
        this.maxDepth = maxDepth;
        this.mask = mask;
        resultsQueue = new LinkedBlockingDeque<>();
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=3){
            throw new IllegalArgumentException("Wrong number of arguments! \nUsage: [rootPath] [maxDepth] [mask].");
        }
        String rootPath = args[0];
        int maxDepth = Integer.parseInt(args[1]);
        String mask = args[2];
        Main main = new Main(rootPath, maxDepth , mask);
        main.searchFilesIterative();
    }
    private Thread printResults(){
        return new Thread(()->{
            String result;
            while(true){
                if((result=resultsQueue.poll())!=null) {
                    if(result.isEmpty())break;
                    System.out.println(result);
                }
            }
        });
    }
    public void searchFilesIterative(){
        printResults().start();
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
                    resultsQueue.add(file.getAbsolutePath() + (file.isDirectory() ? "\\": ""));
                }
            }
        }
        resultsQueue.add("");
    }
}
