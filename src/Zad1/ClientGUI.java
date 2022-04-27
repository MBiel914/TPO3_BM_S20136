package Zad1;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientGUI extends Application {
	private static Client _client;

	private ListView<String> topicsList;
	private ListView<String> newsList;

	Label labelStatus;

	TextField textFieldAddTopic;
	TextField textFieldAddNewNewsOnTopic;
	TextField textFieldStatus;

	ComboBox<String> comboBoxSelectTopic;

	Button buttonAddTopicSubscription;
	Button buttonRemoveTopicSubscription;
	Button buttonClose;

	private String InitAdminClient() {
		String returnMessage;
		try {
			_client = new Client("localhost", 10666);
			returnMessage = _client.LoginToServer(ClientLogin());
		} catch (IOException e) {
			return e.getMessage();
		} catch (Exception e) {
			return e.getMessage();
		}

		return "Login: " + returnMessage;
	}

	private String ClientLogin() {
		TextInputDialog inputLogin = new TextInputDialog("Client1");
		inputLogin.setContentText("Login:");
		inputLogin.setHeaderText("Please enter your login");

		do
			inputLogin.showAndWait();
		while (inputLogin.getEditor().getText().equals(""));

		return inputLogin.getEditor().getText();
	}

	private List<String> GetListOfTopicsOnServer() throws IOException {
		return _client.GetListOfTopicsOnServer();
	}

	public void ReloadComboBoxs() throws IOException {
		comboBoxSelectTopic.getItems().clear();
		comboBoxSelectTopic.getItems().addAll(FXCollections.observableArrayList(GetListOfTopicsOnServer()));
		comboBoxSelectTopic.getSelectionModel().select(0);
	}
	
	private void LoadClientTopics() throws IOException {
		topicsList.getItems().clear();
		topicsList.getItems().addAll(_client.LoadClientTopics());
	}
	
	private String[] LoadArticlesForTopic(String topic) throws IOException {
		return _client.LoadArticlesForTopic(topic);
	}

	@Override
	public void start(Stage mainStage) throws Exception {
		_client.WaitForServer(this);
		
		labelStatus = new Label("Status:");
		textFieldStatus = new TextField(InitAdminClient());
		textFieldStatus.setEditable(false);
		
		topicsList = new ListView<String>();
		topicsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		LoadClientTopics();

		newsList = new ListView<String>();
		newsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		
		topicsList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				try {
					newsList.getItems().clear();
					newsList.getItems().addAll(LoadArticlesForTopic(newValue));
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});

		buttonRemoveTopicSubscription = new Button("Send remove topic subscription");
		buttonRemoveTopicSubscription.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					if (topicsList.getSelectionModel().getSelectedItem().equals(""))
						return;
					
					textFieldStatus.setText("Remove topic: " + topicsList.getSelectionModel().getSelectedItem()
							+ " => " + _client.RemoveTopicSubscription(topicsList.getSelectionModel().getSelectedItem()));
					
					LoadClientTopics();
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});
		
		try {
			comboBoxSelectTopic = new ComboBox<String>(FXCollections.observableArrayList(GetListOfTopicsOnServer()));
			comboBoxSelectTopic.getSelectionModel().select(0);
		} catch (Exception e) {
			textFieldStatus.setText(e.getMessage());
		}
		
		buttonAddTopicSubscription = new Button("Send add topic subscription");
		buttonAddTopicSubscription.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					textFieldStatus.setText("Add topic: " + comboBoxSelectTopic.getSelectionModel().getSelectedItem()
							+ " => " + _client.AddTopicSubscription(comboBoxSelectTopic.getSelectionModel().getSelectedItem()));
					
					topicsList.getSelectionModel().select(0);
					LoadClientTopics();
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});
		
		buttonClose = new Button("Close");
		buttonClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					_client.Close();
					Platform.exit();
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});
		
		GridPane gridPane = new GridPane();

		gridPane.setVgap(4);
		gridPane.setHgap(4);

		gridPane.add(topicsList, 0, 0);
		gridPane.add(newsList, 1, 0);

		gridPane.add(buttonRemoveTopicSubscription, 0, 1);

		gridPane.add(comboBoxSelectTopic, 0, 2);
		gridPane.add(buttonAddTopicSubscription, 1, 2);
		
		gridPane.add(buttonClose, 1, 3);
		
		gridPane.add(labelStatus, 0, 4);
		gridPane.add(textFieldStatus, 1, 4);

		Scene scene = new Scene(gridPane, 500, 500);
		mainStage.setTitle("Client: " + _client.clientName);
		mainStage.setScene(scene);
		mainStage.show();
	}
}
