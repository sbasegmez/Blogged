package com.developi.mcdemo;

import java.io.Serializable;

import com.ibm.commons.util.StringUtil;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

public class Participant implements Serializable {

	private static final long serialVersionUID = 1L;

	// Data Fields
	private String mainDocId;
	private String name;
	private boolean lcvProvided;
	
	// UI Properties
	private boolean selected;
	private boolean deleted;
	private String noteId;

	// No permission for an empty constructor
	private Participant() { 
		setSelected(false);
		setDeleted(false);
		noteId="";
	}

	public Participant(String mainDocId) {
		setMainDocId(mainDocId);
		setLcvProvided(false);
	}
	
	public Participant(String mainDocId, String name) {
		this(mainDocId);
		
		setName(name);
	}
	
	/**
	 * Loads a participant document contents into the object.
	 * 
	 * @param Participant Document
	 * @throws NotesException 
	 */
	public Participant(Document doc) throws NotesException {
		this();
		
		setMainDocId(doc.getItemValueString("MainDocId"));
		setName(doc.getItemValueString("Name"));
		setLcvProvided("1".equals(doc.getItemValueString("LCVFlag")));
		
		// NoteID is important. If somehow noteid is empty, we'll know the participant is new.
		this.noteId=doc.getNoteID();
		
	}

	public void saveToDb(Database db) throws NotesException {
		Document pDoc=null;
		
		if(isNew()) {
			pDoc=db.createDocument();
			pDoc.replaceItemValue("Form", "Participant");
		} else {
			pDoc=db.getDocumentByID(getNoteId());
		}
		
		if(isDeleted()) {
			// Might not be able to delete it. So change the form.
			pDoc.replaceItemValue("Form", "Participant_DELETED");
		} else { 
		
			pDoc.replaceItemValue("MainDocId", getMainDocId());
			pDoc.replaceItemValue("Name", getName());
			pDoc.replaceItemValue("LCVFlag", isLcvProvided()?"1":"");
		}
		
		pDoc.computeWithForm(false, false);
		pDoc.save();
		noteId=pDoc.getNoteID();
	}
	
	public String getNoteId() {
		return noteId;
	}

	public String getMainDocId() {
		return mainDocId;
	}

	public void setMainDocId(String mainDocId) {
		this.mainDocId = mainDocId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLcvProvided() {
		return lcvProvided;
	}

	public void setLcvProvided(boolean lcvProvided) {
		this.lcvProvided = lcvProvided;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public boolean isNew() {
		return StringUtil.isEmpty(getNoteId());
	}

	public String getCardProperties() {
		String props= (isNew()?" new":"") + (isDeleted()?" deleted":"") + (isLcvProvided()?" lcv":"");
		
		return props.trim();
	}
	
	public void toggleDelete() {
		setDeleted(!isDeleted());
	}

	public void toggleLcv() {
		setLcvProvided(! isLcvProvided());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mainDocId == null) ? 0 : mainDocId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Participant))
			return false;
		Participant other = (Participant) obj;
		if (mainDocId == null) {
			if (other.mainDocId != null)
				return false;
		} else if (!mainDocId.equals(other.mainDocId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return name + " (noteId:"+noteId+")";
	}
	
}
