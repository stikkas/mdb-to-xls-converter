package ru.insoft.archive.vkks.converter.error;

/**
 *
 * @author Благодатских С.
 */
public class CaseNotFound extends Exception {

	private final String caseNumber;

	public CaseNotFound(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	public String getCaseNumber() {
		return caseNumber;
	}

}
