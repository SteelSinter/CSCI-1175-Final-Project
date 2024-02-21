import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application {
	TextArea taChat = new TextArea();
	
	@Override
	public void start(Stage mainStage) {
		TextArea taMessage = new TextArea();
		GridPane gridPane = new GridPane();
		Button btConnect = new Button("Connect");
		TextField tfPort = new TextField();
		TextField tfAddress = new TextField();
		
		taChat.setEditable(false);
		taChat.setPrefWidth(300);
		taChat.setWrapText(true);
		taChat.setMaxWidth(400);
		
		taMessage.setMaxHeight(50);
		taMessage.setMaxWidth(400);
		taMessage.setWrapText(true);
		
		tfPort.setPromptText("Port");
		tfAddress.setPromptText("Address");
		
		
		gridPane.add(taChat, 0, 0);
		gridPane.add(taMessage, 0, 1);
		gridPane.add(new VBox(tfPort, tfAddress, btConnect), 1, 0);
		
		Scene scene = new Scene(gridPane, 500, 300);
		
		btConnect.setOnAction(e -> {
			new Thread(() -> {
				try {
					Socket socket = new Socket("localhost" , 8000);
					addStatus("Connected to " + socket.getInetAddress());
					connectToServer(socket);
				} catch (NumberFormatException ex) {
					addStatus("Invalid port or address");
				} catch (UnknownHostException ex) {
					addStatus("Unable to connect");
				} catch (IOException ex) {
					addStatus(ex.toString());
				}
			}).start();
		});
		
		mainStage.setScene(scene);
		mainStage.setTitle("Client");
		mainStage.show();
	}
	
	public void addStatus(String s) {
		taChat.appendText(s + "\r\n");
	}
	
	public void connectToServer(Socket socket) {
		DataInputStream in;
		DataOutputStream out;
		
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			addStatus(e.toString());
		}
	}

	public static void main(String[] args) {
		Application.launch(args);

	}

}
