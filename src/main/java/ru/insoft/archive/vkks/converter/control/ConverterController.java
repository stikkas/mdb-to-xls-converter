package ru.insoft.archive.vkks.converter.control;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import ru.insoft.archive.vkks.converter.Config;
import ru.insoft.archive.vkks.converter.ConverterUi;
import ru.insoft.archive.vkks.converter.Worker;

/**
 *
 * @author Благодатских С.
 */
public class ConverterController {

	private ConverterUi app;
	private FileChooser fileChooser;
	private DirectoryChooser dirChooser;
	private Preferences prefs;
	private static final Map<Pair<String, String>, Worker> runningWorkers = new HashMap<>();

	@FXML
	private TextArea logPanel;

	@FXML
	private TextField dataDirEdit;

	@FXML
	private TextField dbFileEdit;

	@FXML
	private Button execButton;

	@FXML
	private void onExec() {
		String dir = dataDirEdit.getText();
		String dbFile = dbFileEdit.getText();
		Pair p = new Pair(dir, dbFile);
		if (runningWorkers.containsKey(p)) {
			logPanel.insertText(0, "Процесс для файла данных [" + dbFile
					+ "] и папки назначения [" + dir + "] уже запущен.\n");
		} else {
			logPanel.insertText(0, "Запускается обработка файла [" + dbFile
					+ "]. Результат будет помещен в [" + dir + "].\n");
			Worker w = new Worker(dir, dbFileEdit.getText(), logPanel);
			runningWorkers.put(p, w);
			w.start();
		}
	}

	@FXML
	private void onCancel() {
		runningWorkers.forEach((k, v) -> {
			if (v.isAlive())
				v.cancel();
		});
		runningWorkers.clear();
	}

	@FXML
	private void chooseDataDir() {
		setPathEditValue(dirChooser.showDialog(app.getStage()), dataDirEdit);
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
		fileChooser.setTitle("Выбор файла с данными");
	}

	/**
	 * Создает диалог для выбора директории с xls файлами
	 */
	private void createDirChooser() {
		dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Выбор директории с исходными файлами");
	}

	/**
	 * Устанавливает доступность кнопки "Выполнить"
	 */
	private void setExecButtonEnabled() {
		execButton.setDisable(dbFileEdit.getText().isEmpty()
				|| dataDirEdit.getText().isEmpty());
	}

	public void initialize() {
		dbFileEdit.textProperty().addListener(e -> setExecButtonEnabled());
		dataDirEdit.textProperty().addListener(e -> setExecButtonEnabled());
		createFileChooser();
		createDirChooser();
		fileChooser.initialDirectoryProperty().bindBidirectional(
				dirChooser.initialDirectoryProperty());

		prefs = Preferences.userNodeForPackage(getClass());
		String initDirectory = prefs.get(Config.INIT_DIR_KEY, null);
		if (initDirectory != null && Files.isDirectory(Paths.get(initDirectory))) {
			fileChooser.setInitialDirectory(new File(initDirectory));
		}
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
