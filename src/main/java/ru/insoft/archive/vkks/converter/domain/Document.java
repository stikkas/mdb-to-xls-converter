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

	private String reportFormNumber;

	private Calendar date;

	private String title;

	private String graph;

	private String lawCourtName;

	private String subjectNameRF;

	private String reportPeriod;

	private String reportType;

	private String reportYear;
	private String dataForm;
	private String docNumber;

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

	@JoinColumn(name = "Parent_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	@ManyToOne(fetch = FetchType.EAGER)
	public Delo getDelo() {
		return delo;
	}

	public void setDelo(Delo delo) {
		this.delo = delo;
	}

	@Column(name = "Report_form_number", insertable = false, updatable = false)
	public String getReportFormNumber() {
		if (reportFormNumber == null || reportFormNumber.trim().isEmpty()) {
			return "б/н";
		}
		return reportFormNumber;
	}

	public void setReportFormNumber(String reportFormNumber) {
		this.reportFormNumber = reportFormNumber;
	}

	@Column(name = "Date_doc", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	public Calendar getDate() {
		return this.date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	@Column(name = "Doc_title", insertable = false, updatable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "Graph", insertable = false, updatable = false)
	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	@Column(name = "Report_type", insertable = false, updatable = false)
	public String getReportType() {
		if (reportType == null) {
			return "";
		}
		return " " + reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	@Column(name = "Law_court_name", insertable = false, updatable = false)
	public String getLawCourtName() {
		if (lawCourtName == null) {
			return "";
		}
		return lawCourtName;
	}

	public void setLawCourtName(String lawCourtName) {
		this.lawCourtName = lawCourtName;
	}

	@Column(name = "Subject_name_RF", insertable = false, updatable = false)
	public String getSubjectNameRF() {
		if (subjectNameRF == null) {
			return "";
		}
		return " " + subjectNameRF;
	}

	public void setSubjectNameRF(String subjectNameRF) {
		this.subjectNameRF = subjectNameRF;
	}

	@Column(name = "Data_form", insertable = false, updatable = false)
	public String getDataForm() {
		if (dataForm == null) {
			return "";
		}
		return dataForm;
	}

	public void setDataForm(String dataForm) {
		this.dataForm = dataForm;
	}

	@Column(name = "Report_period", insertable = false, updatable = false)
	public String getReportPeriod() {
		if (reportPeriod == null) {
			return "";
		}
		return " " + reportPeriod;
	}

	public void setReportPeriod(String reportPeriod) {
		this.reportPeriod = reportPeriod;
	}

	@Column(name = "Report_year", insertable = false, updatable = false)
	public String getReportYear() {
		return reportYear;
	}

	public void setReportYear(String reportYear) {
		this.reportYear = reportYear;
	}

	@Column(name = "Doc_number", insertable = false, updatable = false)
	public String getDocNumber() {
		return docNumber;
	}

	public void setDocNumber(String docNumber) {
		this.docNumber = docNumber;
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
