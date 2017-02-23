/*
 * Copyright 2016 Information & Computational Sciences, The James Hutton Institute
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

public class BuntataDatasourceAdvanced extends BuntataDatasource
{
	private InstallState state;
	private boolean isDownloading = false;

	public BuntataDatasourceAdvanced()
	{
	}

	public BuntataDatasourceAdvanced(int id, Date createdOn, Date updatedOn)
	{
		super(id, createdOn, updatedOn);
	}

	public BuntataDatasourceAdvanced(int id, Date createdOn, Date updatedOn, String name, String description, int versionNumber, String dataProvider, String contact, boolean showKeyName, String icon, long size, InstallState state)
	{
		super(id, createdOn, updatedOn, name, description, versionNumber, dataProvider, contact, showKeyName, icon, size);
		this.state = state;
	}

	public InstallState getState()
	{
		return state;
	}

	public BuntataDatasourceAdvanced setState(InstallState state)
	{
		this.state = state;
		return this;
	}

	public boolean isDownloading()
	{
		return isDownloading;
	}

	public BuntataDatasourceAdvanced setDownloading(boolean downloading)
	{
		isDownloading = downloading;
		return this;
	}

	public static BuntataDatasourceAdvanced create(BuntataDatasource ds)
	{
		BuntataDatasourceAdvanced result = new BuntataDatasourceAdvanced();
		result.setId(ds.getId());
		result.setName(ds.getName());
		result.setDescription(ds.getDescription());
		result.setContact(ds.getContact());
		result.setDataProvider(ds.getDataProvider());
		result.setVersionNumber(ds.getVersionNumber());
		result.setSizeTotal(ds.getSizeTotal());
		result.setSizeNoVideo(ds.getSizeNoVideo());
		result.setIcon(ds.getIcon());
		result.setShowKeyName(ds.isShowKeyName());
		result.setCreatedOn(ds.getCreatedOn());
		result.setUpdatedOn(ds.getUpdatedOn());

		return result;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	/**
	 * {@link InstallState} represents the different states a {@link BuntataDatasourceAdvanced} can have locally.
	 */
	public enum InstallState
	{
		/** The data source isn't installed at all */
		NOT_INSTALLED,
		/** The data source is installed, but there's an update */
		INSTALLED_HAS_UPDATE,
		/** The data source is installed and there is no update */
		INSTALLED_NO_UPDATE
	}
}
