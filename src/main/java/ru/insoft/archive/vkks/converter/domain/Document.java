package ru.insoft.archive.vkks.converter.domain;

import com.sun.istack.internal.NotNull;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
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

	@Id
	@NotNull
	@Column(name = "ID1")
	private Integer id;

	@JoinColumn(name = "PARENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	@ManyToOne
	private Delo delo;

	@Column(name = "DOC_NUMBER", insertable = false, updatable = false)
	private String docNumber;

	@Column(name = "DATE_DOC", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar dateDoc;

	@Column(name = "DOC_TYPE", insertable = false, updatable = false)
	private String docType;

	@Column(name = "DOC_TITLE", insertable = false, updatable = false)
	private String docTitle;

	@Column(name = "PAGE_S", insertable = false, updatable = false)
	private String pageS;

	@Column(name = "PAGE PO", insertable = false, updatable = false)
	private Integer pagePo;

	@Column(name = "PAGE", insertable = false, updatable = false)
	private Integer page;

	@Column(name = "REMARK_DOCUMENT", insertable = false, updatable = false)
	private String remarkDocument;

	@Column(name = "GRAPH", insertable = false, updatable = false)
	private String graph;

	public Document() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Delo getDelo() {
		return delo;
	}

	public void setDelo(Delo delo) {
		this.delo = delo;
	}

	public String getDocNumber() {
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
	}

	public Calendar getDateDoc() {
		return dateDoc;
	}

	public void setDateDoc(Calendar dateDoc) {
		this.dateDoc = dateDoc;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getDocTitle() {
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	public String getPageS() {
		return pageS;
	}

	public void setPageS(String pageS) {
		this.pageS = pageS;
	}

	public Integer getPagePo() {
		return pagePo;
	}

	public void setPagePo(Integer pagePo) {
		this.pagePo = pagePo;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getRemarkDocument() {
		return remarkDocument;
	}

	public void setRemarkDocument(String remarkDocument) {
		this.remarkDocument = remarkDocument;
	}

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
