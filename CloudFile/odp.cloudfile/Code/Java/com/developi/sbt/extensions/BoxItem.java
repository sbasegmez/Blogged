package com.developi.sbt.extensions;

/**
 * BoxItem class is a representation of a single BoxItem object (file or folder) within Box...
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.developi.toolbox.DevelopiUtils;
import com.developi.toolbox.RestUtils;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;

public class BoxItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static enum ItemType {FILE, FOLDER }; 

	private ItemType type;
	
	private String id;
	private String sequenceId;
	private String etag;
	private String name;
	private String description;
	private long size; 
	private Date createdAt; //created_at
	private String url; 

	private BoxPerson creator;

	private List<BoxItem> pathCollection;
	
	public BoxItem() {}

	public BoxItem(JsonJavaObject data) {
		this();
		
		if(data!=null) {

			this.type="folder".equals(data.getAsString("type"))?ItemType.FOLDER:ItemType.FILE;
			
			this.id=data.getAsString("id");
			this.sequenceId=data.getAsString("sequence-id");
			this.etag=data.getAsString("etag");
			this.name = data.getAsString("name");
			this.description=data.getAsString("description");
			this.size = data.getAsLong("size");
			
			try {
				this.createdAt=JsonGenerator.stringToDate(data.getAsString("created-at"));
			} catch (Exception e) {}
				
			JsonJavaObject createdBy=data.getJsonObject("created_by");
			if(createdBy!=null) this.creator=new BoxPerson(createdBy);

			pathCollection=new ArrayList<BoxItem>();
			
			JsonJavaObject pathObj=data.getJsonObject("path_collection");
			if(pathObj!=null && pathObj.containsKey("entries")) {
				JsonJavaArray entries=pathObj.getAsArray("entries");
				for(Object path: entries) {
					if(path instanceof JsonJavaObject) {
						pathCollection.add(new BoxItem((JsonJavaObject) path));		
					}
				}
			}

			JsonJavaObject sLink=data.getJsonObject("shared_link");
			if(sLink!=null) this.url=sLink.getAsString("url");
		}

	}

	public String getIcon() {
		if(isFolder()) {
			return "ct-folder";
		} else {
			String ext=DevelopiUtils.strRightBack(getName(), ".");
			if(RestUtils.ICONMAP.containsKey(ext)) return RestUtils.ICONMAP.get(ext);
		}
		return "ct-default";
	}

	@Override
	public String toString() {
		return "#"+id+": "+name+" ("+type+" - "+size+" bytes)";
	}

	public String getId() {
		return id;
	}

	public String getSequenceId() {
		return sequenceId;
	}

	public String getEtag() {
		return etag;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public long getSize() {
		return size;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public String getUrl() {
		return url;
	}

	public BoxPerson getCreator() {
		return creator;
	}

	public List<BoxItem> getPathCollection() {
		return pathCollection;
	}

	public ItemType getType() {
		return type;
	}
	
	public boolean isFile() {
		return type.equals(ItemType.FILE);
	}
	
	public boolean isFolder() {
		return type.equals(ItemType.FOLDER);
	}

}
