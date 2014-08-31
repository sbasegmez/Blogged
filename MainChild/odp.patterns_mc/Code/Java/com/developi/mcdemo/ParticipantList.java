package com.developi.mcdemo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.developi.util.Utils;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

public class ParticipantList implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DOCSOURCE_NAME = "eventDoc";

	private List<Participant> _list;

	private String eventCode;

	private DominoDocument getDominoDoc() {
		return (DominoDocument) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), DOCSOURCE_NAME);
	}

	private Document getDoc() {
		try {
			return getDominoDoc().getDocument(true);
		} catch (NotesException e) {
			e.printStackTrace();
		}

		return null;
	}

	private void loadList(String eventCode) {
		Database db=ExtLibUtil.getCurrentDatabase();

		View view=null;
		ViewEntryCollection entries=null;

		try {
			view=db.getView("ParticipantsByEventCode");
			entries=view.getAllEntriesByKey(eventCode, true);

			ViewEntry ve=entries.getFirstEntry();

			while(ve!=null) {

				Document pDoc=null;
				try {
					pDoc=ve.getDocument();

					_list.add(new Participant(pDoc));
				} finally {
					Utils.recycleObject(pDoc);
				}

				ViewEntry tmpEntry=ve;
				ve=entries.getNextEntry(tmpEntry);
				Utils.recycleObject(tmpEntry);
			}

		} catch (NotesException e) {
			// Use OpenNTF Domino API to get rid of this.
			e.printStackTrace();
		} finally {
			Utils.recycleObjects(view, entries);
		}
	}


	/**
	 * This example uses DocumentUniqueId as the event code.
	 * 
	 * @return the event code we will use for Participant docs
	 */
	private String getEventCode() {

		if(StringUtil.isEmpty(eventCode)) {

			Document eventDoc=getDoc();

			try {
				eventCode = eventDoc.getUniversalID();
			} catch (NotesException e) {
				// Use OpenNTF Domino API to get rid of this.
				e.printStackTrace();
			}

		}

		return eventCode;
	}

	public List<Participant> getList() {
		if(_list==null) {
			_list=new ArrayList<Participant>();
	
			DominoDocument dominoDoc=getDominoDoc();
	
			try {
				if(dominoDoc.isNewNote()) {
					// It will be empty on a new document
				} else {
					loadList(getEventCode());
				}
			} catch (NotesException e) {
				// Use OpenNTF Domino API to get rid of this.
				e.printStackTrace();
			}
		}
	
		return _list;
	}

	/**
	 * This will be called from the data source event...
	 * 
	 * It will save/update participant documents and make deletions.
	 * 
	 */
	public void saveParticipants() {
		try {
			Iterator<Participant> iterator=_list.iterator();
			
			while(iterator.hasNext()) {
				Participant p=iterator.next();
				
				p.saveToDb(ExtLibUtil.getCurrentDatabase());
				
				if(p.isDeleted()) {
					iterator.remove();
				}
			}

		} catch (NotesException e) {
			// Use OpenNTF Domino API to get rid of this.
			e.printStackTrace();
		}

	}
	
	public void addSelected() {
		Map<String, Object> viewScope=ExtLibUtil.getViewScope();
		
		Object names=viewScope.get("selectedNames");
		
		if(names instanceof String) {
			add((String) names);
		} else {
			// It should be a list of Strings, we hope.
			for(Object o:(List<?>) names) {
				add((String)o);
			}
		}
		
		viewScope.remove("selectedNames");
	}
	
	public void selectAll() {
		for(Participant p:_list) {
			p.setSelected(true);
		}
	}
	
	public void deselectAll() {
		for(Participant p:_list) {
			p.setSelected(false);
		}
	}
	
	public void removeSelected() {
		Iterator<Participant> iterator=_list.iterator();
		
		while(iterator.hasNext()) {
			Participant p=iterator.next();
				
			if(p.isSelected()) {
				if(p.isNew()) {
					iterator.remove();
				} else {
					p.setDeleted(true);
				}
				
				p.setSelected(false);
			}
		}
	}
	
	public void toggleLcv() {
		for(Participant p:_list) {
			if(p.isSelected()) {
				p.toggleLcv();
				p.setSelected(false);
			}
		}
	}
	
	public void add(String name) {
		Participant participant=new Participant(getEventCode(), name);
		_list.add(participant);
		
	}

	public void sortByLcv() {
		Collections.sort(_list, new Comparator<Participant>() {

			public int compare(Participant p1, Participant p2) {
				return (p1.isLcvProvided() && p2.isLcvProvided())?0:(p1.isLcvProvided()?-1:1);
			}
			
		});
	}
	
	public void sortByName() {
		Collections.sort(_list, new Comparator<Participant>() {

			public int compare(Participant p1, Participant p2) {
				return p1.getName().compareTo(p2.getName());
			}
			
		});
	}

	/**
	 * Will be triggered by an outline onItemClick event. Can be used for toolbars, dropdown menus, etc.
	 */
	public void toolbarClick() {
		XSPContext context=XSPContext.getXSPContext(FacesContext.getCurrentInstance());
		String selection=context.getSubmittedValue();
		
		if("selectAll".equals(selection)) {
			selectAll();
		} else if("deselectAll".equals(selection)) {
			deselectAll();
		} else if("removeSelected".equals(selection)) {
			removeSelected();
		} else if("toggleLcv".equals(selection)) {
			toggleLcv();
		} else if("sortByLcv".equals(selection)) {
			sortByLcv();
		} else if("sortByName".equals(selection)) {
			sortByName();
		}
		
	}
	
}
