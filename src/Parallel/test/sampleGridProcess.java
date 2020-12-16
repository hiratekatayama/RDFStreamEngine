package Parallel.test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;

public class sampleGridProcess implements Callable<String> {
    private String name = null;
    private Socket socket = null;

    public sampleGridProcess(String name, Socket socket) {
        this.name = name;
        this.socket = socket;
    }

    @Override
    public String call() throws Exception {
        System.out.println(System.currentTimeMillis() + " " + this.name + ": grid process started");

        BufferedReader reader = null;
        BufferedWriter writer = null;
        String request, response;

        try {
            reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));

            //仕事を受け取る
            request = reader.readLine();
            System.out.println(System.currentTimeMillis() + " " + this.name + ": grid process request received [" + request + "]");
        } catch (IOException e) {
        }
        return "ok";
    }
}
