package Zad1;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AdminGUI extends Application {
	private static Admin _admin;

	Label labelAddTopic;
	Label labelRemoveTopic;
	Label labelAddNewNewsOnTopic;
	Label labelStatus;

	TextField textFieldAddTopic;
	TextField textFieldAddNewNewsOnTopic;
	TextField textFieldStatus;

	ComboBox<String> comboBoxRemoveTopic;
	ComboBox<String> comboBoxSelectTopic;

	Button buttonAddTopic;
	Button buttonRemoveTopic;
	Button buttonAddNewNewsOnTopic;
	Button buttonClose;

	private String InitAdminClient() {
		String returnMessage;
		try {
			_admin = new Admin("localhost", 10666);
			returnMessage = _admin.LoginToServer();
		} catch (IOException e) {
			return e.getMessage();
		} catch (Exception e) {
			return e.getMessage();
		}

		return "Login: " + returnMessage;
	}

	private List<String> GetListOfTopicsOnServer() throws IOException {
		return _admin.GetListOfTopicsOnServer();
	}

	private void ReloadComboBoxs() throws IOException {
		comboBoxRemoveTopic.getItems().clear();
		comboBoxRemoveTopic.getItems().addAll(FXCollections.observableArrayList(GetListOfTopicsOnServer()));
		comboBoxRemoveTopic.getSelectionModel().select(0);

		comboBoxSelectTopic.getItems().clear();
		comboBoxSelectTopic.getItems().addAll(FXCollections.observableArrayList(GetListOfTopicsOnServer()));
		comboBoxSelectTopic.getSelectionModel().select(0);
	}

	@Override
	public void start(Stage mainStage) throws Exception {
		GridPane gridPane = new GridPane();

		labelAddTopic = new Label("Add new topic:");
		labelRemoveTopic = new Label("Remove topic:");
		labelAddNewNewsOnTopic = new Label("Add new news on topic:");
		labelStatus = new Label("Status:");

		textFieldAddTopic = new TextField("");
		textFieldAddNewNewsOnTopic = new TextField("");

		textFieldStatus = new TextField(InitAdminClient());
		textFieldStatus.setEditable(false);

		try {
			comboBoxRemoveTopic = new ComboBox<String>(FXCollections.observableArrayList(GetListOfTopicsOnServer()));
			comboBoxRemoveTopic.getSelectionModel().select(0);

			comboBoxSelectTopic = new ComboBox<String>(FXCollections.observableArrayList(GetListOfTopicsOnServer()));
			comboBoxSelectTopic.getSelectionModel().select(0);
		} catch (Exception e) {
			textFieldStatus.setText(e.getMessage());
		}

		buttonAddTopic = new Button("Send new topic");
		buttonAddTopic.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					textFieldStatus.setText("Add new topic: " + textFieldAddTopic.getText() + " => "
							+ _admin.AddNewTopic(textFieldAddTopic.getText()));
					textFieldAddTopic.clear();
					ReloadComboBoxs();
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});

		buttonRemoveTopic = new Button("Send remove topic");
		buttonRemoveTopic.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					textFieldStatus.setText("Remove topic: " + comboBoxRemoveTopic.getSelectionModel().getSelectedItem()
							+ " => " + _admin.RemoveTopic(comboBoxRemoveTopic.getSelectionModel().getSelectedItem()));
					ReloadComboBoxs();
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});

		buttonAddNewNewsOnTopic = new Button("Send new news on topic");
		buttonAddNewNewsOnTopic.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					textFieldStatus.setText("Add new news on topic: "
							+ comboBoxSelectTopic.getSelectionModel().getSelectedItem() + " => "
							+ _admin.AddNewNewsOnTopic(comboBoxSelectTopic.getSelectionModel().getSelectedItem(),
									textFieldAddNewNewsOnTopic.getText()));
					
					comboBoxSelectTopic.getSelectionModel().select(0);
					textFieldAddNewNewsOnTopic.clear();
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
					_admin.Close();
					Platform.exit();
				} catch (IOException e) {
					textFieldStatus.setText(e.getMessage());
				}
			}
		});

		gridPane.setMinSize(400, 200);

		gridPane.setPadding(new Insets(2, 2, 2, 2));

		gridPane.setVgap(4);
		gridPane.setHgap(4);

		gridPane.setAlignment(Pos.TOP_LEFT);

		gridPane.add(labelAddTopic, 0, 0);
		gridPane.add(textFieldAddTopic, 2, 0);
		gridPane.add(buttonAddTopic, 3, 0);

		gridPane.add(labelRemoveTopic, 0, 1);
		gridPane.add(comboBoxRemoveTopic, 1, 1);
		gridPane.add(buttonRemoveTopic, 3, 1);

		gridPane.add(labelAddNewNewsOnTopic, 0, 2);
		gridPane.add(comboBoxSelectTopic, 1, 2);
		gridPane.add(textFieldAddNewNewsOnTopic, 2, 2);
		gridPane.add(buttonAddNewNewsOnTopic, 3, 2);

		gridPane.add(labelStatus, 0, 4);
		gridPane.add(textFieldStatus, 2, 4);

		gridPane.add(buttonClose, 3, 5);

		Scene scene = new Scene(gridPane, 570, 160);
		mainStage.setTitle("Client");
		mainStage.setScene(scene);
		mainStage.show();
	}
}
