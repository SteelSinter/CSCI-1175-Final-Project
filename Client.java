import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application {
	public TextArea taChat = new TextArea();
	public TextArea taMessage = new TextArea();
	public VBox chat = new VBox();
	TextField tfPort = new TextField();
	TextField tfAddress = new TextField();
	TextField tfUserName = new TextField();
	DataInputStream in;
	DataOutputStream out;
	
	@Override
	public void start(Stage mainStage) {
		GridPane gridPane = new GridPane();
		ScrollPane spChat = new ScrollPane();
		Button btConnect = new Button("Connect");
		
		taChat.setEditable(false);
		taChat.setPrefWidth(300);
		taChat.setWrapText(true);
		taChat.setMaxWidth(400);
		taChat.setPrefHeight(250);
		
		taMessage.setMaxHeight(50);
		taMessage.setMaxWidth(400);
		taMessage.setWrapText(true);
		
		tfPort.setPromptText("Port");
		tfPort.setText(String.valueOf(8000));
		tfAddress.setPromptText("Address");
		tfAddress.setText("localhost");
		tfUserName.setPromptText("User name");
		tfUserName.setText("User");
		
		gridPane.add(spChat, 0, 0);
		gridPane.add(taMessage, 0, 1);
		gridPane.add(new VBox(tfPort, tfAddress, btConnect, tfUserName), 1, 0);
		spChat.setContent(chat);
		
		Scene scene = new Scene(gridPane, 500, 300);
		
		btConnect.setOnAction(e -> {
			new Thread(() -> {
				try {
					Socket socket = new Socket(new String(
							tfAddress.getText().trim()) , Integer.valueOf(tfPort.getText()));
					addStatus("Connected to " + socket.getInetAddress());
					connectToServer(socket);
				} catch (NumberFormatException ex) {
					addStatus("Invalid port or address");
				} catch (UnknownHostException ex) {
					addStatus("Server not found");
				} catch (IOException ex) {
					addStatus(ex.toString());
				}
			}).start();
		});
		
		taMessage.setOnKeyPressed(e -> {
			if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
				new Thread(() ->{
					sendMessage();
				}).start();
			}
		});
		
		mainStage.setScene(scene);
		mainStage.setTitle("Client");
		mainStage.show();
	}
	
	public void addStatus(String s) {
		Platform.runLater(() -> {
			chat.getChildren().add(new Label(s + "\r\n"));
		});
		//taChat.appendText(s + "\r\n");
	}
	
	public void sendMessage() {
		try {
			out.writeUTF(tfUserName.getText() + ": " + taMessage.getText().trim());
			out.flush();
			taMessage.clear();
		} catch (SocketException ex) {
			addStatus("Server disconnected");
		}
		catch (IOException ex) {
			addStatus(ex.toString());
		}
		catch (NullPointerException ex) {
			addStatus("Message was not sent");
		}
	}
	
	public void connectToServer(Socket socket) throws IOException {
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		new Thread(() -> {
			try {
				listenForData();
			} catch (EOFException e) {
				addStatus("Server disconnected");
			} catch (Exception e) {
				addStatus(e.toString());
			}
		}).start();
	}
	
	public void listenForData() throws Exception {
		while (true) {
			Thread.sleep(100);
			addStatus(in.readUTF());
		}
	}

	public static void main(String[] args) {
		Application.launch(args);

	}

}
