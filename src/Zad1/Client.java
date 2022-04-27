package Zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Client {
	private SocketChannel _channel;
	private ServerSocketChannel _serverSocketChannel;
	public String clientName;
	private Charset _charset;
	ByteBuffer _inByteBuffer;
	CharBuffer _charBuffer;

	public static void main(String[] args) throws IOException {
		ClientGUI.launch(ClientGUI.class, args);
	}

	public String SendMessageWithStringReturn(String serverCommand) throws IOException {
		int readBytes;
		_channel.write(_charset.encode(serverCommand + "\n"));
		System.out.println("Wait for replay.");
		
		do
			readBytes = _channel.read(_inByteBuffer);
		while (readBytes == 0);

		_inByteBuffer.flip();
		_charBuffer = _charset.decode(_inByteBuffer);
		String replayFromServer = _charBuffer.toString();

		System.out.println("Server replay: " + replayFromServer);
		_inByteBuffer.clear();
		_charBuffer.clear();

		return replayFromServer;
	}

	public List<String> GetListOfTopicsOnServer() throws IOException {
		List<String> topics = new ArrayList<>();

		String replayFromServer = SendMessageWithStringReturn("GetListOfTopics");

		if (replayFromServer.isEmpty()) {
			_charBuffer.clear();
			topics.add("");
			return topics;
		}

		topics.add("");
		for (String item : replayFromServer.split(":"))
			topics.add(item);

		System.out.println("Klient: serwer właśnie odpisał ... " + replayFromServer);
		return topics;
	}
	
	public String Close() throws IOException {
		return SendMessageWithStringReturn("Close");
	}

	Client(String server, int port) throws IOException, UnknownHostException, Exception {
		_channel = SocketChannel.open();
		_channel.configureBlocking(false);
		// channel.setOption(SocketOption<String>, "Admin");
		_channel.connect(new InetSocketAddress(server, port));
		System.out.println("Klient: łączę się z serwerem ...");

		while (!_channel.finishConnect()) {
		}

		_charset = Charset.forName("ISO-8859-2");
		int rozmiar_bufora = 1024;
		_inByteBuffer = ByteBuffer.allocateDirect(rozmiar_bufora);
		_charBuffer = null;
	}
	
	public String LoginToServer(String clientLogin) throws IOException {
		clientName = clientLogin;
		return SendMessageWithStringReturn("LoginClient:" + clientLogin);
	}

	public String RemoveTopicSubscription(String selectedItem) throws IOException {
		return SendMessageWithStringReturn("RemoveSubscriptionTopic:" + clientName + ":" + selectedItem);
	}

	public String AddTopicSubscription(String selectedItem) throws IOException {
		return SendMessageWithStringReturn("AddSubscriptionTopic:" + clientName + ":" + selectedItem);
	}

	public String[] LoadClientTopics() throws IOException {
		return SendMessageWithStringReturn("GetClientListOfTopics:" + clientName).split(":");
	}

	public String[] LoadArticlesForTopic(String topic) throws IOException {
		return SendMessageWithStringReturn("LoadArticlesForTopic:" + topic).split(":");
	}

	public void WaitForServer(ClientGUI clientGUI) throws IOException {
//		while (true) {
//			_inByteBuffer.clear();
//			int readBytes = _channel.read(_inByteBuffer);
//			
//			if (readBytes > 0) {
//				
//			}
//		}
	}
}
