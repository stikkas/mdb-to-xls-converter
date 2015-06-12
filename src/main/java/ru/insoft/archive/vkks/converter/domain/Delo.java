package ru.insoft.archive.vkks.converter.domain;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Благодатских С.
 */
@Entity
@Table(name = "Case")
public class Delo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer barcodeNumber;

	private Integer raOpisNumber;

	private Integer raCaseNumber;

	private String caseNumber;

	private Calendar startDate;

	private Calendar endDate;

	private String storeArticle;

	private String storeLife;

	private String caseTitle;

	private Short casePages;

	private Short tomNumber;

	private Integer numberPart;

	private String source;

	private String department;

	private String specificity;

	private String caseRemark;

	private String caseGraph;

	private List<Document> documents;

	public Delo() {
	}

	@Id
	@Column(name = "ID")
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "barcode_number", insertable = false, updatable = false)
	public Integer getBarcodeNumber() {
		return barcodeNumber;
	}

	public void setBarcodeNumber(Integer barcodeNumber) {
		this.barcodeNumber = barcodeNumber;
	}

	@Column(name = "ra_opis_number", insertable = false, updatable = false)
	public Integer getRaOpisNumber() {
		return raOpisNumber;
	}

	public void setRaOpisNumber(Integer raOpisNumber) {
		this.raOpisNumber = raOpisNumber;
	}

	@Column(name = "ra_case_number", insertable = false, updatable = false)
	public Integer getRaCaseNumber() {
		return raCaseNumber;
	}

	public void setRaCaseNumber(Integer raCaseNumber) {
		this.raCaseNumber = raCaseNumber;
	}

	@NotNull(message = "номер дела отсутствует")
	@Column(name = "case_number", insertable = false, updatable = false)
	public String getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	@NotNull(message = "начальная дата дела отсутствует")
	@Column(name = "start_date", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	@NotNull(message = "конечная дата дела отсутствует")
	@Column(name = "end_date", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	@Column(name = "store_article", insertable = false, updatable = false)
	public String getStoreArticle() {
		return storeArticle;
	}

	public void setStoreArticle(String storeArticle) {
		this.storeArticle = storeArticle;
	}

	@Column(name = "store_life", insertable = false, updatable = false)
	public String getStoreLife() {
		return storeLife;
	}

	public void setStoreLife(String storeLife) {
		this.storeLife = storeLife;
	}

	@NotNull(message = "название дела отсутствует")
	@Column(name = "case_title", insertable = false, updatable = false)
	public String getCaseTitle() {
		return caseTitle;
	}

	public void setCaseTitle(String caseTitle) {
		this.caseTitle = caseTitle;
	}

	@Column(name = "case_pages", insertable = false, updatable = false)
	public Short getCasePages() {
		return casePages;
	}

	public void setCasePages(Short casePages) {
		this.casePages = casePages;
	}

	@NotNull(message = "номер тома дела отсутствует")
	@Column(name = "tom_number", insertable = false, updatable = false)
	public Short getTomNumber() {
		return tomNumber;
	}

	public void setTomNumber(Short tomNumber) {
		this.tomNumber = tomNumber;
	}

	@Column(name = "part_number", insertable = false, updatable = false)
	public Integer getNumberPart() {
		return numberPart;
	}

	public void setNumberPart(Integer numberPart) {
		this.numberPart = numberPart;
	}

	@Column(name = "source", insertable = false, updatable = false)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Column(name = "department", insertable = false, updatable = false)
	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@Column(name = "specificity", insertable = false, updatable = false)
	public String getSpecificity() {
		return specificity;
	}

	public void setSpecificity(String specificity) {
		this.specificity = specificity;
	}

	@Column(name = "case_remark", insertable = false, updatable = false)
	public String getCaseRemark() {
		return caseRemark;
	}

	public void setCaseRemark(String caseRemark) {
		this.caseRemark = caseRemark;
	}

	@Column(name = "case_graph", insertable = false, updatable = false)
	public String getCaseGraph() {
		return caseGraph;
	}

	public void setCaseGraph(String caseGraph) {
		this.caseGraph = caseGraph;
	}

	@OneToMany(mappedBy = "delo", fetch = FetchType.EAGER)
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
