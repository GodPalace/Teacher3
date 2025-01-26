package com.godpalace.test;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class TestMain extends Application {
    @Override
    public void start(Stage stage) {
        TableView<String> tableView = new TableView<>();

        TableColumn<String, String> column = new TableColumn<>("ColumnA");
        TableColumn<String, String> column2 = new TableColumn<>("ColumnB");

        //column.setCellValueFactory(new PropertyValueFactory<>("columna"));
        //column2.setCellValueFactory(new PropertyValueFactory<>("columnb"));

        tableView.getColumns().addAll(column, column2);

        ObservableList<String> data = FXCollections.observableArrayList();
        for (int i = 0; i < 100; i++) {
            data.add("Row " + i);
        }
        tableView.setItems(data);

        stage.setScene(new Scene(tableView));
        stage.show();
    }
}
