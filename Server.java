import java.awt.image.BufferedImage;
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
/**
 * 
 * @author James Jesus
 * 2/21/24
 * 
 * Server can connect to multiple clients using a thread for each.
 *
 */
public class Server extends Application {
	public ServerSocket serverSocket;
	/**
	 * TextArea to show chat history and debug messsages.
	 */
	public TextArea ta = new TextArea();
	/**
	 * Indicates wether the server has been stopped.
	 */
	public boolean serverStopped = false;
	public ObjectOutputStream output;
	/**
	 * HashMap to store sockets and the ObjectOutputStreams for those Objects.
	 */
	java.util.HashMap<Socket, ObjectOutputStream> sockets = new java.util.HashMap<Socket, ObjectOutputStream>();
	/**
	 * Start method
	 */
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
	/**
	 * Thread to wait for clients to connect and make a new thread for that client.
	 */
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
	/**
	 * Establish input/output streams for a client. Waits for objects and sends
	 * that object to each client.
	 * @param socket Socket the client is connected to.
	 */
	public void connectToClient(Socket socket) {
		ObjectInputStream in;
		ObjectOutputStream out;
		try {
			addStatus("Connected to " + socket.getInetAddress());
			in = new ObjectInputStream(new DataInputStream(socket.getInputStream()));
			out = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
			sockets.put(socket, out); // Add the socket and OutputStream to a list.
			while (!serverStopped) {
				//addStatus("Waiting for data...");
				Object o = null;
				o = in.readObject();
				addStatus(o.toString());
				//addStatus("Sending data...");
				
				for (Socket s: sockets.keySet()) {
					//addStatus("Sending to " + s.toString());
					output = sockets.get(s);
					//addStatus("Writing...");
					output.writeObject(o);
					//addStatus("Flushing...");
					output.flush();
					Thread.yield();
					//addStatus("Done");
				}
			}
		} catch (NullPointerException e) {
			addStatus("Null object");
		} catch (EOFException e) {
			addStatus("Client " + socket.getInetAddress().getHostName() + " disconnected");
			sockets.remove(socket);
		} catch (IOException e) {
			addStatus("While waiting for client data: " + e.toString());
			sockets.remove(socket);
		} catch (ClassNotFoundException e) {
			addStatus("While waiting for client data: " + e.toString());
		}
		sockets.remove(socket);
	}
	/**
	 * Appends the String to the TextArea.
	 * @param s String to append.
	 */
	public void addStatus(String s) {
		ta.appendText(new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + ": " + s + "\n");
	}
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);

	}

}