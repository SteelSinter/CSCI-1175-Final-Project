import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Server extends Application {
	ServerSocket serverSocket;
	TextArea ta = new TextArea();
	
	@Override
	public void start(Stage mainStage) {
		ta.setEditable(false);
		Scene scene = new Scene(ta, 200, 300);
		
		// Create server socket
		try {
			serverSocket = new ServerSocket(8000);
		} catch (IOException e) {
			addStatus(e.toString());
		}
		
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
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				new Thread(() -> {
					connectToClient(socket);
					addStatus("Connected to " + socket.getInetAddress());
				}).start();
			} catch (IOException e) {
				addStatus(e.toString());
			}
		}
	}
	
	public void connectToClient(Socket socket) {
		DataInputStream in;
		DataOutputStream out;
		
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			while (true) {
				addStatus(String.valueOf(in.read()));
			}
		} catch (IOException e) {
			addStatus(e.toString());
		}
	}
	
	public void addStatus(String s) {
		ta.appendText(s + "\r\n");
	}

	public static void main(String[] args) {
		Application.launch(args);

	}

}
