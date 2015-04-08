package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.insoft.archive.vkks.converter.control.ConverterController;

/**
 *
 * @author Благодатских С.
 */
public class ConverterUi extends Application {

	private DriverManagerDataSource dataSource;

	private Stage stage;

	@Override
	public void start(Stage stage) throws IOException {
		this.stage = stage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConverterUi.fxml"));
		Parent root = loader.load();

		((ConverterController) loader.getController()).setApp(this);

		stage.setTitle("Конвертер данных из ПИ \"Документооборот\" в ПИ \"Архивное дело\"");
		stage.setScene(new Scene(root));
		stage.show();
		stage.setMinHeight(stage.getHeight());
		stage.setMinWidth(stage.getWidth());

		dataSource = dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("net.ucanaccess.jdbc.UcanaccessDriver");
	}

	public static void main(String[] args) {
		launch(args);
	}

	public DriverManagerDataSource getDataSource() {
		return dataSource;
	}

	public Stage getStage() {
		return stage;
	}

}
