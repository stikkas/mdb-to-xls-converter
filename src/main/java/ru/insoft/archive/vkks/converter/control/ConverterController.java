package ru.insoft.archive.vkks.converter.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import ru.insoft.archive.vkks.converter.Config;
import ru.insoft.archive.vkks.converter.ConverterUi;
import ru.insoft.archive.vkks.converter.WorkMode;
import ru.insoft.archive.vkks.converter.Worker;

/**
 *
 * @author Благодатских С.
 */
public class ConverterController {

	private ConverterUi app;
	private FileChooser fileChooser;
	private FileChooser saveLogChooser;
	private DirectoryChooser dirChooser;
	private Preferences prefs;
	/**
	 * Количество запущенных процессов конвертации
	 */
	private final ObjectProperty<AtomicInteger> countWorkers = new SimpleObjectProperty<>(new AtomicInteger(0));

	/**
	 * Запущенные процессы конвертации, нужны для операции прерывания и для
	 * отслеживания попытки запустить один и тотже процесс дважды.
	 */
	private static final Map<Pair<String, String>, Worker> runningWorkers = new HashMap<>();

	@FXML
	private Spinner<Integer> caseId;

	@FXML
	private CheckBox allCasesCheck;

	@FXML
	private TextArea logPanel;

	@FXML
	private TextField dataDirEdit;

	@FXML
	private TextField dbFileEdit;

	@FXML
	private Button execButton;

	@FXML
	private Button saveLogButton;

	@FXML
	private Button cancelButton;

	/**
	 * Формировать для каждого дела отдельный файл
	 */
	@FXML
	private RadioButton everyRadio;

	/**
	 * Формировать для одного года одного дела один файл
	 */
	@FXML
	private RadioButton groupRadio;

	private final ToggleGroup formatDst = new ToggleGroup();

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
			Worker w;

			if (caseId.isDisabled()) {
				w = new Worker(dir, dbFileEdit.getText(), logPanel,
						formatDst.getSelectedToggle() == everyRadio
								? WorkMode.CASE_XLS : WorkMode.GROUP_CASE_XLS);
			} else {
				w = new Worker(dir, dbFileEdit.getText(), logPanel, WorkMode.BY_ID, caseId.getValue());
			}

			runningWorkers.put(p, w);
			countWorkers.set(new AtomicInteger(countWorkers.get().incrementAndGet()));
			w.doneProperty().addListener(e -> {
				countWorkers.set(new AtomicInteger(countWorkers.get().decrementAndGet()));
				runningWorkers.remove(p);
			});
			w.start();
		}
	}

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

	/**
	 * При нажатии на кнопку "Прервать" выполняется завершение всех рабочих
	 * процессов При этом дается возможность обработать до конца начатые дела.
	 */
	@FXML
	private void onCancel() {
		runningWorkers.forEach((k, v) -> {
			if (v.isAlive()) {
				v.cancel();
			}
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
		fileChooser.setTitle("Выбор файла mdb с данными");
	}

	/**
	 * Создает диалог для выбора директории с xls файлами
	 */
	private void createDirChooser() {
		dirChooser = new DirectoryChooser();
		dirChooser.setTitle("Выбор директории для сохранения сконвертированных данных");
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

		// Выбираем сохраненный путь к последней выбранной папке
		prefs = Preferences.userNodeForPackage(getClass());
		String initDirectory = prefs.get(Config.INIT_DIR_KEY, null);
		if (initDirectory != null && Files.isDirectory(Paths.get(initDirectory))) {
			fileChooser.setInitialDirectory(new File(initDirectory));
		}

		formatDst.getToggles().addAll(everyRadio, groupRadio);

		everyRadio.disableProperty().bind(caseId.disableProperty().not());
		groupRadio.disableProperty().bind(caseId.disableProperty().not());
		caseId.disableProperty().bind(allCasesCheck.selectedProperty());

		// Настройка выбора только числовых значения для ID дела
		caseId.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
		caseId.getEditor().textProperty().addListener(e -> {
			Integer value = 1;
			try {
				value = Integer.parseInt(caseId.getEditor().getText());
			} catch (NumberFormatException ex) {
				caseId.getEditor().setText(value.toString());
			}
			caseId.getValueFactory().setValue(value);
		});

		execButton.setTooltip(new Tooltip("Начать конвертацию выбранных данных"));
		cancelButton.setTooltip(new Tooltip("Прервать конвертацию всех данных"));
		saveLogButton.setTooltip(new Tooltip("Создать файл отчета работы конвертора"));

		saveLogButton.disableProperty().bind(logPanel.textProperty().isEmpty());
		countWorkers.addListener(e -> {
			cancelButton.setDisable(countWorkers.get().get() <= 0);
		});
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
