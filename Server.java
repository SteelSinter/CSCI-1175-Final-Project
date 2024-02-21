import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
	ServerSocket serverSocket;
	TextArea ta = new TextArea();
	public boolean serverStopped = false;
	
	@Override
	public void start(Stage mainStage) {
		VBox pane = new VBox();
		Button btStop = new Button("Shutdown server");
		ta.setEditable(false);
		ta.setStyle("");
		
		pane.getChildren().addAll(ta, btStop);
		Scene scene = new Scene(pane , 300, 300);
		
		// Create server socket
		try {
			serverSocket = new ServerSocket(8000);
			addStatus("Server " + InetAddress.getLocalHost() + " hosted on port " + serverSocket.getLocalPort());
		} catch (IOException e) {
			addStatus(e.toString());
			addStatus("Server was unable to create socket");
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
			while (!serverStopped) {
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
