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
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;

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

	private String docPages;

	private String docRemark;

	private String prikGraph;

	private String dopGraph;

	public Document() {
	}

	@Id
	@Column(name = "ID1")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@JoinColumn(name = "PARENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	@ManyToOne(fetch = FetchType.EAGER)
	public Delo getDelo() {
		return delo;
	}

	public void setDelo(Delo delo) {
		this.delo = delo;
	}

	@NotEmpty(message = "номер документа отсутствует")
	@Column(name = "doc_number", insertable = false, updatable = false)
	public String getDocNumber() {
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	@NotNull(message = "дата документа отсутствует")
	@Column(name = "doc_date", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getDocDate() {
		return docDate;
	}

	public void setDocDate(Calendar docDate) {
		this.docDate = docDate;
	}

	@Column(name = "doc_type", insertable = false, updatable = false)
	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	@NotEmpty(message = "название документа отсутствует")
	@Column(name = "doc_title", insertable = false, updatable = false)
	public String getDocTitle() {
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	@NotEmpty(message = "начальная страница документа отсутствует")
	@Column(name = "start_page", insertable = false, updatable = false)
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	@Column(name = "end_page", insertable = false, updatable = false)
	public String getEndPage() {
		return endPage;
	}

	public void setEndPage(String endPage) {
		this.endPage = endPage;
	}

	@Column(name = "doc_pages", insertable = false, updatable = false)
	public String getDocPages() {
		return docPages;
	}

	public void setDocPages(String docPages) {
		this.docPages = docPages;
	}

	@Column(name = "doc_remark", insertable = false, updatable = false)
	public String getDocRemark() {
		return docRemark;
	}

	public void setDocRemark(String docRemark) {
		this.docRemark = docRemark;
	}

	@NotEmpty(message = "прикрепленный графический образ документа отсутствует")
	@Column(name = "prik_graph", insertable = false, updatable = false)
	public String getPrikGraph() {
		return prikGraph;
	}

	public void setPrikGraph(String prikGraph) {
		this.prikGraph = prikGraph;
	}

	@Column(name = "dop_graph", insertable = false, updatable = false)
	public String getDopGraph() {
		return dopGraph;
	}

	public void setDopGraph(String dopGraph) {
		this.dopGraph = dopGraph;
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
