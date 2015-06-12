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

/**
 *
 * @author Благодатских С.
 */
@Entity
@Table(name = "Case")
public class Delo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	private Integer id;

	@Column(name = "barcode_number", insertable = false, updatable = false)
	private Integer barcodeNumber;

	@Column(name = "ra_opis_number", insertable = false, updatable = false)
	private Integer raOpisNumber;

	@Column(name = "ra_case_number", insertable = false, updatable = false)
	private Integer raCaseNumber;

	@Column(name = "case_number", insertable = false, updatable = false)
	private String caseNumber;

	@Column(name = "start_date", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startDate;

	@Column(name = "end_date", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar endDate;

	@Column(name = "store_article", insertable = false, updatable = false)
	private String storeArticle;

	@Column(name = "store_life", insertable = false, updatable = false)
	private String storeLife;

	@Column(name = "case_title", insertable = false, updatable = false)
	private String caseTitle;

	@Column(name = "case_pages", insertable = false, updatable = false)
	private Short casePages;

	@Column(name = "tom_number", insertable = false, updatable = false)
	private Short tomNumber;

	@Column(name = "part_number", insertable = false, updatable = false)
	private Integer numberPart;

	@Column(name = "source", insertable = false, updatable = false)
	private String source;

	@Column(name = "department", insertable = false, updatable = false)
	private String department;

	@Column(name = "specificity", insertable = false, updatable = false)
	private String specificity;

	@Column(name = "case_remark", insertable = false, updatable = false)
	private String caseRemark;

	@Column(name = "case_graph", insertable = false, updatable = false)
	private String caseGraph;

	@OneToMany(mappedBy = "delo", fetch = FetchType.EAGER)
	private List<Document> documents;

	public Delo() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBarcodeNumber() {
		return barcodeNumber;
	}

	public void setBarcodeNumber(Integer barcodeNumber) {
		this.barcodeNumber = barcodeNumber;
	}

	public Integer getRaOpisNumber() {
		return raOpisNumber;
	}

	public void setRaOpisNumber(Integer raOpisNumber) {
		this.raOpisNumber = raOpisNumber;
	}

	public Integer getRaCaseNumber() {
		return raCaseNumber;
	}

	public void setRaCaseNumber(Integer raCaseNumber) {
		this.raCaseNumber = raCaseNumber;
	}

	public String getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	public String getStoreArticle() {
		return storeArticle;
	}

	public void setStoreArticle(String storeArticle) {
		this.storeArticle = storeArticle;
	}

	public String getStoreLife() {
		return storeLife;
	}

	public void setStoreLife(String storeLife) {
		this.storeLife = storeLife;
	}

	public String getCaseTitle() {
		return caseTitle;
	}

	public void setCaseTitle(String caseTitle) {
		this.caseTitle = caseTitle;
	}

	public Short getCasePages() {
		return casePages;
	}

	public void setCasePages(Short casePages) {
		this.casePages = casePages;
	}

	public Short getTomNumber() {
		return tomNumber;
	}

	public void setTomNumber(Short tomNumber) {
		this.tomNumber = tomNumber;
	}

	public Integer getNumberPart() {
		return numberPart;
	}

	public void setNumberPart(Integer numberPart) {
		this.numberPart = numberPart;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getSpecificity() {
		return specificity;
	}

	public void setSpecificity(String specificity) {
		this.specificity = specificity;
	}

	public String getCaseRemark() {
		return caseRemark;
	}

	public void setCaseRemark(String caseRemark) {
		this.caseRemark = caseRemark;
	}

	public String getCaseGraph() {
		return caseGraph;
	}

	public void setCaseGraph(String caseGraph) {
		this.caseGraph = caseGraph;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
	
}
