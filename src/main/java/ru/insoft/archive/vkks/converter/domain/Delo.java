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
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Благодатских С.
 */
@Entity
@Table(name = "Delo")
public class Delo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private Integer barcodeNumber;

	private String opis;

	private String caseDelo;

	private String caseNumber;

	private Calendar startDate;

	private Calendar endDate;

	private String storeArticle;

	private String storeLife;

	private String caseTitle;

	private Integer page;

	private Integer tomNumber;

	private Integer numberPart;

	private String source;

	private String sourcePred;

	private String especially;

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

	@Column(name = "Case_barcod", insertable = false, updatable = false)
	public Integer getBarcodeNumber() {
		return barcodeNumber;
	}

	public void setBarcodeNumber(Integer barcodeNumber) {
		this.barcodeNumber = barcodeNumber;
	}

	@Column(name = "Case_opis", insertable = false, updatable = false)
	public String getOpis() {
		return opis;
	}

	public void setOpis(String opis) {
		this.opis = opis;
	}

	@Column(name = "Case_delo", insertable = false, updatable = false)
	public String getCaseDelo() {
		return caseDelo;
	}

	public void setCaseDelo(String caseDelo) {
		this.caseDelo = caseDelo;
	}

	@NotEmpty(message = "номер дела отсутствует")
	@Column(name = "Case_number", insertable = false, updatable = false)
	public String getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	@NotNull(message = "начальная дата дела отсутствует")
	@Column(name = "Date_start", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getStartDate() {
		return startDate;
	}

	public void setStartDate(Calendar startDate) {
		this.startDate = startDate;
	}

	@NotNull(message = "конечная дата дела отсутствует")
	@Column(name = "Date_end", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getEndDate() {
		return endDate;
	}

	public void setEndDate(Calendar endDate) {
		this.endDate = endDate;
	}

	@Column(name = "Article_storage_delo", insertable = false, updatable = false)
	public String getStoreArticle() {
		return storeArticle;
	}

	public void setStoreArticle(String storeArticle) {
		this.storeArticle = storeArticle;
	}

	@Column(name = "Store_life", insertable = false, updatable = false)
	public String getStoreLife() {
		return storeLife;
	}

	public void setStoreLife(String storeLife) {
		this.storeLife = storeLife;
	}

	@NotEmpty(message = "название дела отсутствует")
	@Column(name = "Delo_title", insertable = false, updatable = false)
	public String getCaseTitle() {
		return caseTitle;
	}

	public void setCaseTitle(String caseTitle) {
		this.caseTitle = caseTitle;
	}

	@Column(name = "Page_delo", insertable = false, updatable = false)
	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	@NotNull(message = "номер тома дела отсутствует")
	@Column(name = "Number_tom", insertable = false, updatable = false)
	public Integer getTomNumber() {
		return tomNumber;
	}

	public void setTomNumber(Integer tomNumber) {
		this.tomNumber = tomNumber;
	}

	@Column(name = "Number_part", insertable = false, updatable = false)
	public Integer getNumberPart() {
		return numberPart;
	}

	public void setNumberPart(Integer numberPart) {
		this.numberPart = numberPart;
	}

	@Column(name = "Source", insertable = false, updatable = false)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Column(name = "Source_pred", insertable = false, updatable = false)
	public String getSourcePred() {
		return sourcePred;
	}

	public void setSourcePred(String sourcePred) {
		this.sourcePred = sourcePred;
	}

	@Column(name = "Especially", insertable = false, updatable = false)
	public String getEspecially() {
		return especially;
	}

	public void setEspecially(String especially) {
		this.especially = especially;
	}

	@Column(name = "Remark_delo", insertable = false, updatable = false)
	public String getCaseRemark() {
		return caseRemark;
	}

	public void setCaseRemark(String caseRemark) {
		this.caseRemark = caseRemark;
	}

	@Column(name = "Graph_delo", insertable = false, updatable = false)
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
