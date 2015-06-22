package ru.insoft.archive.vkks.converter.control;

import javafx.scene.control.TextField;

/**
 * Поле ввода чисел
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class NumberField extends TextField {

	private final Integer minValue = 1;
	private String currentText = minValue.toString();

	public NumberField() {
		setText(currentText);

		textProperty().addListener(e -> {
			try {
				Integer.parseInt(getText());
				currentText = getText();
			} catch (NumberFormatException ex) {
				setText(currentText);
			}
		});
	}

	public Integer getValue() {
		return Integer.valueOf(getText());
	}
}
