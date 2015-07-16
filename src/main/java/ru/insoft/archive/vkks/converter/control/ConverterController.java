package ru.insoft.archive.vkks.converter.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import ru.insoft.archive.vkks.converter.Config;
import ru.insoft.archive.vkks.converter.ConverterUi;
import ru.insoft.archive.vkks.converter.Worker;
import ru.insoft.archive.vkks.converter.error.WrongModeException;

/**
 *
 * @author Благодатских С.
 */
public class ConverterController {

	private ConverterUi app;
	private FileChooser fileChooser;
	private FileChooser saveLogChooser;
	private Preferences prefs;

	@FXML
	private TextArea logPanel;

	@FXML
	private TextField dbFileEdit;

	@FXML
	private Button execButton;

	@FXML
	private Button saveLogButton;

	/**
	 * Запуск конвертации
	 */
	@FXML
	private void onExec() {
		String dbFile = dbFileEdit.getText();
		logPanel.insertText(0, "Запускается обработка файла [" + dbFile
				+ "].\n");
		Worker w;
		try {
			w = new Worker(dbFileEdit.getText(), logPanel);
			w.start();
		} catch (WrongModeException ex) {
			Platform.runLater(() -> logPanel.insertText(0, ex.getMessage() + "\n"));
		}
	}

	/**
	 * Сохраняем информацию из TextArea в файл
	 */
	@FXML
	private void onSaveLog() {
		if (saveLogChooser == null) {
			saveLogChooser = new FileChooser();
			saveLogChooser.setTitle("Выбор файла для сохранения отчета");
		}
		File file = saveLogChooser.showSaveDialog(app.getStage());
		if (file != null) {
			try {
				Files.write(file.toPath(), logPanel.getText().getBytes());
			} catch (IOException ex) {
				logPanel.insertText(0, "Произошла ошибка записи в файл: " + ex.getMessage()
						+ "\nПопробуйте еще раз, либо выделите весь текст мышкой, скопируйте и вставьте в файл"
				);
			}
		}
	}

	@FXML
	private void chooseDBFile() {
		setPathEditValue(fileChooser.showOpenDialog(app.getStage()), dbFileEdit);
	}

	/**
	 * Устанавливает значение для текстового поля ввода
	 */
	private void setPathEditValue(File file, TextField field) {
		if (file != null) {
			field.setText(file.getAbsolutePath());
			fileChooser.setInitialDirectory(
					Paths.get(file.getAbsolutePath()).getParent().toFile());
		} else {
			field.clear();
		}
	}

	/**
	 * Создает диалог для выбора базы данных Access
	 */
	private void createFileChooser() {
		fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().
				add(new FileChooser.ExtensionFilter("MS Access DB", "*.mdb"));
		fileChooser.setTitle("Выбор файла mdb с данными");
	}

	public void initialize() {
		execButton.disableProperty().bind(dbFileEdit.textProperty().isEmpty());
		createFileChooser();

		// Выбираем сохраненный путь к последней выбранной папке
		prefs = Preferences.userNodeForPackage(getClass());
		String initDirectory = prefs.get(Config.INIT_DIR_KEY, null);
		if (initDirectory != null && Files.isDirectory(Paths.get(initDirectory))) {
			fileChooser.setInitialDirectory(new File(initDirectory));
		}

		execButton.setTooltip(new Tooltip("Начать конвертацию данных"));
		saveLogButton.setTooltip(new Tooltip("Создать файл отчета работы конвертора"));
		saveLogButton.disableProperty().bind(logPanel.textProperty().isEmpty());
	}

	public void setApp(ConverterUi app) {
		this.app = app;
	}

	/**
	 * Сохраняет путь к директории с данными в настройках пользователя
	 */
	public void savePrefs() {
		prefs.put(Config.INIT_DIR_KEY, fileChooser.getInitialDirectory().getAbsolutePath());
	}
}
