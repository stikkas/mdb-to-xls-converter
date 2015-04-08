package ru.insoft.archive.vkks.converter.control;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import ru.insoft.archive.vkks.converter.Config;
import ru.insoft.archive.vkks.converter.ConverterUi;

/**
 *
 * @author Благодатских С.
 */
public class ConverterController implements Config {

	private ConverterUi app;
	private static final String dbPrefix = "jdbc:ucanaccess://";

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
		app.getDataSource().setUrl(dbPrefix + dbFileEdit.getText());
		try {
			Connection con = app.getDataSource().getConnection();
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery("select * from " + CASE_TABLE_NAME);
			while (rs.next()) {
				System.out.println(rs.getObject(1));
				/*
				 int columnsCount = rs.getMetaData().getColumnCount();
				 for (int i = 0; i < columnsCount; ++i) {
				 System.out.print(rs.getObject(i) + ", ");
				 }
				 System.out.println();
				 */
			}
		} catch (SQLException ex) {
			Logger.getLogger(ConverterController.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@FXML
	private void chooseDataDir() {

	}

	@FXML
	private void chooseDBFile() {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().
				add(new FileChooser.ExtensionFilter("MS Access DB", "*.mdb"));
		chooser.setTitle("Выбор файла с данными");
		File file = chooser.showOpenDialog(app.getStage());
		if (file != null) {
			dbFileEdit.setText(file.getAbsolutePath());
		} else {
			dbFileEdit.clear();
		}
	}

	private void setExecButtonEnabled() {
		execButton.setDisable(dbFileEdit.getText().isEmpty()); 
	}

	public void initialize() {
		dbFileEdit.textProperty().addListener(e -> setExecButtonEnabled());
	}

	public void setApp(ConverterUi app) {
		this.app = app;
	}

}
