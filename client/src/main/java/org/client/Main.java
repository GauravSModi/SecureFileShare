package org.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

public class Main {


    public static boolean cont = true;

    public static void main(String[] args) {

        System.out.println("Enter \"Help\" for usage hints.");



        try (
                Socket socket = new Socket();
                Scanner scanner = new Scanner(System.in);
        ) {
            while (cont) {
                System.out.print("Please enter command: ");
                String command = scanner.nextLine();

                CommandHandler.handleCommand(command);

            }
        } catch (IOException e) {
            System.err.println("Error creating socket: " + e.getMessage());
        }
    }
}