package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.insoft.archive.vkks.converter.control.ConverterController;

/**
 * Тестовая версия для МИНЮСТА
 *
 * @author Благодатских С.
 */
public class ConverterUi extends Application {

	private final static String TITLE = "Конвертор МИНЮСТ (Поиск по Case_barcod)";
	private Stage stage;

	@Override
	public void start(Stage stage) throws IOException {
		this.stage = stage;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConverterUi.fxml"));
		Parent root = loader.load();

		ConverterController controller = (ConverterController) loader.getController();
		controller.setApp(this);

		stage.setTitle(TITLE);
		stage.setScene(new Scene(root));
		stage.show();
		stage.setMinHeight(stage.getHeight());
		stage.setMinWidth(stage.getWidth());

		stage.setOnCloseRequest(we -> {
			try {
				controller.savePrefs();
			} catch (Exception e) {
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
