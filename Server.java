import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Server extends Application {
	public ServerSocket serverSocket;
	public TextArea ta = new TextArea();
	public boolean serverStopped = false;
	java.util.ArrayList<Socket> clients = new java.util.ArrayList<Socket>();
	
	@Override
	public void start(Stage mainStage) {
		VBox pane = new VBox();
		Button btStop = new Button("Shutdown server");
		ta.setEditable(false);
		ta.setStyle("");
		ta.setWrapText(true);
		ta.setPrefHeight(500);
		
		btStop.setStyle("-fx-background-color: red;");
		btStop.setTextFill(javafx.scene.paint.Color.WHITE);
		
		pane.getChildren().addAll(ta, btStop);
		Scene scene = new Scene(pane , 500, 300);
		
		// Create server socket
		try {
			serverSocket = new ServerSocket(8000);
			addStatus("Server " + InetAddress.getLocalHost() + " hosted on port " 
			+ serverSocket.getLocalPort());
		} catch (IOException e) {
			addStatus(e.toString());
			addStatus("Server was unable to create socket");
			System.exit(1);
		}
		
		btStop.setOnAction(e -> {
			serverStopped = true;
			System.exit(0);
		});
		
		// Connect to clients
		addStatus("Waiting for connections...");
		new Thread(() -> {
			waitForClients();
		}).start();
		
		mainStage.setTitle("Server");
		mainStage.setScene(scene);
		mainStage.show();
		
	}
	
	public void waitForClients() {
		while (!serverStopped) {
			try {
				Socket socket = serverSocket.accept();
				new Thread(() -> {
					connectToClient(socket);
				}).start();
			} catch (IOException e) {
				addStatus(e.toString());
			}
		}
	}
	
	public void connectToClient(Socket socket) {
		ObjectInputStream in;
		ObjectOutputStream out;
		clients.add(socket);
		try {
			addStatus("Connected to " + socket.getInetAddress());
			in = new ObjectInputStream(new DataInputStream(socket.getInputStream()));
			out = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));//////////
			while (!serverStopped) {
				Object o = in.readObject();//	The problem is that this needs a way to get each output
				addStatus(o.toString()); // 	stream to send the objects back to the clients.
				for (Socket s: clients) { //	This is attempting to use the same outputstream for each client.
					out = new ObjectOutputStream(new DataOutputStream(s.getOutputStream()));
					out.writeObject(o);
					out.flush();
					Thread.yield();
				}
			}
		} catch (EOFException e) {
			addStatus("Client " + socket.getInetAddress().getHostName() + " disconnected");
			clients.remove(socket);
		} catch (IOException e) {
			addStatus("While waiting for client data: " + e.toString());
			clients.remove(socket);
		} catch (ClassNotFoundException e) {
			addStatus("While waiting for client data: " + e.toString());
		}
		clients.remove(socket);
	}
	
	public void addStatus(String s) {
		ta.appendText(new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + ": " + s + "\r\n");
	}

	public static void main(String[] args) {
		Application.launch(args);

	}

}
