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

package uk.ac.hutton.ics.buntata.activity;

import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.support.design.widget.*;
import android.support.v4.content.*;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.*;

import java.io.*;

import butterknife.*;
import uk.ac.hutton.ics.buntata.*;
import uk.ac.hutton.ics.buntata.database.entity.*;
import uk.ac.hutton.ics.buntata.util.*;

/**
 * The {@link VideoActivity} contains the {@link uk.ac.hutton.ics.buntata.fragment.DatasourceFragment}. It's used to select the data source.
 *
 * @author Sebastian Raubach
 */
public class VideoActivity extends BaseActivity implements MediaController.MediaPlayerControl
{
	public static final String PARAM_MEDIA         = "PARAM_MEDIA";
	public static final String PARAM_DATASOURCE_ID = "PARAM_DATASOURCE_ID";

	private MediaPlayer player;
	private Uri         uri;

	@BindView(R.id.video_view_surface)
	SurfaceView sv;
	@BindView(R.id.toolbar)
	Toolbar     toolbar;

	private MediaController mediaController;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ButterKnife.bind(this);

		/* Get parameters */
		Bundle args = getIntent().getExtras();
		BuntataMediaAdvanced media = (BuntataMediaAdvanced) args.getSerializable(PARAM_MEDIA);
		int datasourceId = args.getInt(PARAM_DATASOURCE_ID, -1);

		/* Prompt the user to select at least one data source */
		if (!PreferenceUtils.getPreferenceAsBoolean(this, PreferenceUtils.PREFS_AT_LEAST_ONE_DATASOURCE, false))
			SnackbarUtils.show(findViewById(android.R.id.content), R.string.snackbar_select_datasource, Snackbar.LENGTH_LONG);

		setSupportActionBar(toolbar);

		/* Set the toolbar as the action bar */
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setTitle(media.getName());
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			{
				getSupportActionBar().setHomeButtonEnabled(true);
			}
		}

		File file = FileUtils.getFileForDatasource(this, datasourceId, media.getInternalLink());
		uri = Uri.fromFile(file);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();

		if (player != null)
		{
			player.pause();
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (player == null)
		{
			try
			{
				player = new MediaPlayer();
				player.setDataSource(this, uri);

				mediaController = new MediaController(this);

				sv.getHolder().addCallback(new SurfaceHolder.Callback()
				{
					@Override
					public void surfaceCreated(SurfaceHolder holder)
					{
						player.setDisplay(holder);
					}

					@Override
					public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
					{
						player.setDisplay(holder);
					}

					@Override
					public void surfaceDestroyed(SurfaceHolder holder)
					{
					}
				});

				player.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener()
				{
					@Override
					public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
					{
						// // Get the dimensions of the video
						int videoWidth = player.getVideoWidth();
						int videoHeight = player.getVideoHeight();
						float videoProportion = (float) videoWidth / (float) videoHeight;

						// Get the width of the screen
						Point point = new Point();
						getWindowManager().getDefaultDisplay().getSize(point);
						int screenWidth = point.x;
						int screenHeight = point.y;
						float screenProportion = (float) screenWidth / (float) screenHeight;

						// Get the SurfaceView layout parameters
						ViewGroup.LayoutParams lp = sv.getLayoutParams();
						if (videoProportion > screenProportion)
						{
							lp.width = screenWidth;
							lp.height = (int) ((float) screenWidth / videoProportion);
						}
						else
						{
							lp.width = (int) (videoProportion * (float) screenHeight);
							lp.height = screenHeight;
						}
						// Commit the layout parameters
						sv.setLayoutParams(lp);

						player.start();
					}
				});
				player.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
				{
					@Override
					public void onPrepared(MediaPlayer mp)
					{
						mediaController.setMediaPlayer(VideoActivity.this);
						mediaController.setAnchorView(sv);
					}
				});
				player.prepare();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//the MediaController will hide after 3 seconds - tap the screen to make it appear again
		mediaController.show();
		return true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		if (player != null)
		{
			player.stop();
			player.release();
			player = null;
		}
	}

	@Override
	protected Integer getLayoutId()
	{
		return R.layout.activity_video;
	}

	@Override
	protected Integer getToolbarId()
	{
		return R.id.toolbar;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void start()
	{
		player.start();
	}

	@Override
	public void pause()
	{
		player.pause();
	}

	@Override
	public int getDuration()
	{
		return player.getDuration();
	}

	@Override
	public int getCurrentPosition()
	{
		return player.getCurrentPosition();
	}

	@Override
	public void seekTo(int pos)
	{
		player.seekTo(pos);
	}

	@Override
	public boolean isPlaying()
	{
		return player.isPlaying();
	}

	@Override
	public int getBufferPercentage()
	{
		return 0;
	}

	@Override
	public boolean canPause()
	{
		return true;
	}

	@Override
	public boolean canSeekBackward()
	{
		return true;
	}

	@Override
	public boolean canSeekForward()
	{
		return true;
	}

	@Override
	public int getAudioSessionId()
	{
		return 0;
	}
}
