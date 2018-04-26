/*
 * Copyright 2017 Information & Computational Sciences, The James Hutton Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.hutton.ics.buntata.database.entity;

import java.util.*;

import jhi.buntata.resource.*;

/**
 * @author Sebastian Raubach
 */

public class LogEntry extends DatabaseObject
{
	public static final String TABLE_NAME          = "logentries";
	public static final String FIELD_DATASOURCE_ID = "datasource_id";
	public static final String FIELD_NODE_ID       = "node_id";
	public static final String FIELD_NODE_NAME     = "node_name";
	public static final String FIELD_NOTE          = "note";
	public static final String FIELD_LATITUDE      = "latitude";
	public static final String FIELD_LONGITUDE     = "longitude";
	public static final String FIELD_ELEVATION     = "elevation";

	private Integer datasourceId;
	private Integer nodeId;
	private String  nodeName;
	private String  note;
	private Double  latitute;
	private Double  longitude;
	private Double  elevation;

	public LogEntry()
	{
		super(-1, new Date(), new Date());
	}

	public LogEntry(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public LogEntry(int id, Date createdOn, Date updatedOn, Integer datasourceId, Integer nodeId, String nodeName, String note, Double latitute, Double longitude, Double elevation)
	{
		super(id, createdOn, updatedOn);
		this.datasourceId = datasourceId;
		this.nodeId = nodeId;
		this.nodeName = nodeName;
		this.note = note;
		this.latitute = latitute;
		this.longitude = longitude;
		this.elevation = elevation;
	}

	public Integer getDatasourceId()
	{
		return datasourceId;
	}

	public LogEntry setDatasourceId(Integer datasourceId)
	{
		this.datasourceId = datasourceId;
		return this;
	}

	public Integer getNodeId()
	{
		return nodeId;
	}

	public LogEntry setNodeId(Integer nodeId)
	{
		this.nodeId = nodeId;
		return this;
	}

	public String getNodeName()
	{
		return nodeName;
	}

	public LogEntry setNodeName(String nodeName)
	{
		this.nodeName = nodeName;
		return this;
	}

	public String getNote()
	{
		return note;
	}

	public LogEntry setNote(String note)
	{
		this.note = note;
		return this;
	}

	public Double getLatitute()
	{
		return latitute;
	}

	public LogEntry setLatitute(Double latitute)
	{
		this.latitute = latitute;
		return this;
	}

	public Double getLongitude()
	{
		return longitude;
	}

	public LogEntry setLongitude(Double longitude)
	{
		this.longitude = longitude;
		return this;
	}

	public Double getElevation()
	{
		return elevation;
	}

	public LogEntry setElevation(Double elevation)
	{
		this.elevation = elevation;
		return this;
	}
}
