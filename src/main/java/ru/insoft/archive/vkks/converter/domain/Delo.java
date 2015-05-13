package ru.insoft.archive.vkks.converter.domain;

import com.sun.istack.internal.NotNull;
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
@Table(name = "DELO")
public class Delo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
	@Column(name = "ID")
	private Integer id;

	@Column(name = "CASE_BARCOD", insertable = false, updatable = false)
	private Integer caseBarcod;

	@Column(name = "CASE_OPIS", insertable = false, updatable = false)
	private Integer caseOpis;

	@Column(name = "CASE_DELO", insertable = false, updatable = false)
	private Integer caseDelo;

	@Column(name = "CASE_NUMBER", insertable = false, updatable = false)
	private String caseNumber;

	@Column(name = "DATE_START", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar dateStart;

	@Column(name = "DATE_END", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar dateEnd;

	@Column(name = "ARTICLE_STORAGE_DELO", insertable = false, updatable = false)
	private String articleStorageDelo;

	@Column(name = "STORE_LIFE", insertable = false, updatable = false)
	private String storeLife;

	@Column(name = "DELO_TITLE", insertable = false, updatable = false)
	private String deloTitle;

	@Column(name = "PAGE_DELO", insertable = false, updatable = false)
	private String pageDelo;

	@Column(name = "NUMBER_TOM", insertable = false, updatable = false)
	private Integer numberTom;

	@Column(name = "NUMBER_PART", insertable = false, updatable = false)
	private Integer numberPart;

	@Column(name = "SOURCE", insertable = false, updatable = false)
	private String source;

	@Column(name = "SOURCE_PRED", insertable = false, updatable = false)
	private String sourcePred;

	@Column(name = "ESPECIALLY", insertable = false, updatable = false)
	private String especially;

	@Column(name = "REMARK_DELO", insertable = false, updatable = false)
	private String remarkDelo;

	@Column(name = "GRAPH_DELO", insertable = false, updatable = false)
	private String graphDelo;

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

	public Integer getCaseBarcod() {
		return caseBarcod;
	}

	public void setCaseBarcod(Integer caseBarcod) {
		this.caseBarcod = caseBarcod;
	}

	public Integer getCaseOpis() {
		return caseOpis;
	}

	public void setCaseOpis(Integer caseOpis) {
		this.caseOpis = caseOpis;
	}

	public Integer getCaseDelo() {
		return caseDelo;
	}

	public void setCaseDelo(Integer caseDelo) {
		this.caseDelo = caseDelo;
	}

	public String getCaseNumber() {
		return caseNumber;
	}

	public void setCaseNumber(String caseNumber) {
		this.caseNumber = caseNumber;
	}

	public Calendar getDateStart() {
		return dateStart;
	}

	public void setDateStart(Calendar dateStart) {
		this.dateStart = dateStart;
	}

	public Calendar getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(Calendar dateEnd) {
		this.dateEnd = dateEnd;
	}

	public String getArticleStorageDelo() {
		return articleStorageDelo;
	}

	public void setArticleStorageDelo(String articleStorageDelo) {
		this.articleStorageDelo = articleStorageDelo;
	}

	public String getStoreLife() {
		return storeLife;
	}

	public void setStoreLife(String storeLife) {
		this.storeLife = storeLife;
	}

	public String getDeloTitle() {
		return deloTitle;
	}

	public void setDeloTitle(String deloTitle) {
		this.deloTitle = deloTitle;
	}

	public String getPageDelo() {
		return pageDelo;
	}

	public void setPageDelo(String pageDelo) {
		this.pageDelo = pageDelo;
	}

	public Integer getNumberTom() {
		return numberTom;
	}

	public void setNumberTom(Integer numberTom) {
		this.numberTom = numberTom;
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

	public String getSourcePred() {
		return sourcePred;
	}

	public void setSourcePred(String sourcePred) {
		this.sourcePred = sourcePred;
	}

	public String getEspecially() {
		return especially;
	}

	public void setEspecially(String especially) {
		this.especially = especially;
	}

	public String getRemarkDelo() {
		return remarkDelo;
	}

	public void setRemarkDelo(String remarkDelo) {
		this.remarkDelo = remarkDelo;
	}

	public String getGraphDelo() {
		return graphDelo;
	}

	public void setGraphDelo(String graphDelo) {
		this.graphDelo = graphDelo;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
