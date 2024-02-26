import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.awt.image.BufferedImage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Client extends Application {
	//public TextArea taChat = new TextArea();
	TextArea taMessage = new TextArea(); // Where the user enters text to send
	VBox chat = new VBox(); // Content for the chat
	ScrollPane spChat = new ScrollPane(); // Scroll pane to contain the chat
	
	TextField tfPort = new TextField();
	TextField tfAddress = new TextField();
	TextField tfUserName = new TextField();
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	@Override
	public void start(Stage mainStage) {
		GridPane gridPane = new GridPane();
		Button btConnect = new Button("Connect");
		// TODO: Add FIle chooser
		
	//	taChat.setEditable(false);
	//	taChat.setPrefWidth(300);
	//	taChat.setWrapText(true);
	//	taChat.setMaxWidth(400);
	//	taChat.setPrefHeight(250);
		
		//chat.setPadding(new javafx.geometry.Insets(1));
		chat.setPrefHeight(200);
		chat.setSpacing(1);
		
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
	
	public void addStatus(Object o) {
		Platform.runLater(() -> {
			chat.getChildren().add((Node) o);
			spChat.setVvalue(1.0d);
		});
	}
	
	public void addStatus(String s) {
		Platform.runLater(() -> {
			Label msg = new Label(s + "\r\n");
			msg.setMaxHeight(1);
			chat.getChildren().add(msg);
			VBox.setMargin(msg, new javafx.geometry.Insets(1));
			spChat.setVvalue(1.0d);
		});
		//taChat.appendText(s + "\r\n");
	}
	
	public void sendMessage() {
		try {
			if (false/** can send image **/) {
				// send image
			}
			else {
				out.writeObject(new Label(taMessage.getText()));
				taMessage.clear();
			}
			out.flush();
			//out.writeUTF(tfUserName.getText() + ": " + taMessage.getText().trim());
			//out.flush();
			//taMessage.clear();
		} catch (SocketException ex) {
			addStatus("Server disconnected");
		} catch (IOException ex) {
			addStatus(ex.toString());
		} catch (NullPointerException ex) {
			addStatus("Message was not sent");
		}
	}
	
	public void connectToServer(Socket socket) throws IOException {
		System.out.println("Connecting to server");
		out = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
		in = new ObjectInputStream(new DataInputStream(socket.getInputStream()));
		System.out.println("out toString:");
		System.out.println(out.toString());
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
			Object o = in.readObject();
			addStatus(o);
		}
	}

	public static void main(String[] args) {
		Application.launch(args);

	}
	
	public static void writeJPG(BufferedImage bufferedImage, OutputStream outputStream, 
			float quality) throws IOException {
		
		    Iterator<ImageWriter> iterator =
		        ImageIO.getImageWritersByFormatName("jpg");
		    ImageWriter imageWriter = iterator.next();
		    ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
		    imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		    imageWriteParam.setCompressionQuality(quality);
		    ImageOutputStream imageOutputStream =
		        new MemoryCacheImageOutputStream(outputStream);
		    imageWriter.setOutput(imageOutputStream);
		    IIOImage iioimage = new IIOImage(bufferedImage, null, null);
		    imageWriter.write(null, iioimage, imageWriteParam);
		    imageOutputStream.flush();
		}

}

class Message implements java.io.Serializable {
	String s = null;
	Image i = null;
}
