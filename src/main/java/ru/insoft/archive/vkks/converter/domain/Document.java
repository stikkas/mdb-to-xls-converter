package ru.insoft.archive.vkks.converter.domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
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

/**
 *
 * @author Благодатских С.
 */
@Entity
@Table(name = "DOCUMENT")
public class Document implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Delo delo;

	private String docNumber;

	private Calendar dateDoc;

	private String docType;

	private String docTitle;

	private String pageS;

	private Integer pagePo;

	private Integer page;

	private String remarkDocument;

	private String graph;

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

	@Column(name = "DOC_NUMBER", insertable = false, updatable = false)
	public String getDocNumber() {
		if (docNumber == null || docNumber.trim().isEmpty()) {
			return "1000";
		}
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	@Column(name = "DATE_DOC", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getDateDoc() {
		if (dateDoc == null) {
			Calendar cal = GregorianCalendar.getInstance();
			cal.set(1111, 10, 11);
			return cal;
		}

		return dateDoc;
	}

	public void setDateDoc(Calendar dateDoc) {
		this.dateDoc = dateDoc;
	}

	@Column(name = "DOC_TYPE", insertable = false, updatable = false)
	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	@Column(name = "DOC_TITLE", insertable = false, updatable = false)
	public String getDocTitle() {
		if (docTitle == null || docTitle.trim().isEmpty()) {
			return docType;
		}
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	@Column(name = "PAGE_S", insertable = false, updatable = false)
	public String getPageS() {
		return pageS;
	}

	public void setPageS(String pageS) {
		this.pageS = pageS;
	}

	@Column(name = "PAGE PO", insertable = false, updatable = false)
	public Integer getPagePo() {
		return pagePo;
	}

	public void setPagePo(Integer pagePo) {
		this.pagePo = pagePo;
	}

	@Column(name = "PAGE", insertable = false, updatable = false)
	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	@Column(name = "REMARK_DOCUMENT", insertable = false, updatable = false)
	public String getRemarkDocument() {
		return remarkDocument;
	}

	public void setRemarkDocument(String remarkDocument) {
		this.remarkDocument = remarkDocument;
	}

	@Column(name = "GRAPH", insertable = false, updatable = false)
	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
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
