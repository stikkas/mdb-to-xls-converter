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

	private Integer tom;

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

	@Column(name = "Delo_title", insertable = false, updatable = false)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "Number_tom", insertable = false, updatable = false)
	public Integer getTom() {
		return tom;
	}

	public void setTom(Integer tom) {
		this.tom = tom;
	}

	@OneToMany(mappedBy = "delo", fetch = FetchType.EAGER)
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
