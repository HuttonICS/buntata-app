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

public class LogEntryImage extends DatabaseObject
{
	public static final String TABLE_NAME        = "logentryimages";
	public static final String FIELD_LOGENTRY_ID = "logentry_id";
	public static final String FIELD_PATH        = "path";

	private Integer logEntryId;
	private String  path;

	private LogEntry logEntry;

	public LogEntryImage()
	{
		super(-1, new Date(), new Date());
	}

	public LogEntryImage(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public LogEntryImage(int id, Date createdOn, Date updatedOn, Integer logEntryId, String path)
	{
		super(id, createdOn, updatedOn);
		this.logEntryId = logEntryId;
		this.path = path;
	}

	public Integer getLogEntryId()
	{
		return logEntryId;
	}

	public LogEntryImage setLogEntryId(Integer logEntryId)
	{
		this.logEntryId = logEntryId;
		return this;
	}

	public String getPath()
	{
		return path;
	}

	public LogEntryImage setPath(String path)
	{
		this.path = path;
		return this;
	}

	public LogEntry getLogEntry()
	{
		return logEntry;
	}

	public LogEntryImage setLogEntry(LogEntry logEntry)
	{
		this.logEntry = logEntry;
		this.logEntryId = logEntry == null ? null : logEntry.getId();
		return this;
	}
}
