package ru.insoft.archive.vkks.converter.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import javax.swing.JOptionPane;
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
	private Preferences prefs;
	/**
	 * Количество запущенных процессов конвертации
	 */
	private final ObjectProperty<AtomicInteger> countWorkers = new SimpleObjectProperty<>(new AtomicInteger(0));

	/**
	 * Запущенные процессы конвертации, нужны для операции прерывания и для
	 * отслеживания попытки запустить один и тотже процесс дважды.
	 */
	private static final Map<String, Worker> runningWorkers = new HashMap<>();


	@FXML
	private TextArea logPanel;

	@FXML
	private TextField dbFileEdit;

	@FXML
	private Button execButton;

	@FXML
	private Button saveLogButton;

	@FXML
	private Button cancelButton;

	@FXML
	private NumberField caseIdEdit;

	@FXML
	private Label caseIdLabel;

	@FXML
	private NumberField year1Edit;

	@FXML
	private Label year1Label;

	@FXML
	private NumberField year2Edit;

	@FXML
	private Label year2Label;

	@FXML
	private ComboBox<String> modeBox;

	/**
	 * Список режимов работы
	 */
	private static final String[] modes = {
		"формировать XLS файл для одного тома дела",
		"формировать XLS файлы с томами по делу для указанного года",
		"формировать XLS файлы с делами по подразделению для указанного года"};

	private Map<String, Pair<Label, NumberField>> modesElements = new HashMap<>();

	@FXML
	private void onExec() {
		String dbFile = dbFileEdit.getText();
		if (runningWorkers.containsKey(dbFile)) {
			logPanel.insertText(0, "Процесс для файла данных [" + dbFile + "] уже запущен.\n");
		} else {
			logPanel.insertText(0, "Запускается обработка файла [" + dbFile
					+ "].\n");
			Worker w;

			/*
			if (caseId.isDisabled()) {
				w = new Worker(dir, dbFileEdit.getText(), logPanel,
						formatDst.getSelectedToggle() == everyRadio
								? WorkMode.CASE_XLS : WorkMode.GROUP_CASE_XLS);
			} else {
				w = new Worker(dir, dbFileEdit.getText(), logPanel, WorkMode.BY_ID, caseId.getValue());
			}

			runningWorkers.put(dbFile, w);
			countWorkers.set(new AtomicInteger(countWorkers.get().incrementAndGet()));
			w.doneProperty().addListener(e -> {
				countWorkers.set(new AtomicInteger(countWorkers.get().decrementAndGet()));
				runningWorkers.remove(dbFile);
			});
			w.start();
			*/
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
	 * Устанавливает доступность кнопки "Выполнить"
	 */
	private void setExecButtonEnabled() {
		execButton.setDisable(dbFileEdit.getText().isEmpty());
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

		execButton.setTooltip(new Tooltip("Начать конвертацию выбранных данных"));
		cancelButton.setTooltip(new Tooltip("Прервать конвертацию всех данных"));
		saveLogButton.setTooltip(new Tooltip("Создать файл отчета работы конвертора"));

		saveLogButton.disableProperty().bind(logPanel.textProperty().isEmpty());
		countWorkers.addListener(e -> {
			cancelButton.setDisable(countWorkers.get().get() <= 0);
		});

		modesElements.put(modes[0], new Pair<>(caseIdLabel, caseIdEdit));
		modesElements.put(modes[1], new Pair<>(year1Label, year1Edit));
		modesElements.put(modes[2], new Pair<>(year2Label, year2Edit));
		showModeOptions();

		modeBox.getItems().addAll(modes);
		modeBox.setValue(modes[0]);
	}

	private void showModeOptions() {
		caseIdLabel.visibleProperty().bind(modeBox.valueProperty().isEqualTo(modes[0]));
		modesElements.keySet().stream().map((key) -> {
			Pair<Label, NumberField> p = modesElements.get(key);
			p.getKey().visibleProperty().bind(modeBox.valueProperty().isEqualTo(key));
			return p;
		}).forEach((p) -> {
			p.getValue().visibleProperty().bind(p.getKey().visibleProperty());
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
