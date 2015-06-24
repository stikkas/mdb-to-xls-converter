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
@Table(name = "Delo")
public class Delo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String title;

	private Calendar startDate;

	private Calendar endDate;

	private Integer tom;

	private String graph;

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

	@Column(name = "Delo_title", insertable = false, updatable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@NotNull(message = "номер тома отсутствует")
	@Column(name = "Number_tom", insertable = false, updatable = false)
	public Integer getTom() {
		return tom;
	}

	public void setTom(Integer tom) {
		this.tom= tom;
	}

	@Column(name = "Graph_delo", insertable = false, updatable = false)
	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	@OneToMany(mappedBy = "delo", fetch = FetchType.EAGER)
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
