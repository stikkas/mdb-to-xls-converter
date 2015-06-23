package ru.insoft.archive.vkks.converter.domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import ru.insoft.archive.vkks.converter.Config;

/**
 *
 * @author Благодатских С.
 */
@Entity
@Table(name = "Document")
public class Document implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Delo delo;

	private String docNumber;

	private Calendar docDate;

	private String docType;

	private String docTitle;

	private String startPage;

	private String endPage;

	private String page;

	private String mainId;

	private Document main;

	private String docRemark;

	private String prikGraph;
	private boolean changed = false;

	public Document() {
	}

	public Document(String docNumber, Calendar docDate, String docTitle,
			String prickGraph, String docType, Delo delo) {
		this.docNumber = docNumber;
		this.docDate = docDate;
		this.docTitle = docTitle;
		this.prikGraph = prickGraph;
		this.docType = docType;
		this.delo = delo;
	}

	@Id
	@Column(name = "ID1")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@JoinColumn(name = "Parent_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	@ManyToOne(fetch = FetchType.EAGER)
	public Delo getDelo() {
		return delo;
	}

	public void setDelo(Delo delo) {
		this.delo = delo;
	}

	@Column(name = "Doc_number", insertable = false, updatable = false)
	public String getDocNumber() {
		if (docNumber == null || docNumber.trim().isEmpty() || docNumber.equals("без №")) {
			if (main != null) {
				return "к " + main.getDocNumber();
			} else {
				return "без №";
			}
		}
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	@Column(name = "Date_doc", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getDocDate() {
		if (docDate == null || changed) {
			if (main != null) {
				return main.getDocDate();
			} else {
				changed = true;
				return delo.getEndDate();
			}
		}
		return docDate;
	}

	public void setDocDate(Calendar docDate) {
		this.docDate = docDate;
	}

	@Column(name = "Doc_type", insertable = false, updatable = false)
	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	@Column(name = "Doc_title", insertable = false, updatable = false)
	public String getDocTitle() {
		if (docTitle == null || docTitle.trim().isEmpty()
				|| docTitle.equals(docType + " в деле " + delo.getCaseTitle())) {
			if (main != null) {
				return docType + " к " + main.getDocType()
						+ " №" + main.getDocNumber() + " от "
						+ Config.sdf.format(main.getDocDate().getTime());
			} else {
				return docType + " в деле " + delo.getCaseTitle();
			}
		}
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	/**
	 * Номер страницы может содержать букву на конце, нам нужны только цифры
	 *
	 * @return
	 */
	@Column(name = "Page_s", insertable = false, updatable = false)
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	public Integer getNumberPage() {
		if (!(startPage == null || startPage.trim().isEmpty())) {
			int i = 0;
			int size = startPage.length();
			while (i < size && Character.isDigit(startPage.charAt(i))) {
				++i;
			}
			return Integer.parseInt(startPage.substring(0, i));
		}
		return null;
	}

	@Column(name = "Page_po", insertable = false, updatable = false)
	public String getEndPage() {
		return endPage;
	}

	public void setEndPage(String endPage) {
		this.endPage = endPage;
	}

	@Column(name = "Page", insertable = false, updatable = false)
	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	@Transient
	public Document getMain() {
		return main;
	}

	public void setMain(Document main) {
		this.main = main;
	}

	@Column(name = "ID_main", insertable = false, updatable = false)
	public String getMainId() {
		return mainId;
	}

	public void setMainId(String mainId) {
		this.mainId = mainId;
	}

	@Column(name = "Remark_document", insertable = false, updatable = false)
	public String getDocRemark() {
		return docRemark;
	}

	public void setDocRemark(String docRemark) {
		this.docRemark = docRemark;
	}

	@Column(name = "Graph", insertable = false, updatable = false)
	public String getPrikGraph() {
		return prikGraph;
	}

	public void setPrikGraph(String prikGraph) {
		this.prikGraph = prikGraph;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + Objects.hashCode(this.id);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Document other = (Document) obj;
		return Objects.equals(id, other.id);
	}

}
