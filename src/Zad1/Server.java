package Zad1;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Server {
	private Map<SocketChannel, String> clients;
	private Map<String, List<String>> clientsTopics;
	private Map<String, Map<Integer, String>> _articles;
	private List<String> _topics;

	private int counter = 0;

	public static void main(String[] args) throws IOException, InterruptedException {
		new Server();
	}

	Server() throws IOException {
		clients = new HashMap<SocketChannel, String>();
		_articles = new HashMap<String, Map<Integer, String>>();
		clientsTopics = new HashMap<String, List<String>>();
		String host = "localhost";
		int port = 10666;

		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		Selector selector = Selector.open();

		serverChannel.socket().bind(new InetSocketAddress(host, port));
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		LoadTopics();
		LoadArticles();
		LoadClientsTopics();

		PrintAllArticles();

		System.out.println("Serwer: czekam ... ");

		while (true) {
			selector.select();
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iter = keys.iterator();

			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				iter.remove();

				if (key.isAcceptable()) { // połaczenie klienta gotowe do akceptacji

					System.out.println("Serwer: ktoś się połączył ..., akceptuję go ... ");
					SocketChannel cc = serverChannel.accept();
					cc.configureBlocking(false);
					cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
					continue;
				}

				if (key.isReadable()) { // któryś z kanałów gotowy do czytania
					SocketChannel cc = (SocketChannel) key.channel();
					serviceRequest(cc);
					continue;
				}
				if (key.isWritable()) { // któryś z kanałów gotowy do pisania

					// Uzyskanie kanału
					// SocketChannel cc = (SocketChannel) key.channel();

					// pisanie do kanału
					// ...
					continue;
				}

			}
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void PrintAllArticles() {
		Set set = _articles.entrySet();
		Iterator iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Set setArticles = ((Map<Integer, String>) entry.getValue()).entrySet();
			Iterator iteratorArticles = setArticles.iterator();

			System.out.println("Topic: " + entry.getKey());
			while (iteratorArticles.hasNext()) {
				Map.Entry entryArticle = (Map.Entry) iteratorArticles.next();
				System.out.println("key : " + entryArticle.getKey() + " & Value : ");
				System.out.println("\t" + entryArticle.getValue());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void LoadClientsTopics() {
		clientsTopics = new HashMap<String, List<String>>();

		try {
			FileInputStream fileInput = new FileInputStream("ClientsTopics.dat");
			ObjectInputStream objectInput = new ObjectInputStream(fileInput);

			clientsTopics = (HashMap<String, List<String>>) objectInput.readObject();

			objectInput.close();
			fileInput.close();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.out.println("Class not found");
			System.out.println(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void LoadArticles() {
		_articles = new HashMap<String, Map<Integer, String>>();

		try {
			FileInputStream fileInput = new FileInputStream("Articles.dat");
			ObjectInputStream objectInput = new ObjectInputStream(fileInput);

			_articles = (HashMap<String, Map<Integer, String>>) objectInput.readObject();

			objectInput.close();
			fileInput.close();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.out.println("Class not found");
			System.out.println(ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void LoadTopics() {
		_topics = new ArrayList<String>();

		try {
			FileInputStream fileInput = new FileInputStream("Topics.dat");
			ObjectInputStream objectInput = new ObjectInputStream(fileInput);

			_topics = (ArrayList<String>) objectInput.readObject();

			objectInput.close();
			fileInput.close();
		} catch (IOException ex) {
			System.out.println(ex.getMessage());
		} catch (ClassNotFoundException ex) {
			System.out.println("Class not found");
			System.out.println(ex.getMessage());
		}
	}

	private void SaveTopics() {
		try {
			FileOutputStream myFileOutStream = new FileOutputStream("Topics.dat");

			ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream);

			myObjectOutStream.writeObject(_topics);
			myObjectOutStream.close();
			myFileOutStream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private void SaveArticles() {
		try {
			FileOutputStream myFileOutStream = new FileOutputStream("Articles.dat");

			ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream);

			myObjectOutStream.writeObject(_articles);
			myObjectOutStream.close();
			myFileOutStream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void SaveClientsTopics() {
		try {
			FileOutputStream myFileOutStream = new FileOutputStream("ClientsTopics.dat");

			ObjectOutputStream myObjectOutStream = new ObjectOutputStream(myFileOutStream);

			myObjectOutStream.writeObject(clientsTopics);
			myObjectOutStream.close();
			myFileOutStream.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	private static Charset charset = Charset.forName("ISO-8859-2");
	private static final int BSIZE = 1024;
	private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);
	private StringBuffer reqString = new StringBuffer();

	private void serviceRequest(SocketChannel socketChannel) {
		if (!socketChannel.isOpen())
			return; // jeżeli kanał zamknięty
		reqString.setLength(0);
		bbuf.clear();

		try {
			readLoop: // Czytanie jest nieblokujące
			while (true) { // kontynujemy je dopóki
				// System.out.println("Read...");
				int n = socketChannel.read(bbuf); // nie natrafimy na koniec wiersza
				if (n > 0) {
					bbuf.flip();
					CharBuffer cbuf = charset.decode(bbuf);
					while (cbuf.hasRemaining()) {
						char c = cbuf.get();
						// System.out.println(c);
						if (c == '\r' || c == '\n')
							break readLoop;
						else {
							// System.out.println(c);
							reqString.append(c);
						}
					}
				}
			}

			String cmd = reqString.toString();
			System.out.println("cmd: " + cmd);

			if (cmd.equals("Login")) {
				System.out.println("Admin" + counter);

				clients.put(socketChannel, "Admin" + counter++);
				socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
			} else if (cmd.contains(new StringBuffer("LoginClient"))) {
				String clientName = cmd.split(":")[1];
				if (clients.containsValue(clientName))
					socketChannel
							.write(charset.encode(CharBuffer.wrap("Client already log in, please restart Your app.")));
				else {
					clients.put(socketChannel, clientName);

					System.out.println("Client log in: " + cmd.split(":")[1]);
					socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
				}
			} else if (cmd.equals("GetListOfTopics")) {
				String message = "";
				for (String item : _topics)
					message += item + ":";
				message.substring(0, message.length() - 1);

				System.out.println(message);

				socketChannel.write(charset.encode(CharBuffer.wrap(message)));
			} else if (cmd.equals("CheckConnection")) {
				System.out.println(clients.get(socketChannel));
				socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
			} else if (cmd.equals("Close")) { // koniec komunikacji
				clients.remove(socketChannel);

				System.out.println("Left connected clients:");
				if (!clients.isEmpty())
					for (Map.Entry<SocketChannel, String> entry : clients.entrySet())
						System.out.println("/t*Key = " + entry.getKey() + ", Value = " + entry.getValue());
				else
					System.out.println("/t*Empty - No clients");

				socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
				socketChannel.close(); // - zamknięcie kanału
				socketChannel.socket().close(); // i gniazda
			} else if (cmd.contains(new StringBuffer("AddTopic"))) {
				if (!_topics.contains(cmd.split(":")[1])) {
					_topics.add(cmd.split(":")[1]);

					System.out.println("Add new topic: " + cmd.split(":")[1]);

					SaveTopics();
					socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
				} else {
					socketChannel.write(charset.encode(CharBuffer.wrap("Topic is already added.")));
				}
			} else if (cmd.contains(new StringBuffer("RemoveTopic"))) {
				String topic = cmd.split(":")[1];

				if (_topics.contains(topic)) {
					_topics.remove(topic);

					System.out.println("Topic removed: " + topic);

					_articles.remove(topic);

					SaveTopics();
					SaveArticles();

					socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
				} else {
					socketChannel.write(charset.encode(CharBuffer.wrap("Topic don't exists.")));
				}
			} else if (cmd.contains(new StringBuffer("AddNews"))) {
				String topic = "";
				String article = "";

				try {
					topic = cmd.split(":")[1];
					article = cmd.split(":")[2];
				} catch (Exception e) {

				}

				if (!topic.isEmpty() && !article.isEmpty()) {
					if (_topics.contains(topic)) {
						if (_articles.containsKey(topic)) {
							Map<Integer, String> tmp = _articles.get(topic);
							tmp.put(tmp.size() + 1, article);

							System.out.println("New article was been added on topic: " + topic + " => " + article);
						} else {
							Map<Integer, String> tmp = new HashMap<Integer, String>();
							tmp.put(1, article);

							_articles.put(topic, tmp);

							System.out.println("New article was been added on topic: " + topic + " => " + article);
						}

						SaveArticles();
						socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
					} else {
						socketChannel.write(charset.encode(CharBuffer.wrap("Topic don't exists.")));
					}
				} else {
					socketChannel.write(charset.encode(CharBuffer.wrap("Topic or article was empty.")));
				}
			} else if (cmd.contains(new StringBuffer("AddSubscriptionTopic"))) {
				String clientName = cmd.split(":")[1];
				String topicName = cmd.split(":")[2];

				if (clientsTopics.containsKey(clientName)) {
					List<String> topics = clientsTopics.get(clientName);
					if (topics.contains(topicName))
						socketChannel.write(charset.encode(CharBuffer.wrap("Topic already exists.")));
					else {
						topics.add(topicName);
						socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
					}
				} else {
					List<String> topics = new ArrayList<String>();
					topics.add(topicName);
					
					clientsTopics.put(clientName, topics);
					
					socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
				}
				
				SaveClientsTopics();
			} else if (cmd.contains(new StringBuffer("RemoveSubscriptionTopic"))) {
				String clientName = cmd.split(":")[1];
				String topicName = cmd.split(":")[2];

				System.out.println("RemoveSubscriptionTopic Start");
				
				if (clientsTopics.containsKey(clientName)) {
					List<String> topics = clientsTopics.get(clientName);
					if (topics.contains(topicName)) {
						topics.remove(topicName);
						socketChannel.write(charset.encode(CharBuffer.wrap("OK")));
					}
					else {
						socketChannel.write(charset.encode(CharBuffer.wrap("Topic don't exists.")));
					}
				} else {
					socketChannel.write(charset.encode(CharBuffer.wrap("Client don't exists.")));
				}
				
				SaveClientsTopics();
			} else if (cmd.contains(new StringBuffer("GetClientListOfTopics"))) {
				String message = "";
				String clientName = cmd.split(":")[1];
				
				System.out.println("GetClientListOfTopics Start");
				
				for (String item : clientsTopics.get(clientName))
					message += item + ":";
				message.substring(0, message.length() - 1);

				System.out.println(message);

				socketChannel.write(charset.encode(CharBuffer.wrap(message)));
			} else if (cmd.contains(new StringBuffer("LoadArticlesForTopic"))) {
				String message = "";
				String topicsName = cmd.split(":")[1];
				
				System.out.println("LoadArticlesForTopic Start");
				
				if (_articles.containsKey(topicsName)) {
					Map<Integer, String> articles = _articles.get(topicsName);
					Set<Entry<Integer, String>> set = articles.entrySet();
					Iterator<Entry<Integer, String>> iterator = set.iterator();

					while (iterator.hasNext()) {
						Entry<Integer, String> entry = iterator.next();

						message += entry.getValue() + ":";
					}
					
					message.substring(0, message.length() - 1);
					
					System.out.println(message);
					
					socketChannel.write(charset.encode(CharBuffer.wrap(message)));
				} else
					socketChannel.write(charset.encode(CharBuffer.wrap(":")));
			}
			
		} catch (Exception exc) { // przerwane polączenie?
			exc.printStackTrace();
			try {
				clients.remove(socketChannel);
				socketChannel.close();
				socketChannel.socket().close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
