package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.insoft.archive.vkks.converter.control.ConverterController;

/**
 *
 * @author Благодатских С.
 */
public class ConverterUi extends Application {


	private Stage stage;

	@Override
	public void start(Stage stage) throws IOException {
		this.stage = stage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConverterUi.fxml"));
		Parent root = loader.load();

		ConverterController controller = (ConverterController) loader.getController();
		controller.setApp(this);

		stage.setTitle("Конвертер данных из ПИ \"Документооборот\" в ПИ \"Архивное дело\"");
		stage.setScene(new Scene(root));
		stage.show();
		stage.setMinHeight(stage.getHeight());
		stage.setMinWidth(stage.getWidth());

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
          public void handle(WindowEvent we) {
			  controller.savePrefs();
          }
      });
	}

	public static void main(String[] args) {
		launch(args);
	}

	public Stage getStage() {
		return stage;
	}

}
