/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.recent;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.tablet.TabletStatusBar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import java.util.HashMap;
import java.util.Map;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.PixelFormat;

import com.mediatek.common.featureoption.FeatureOption;

public class RecentTasksLoader implements View.OnTouchListener {
    static final String TAG = "RecentTasksLoader";
    static final boolean DEBUG = TabletStatusBar.DEBUG || PhoneStatusBar.DEBUG || false;

    private static final int DISPLAY_TASKS = 20;
    private static final int MAX_TASKS = DISPLAY_TASKS + 1; // allow extra for non-apps

    private Context mContext;
    private RecentsPanelView mRecentsPanel;

    private Object mFirstTaskLock = new Object();
    private TaskDescription mFirstTask;
    private boolean mFirstTaskLoaded;

    private AsyncTask<Void, ArrayList<TaskDescription>, Void> mTaskLoader;
    private AsyncTask<Void, TaskDescription, Void> mThumbnailLoader;
    private Handler mHandler;

    private int mIconDpi;
    private Bitmap mDefaultThumbnailBackground;
    private Bitmap mDefaultIconBackground;
    private int mNumTasksInFirstScreenful = Integer.MAX_VALUE;

    private boolean mFirstScreenful;
    private ArrayList<TaskDescription> mLoadedTasks;

    private enum State { LOADING, LOADED, CANCELLED };
    private State mState = State.CANCELLED;


    private static RecentTasksLoader sInstance;
    public static RecentTasksLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RecentTasksLoader(context);
        }
        return sInstance;
    }

    private RecentTasksLoader(Context context) {
        mContext = context;

       /**Begin: modifie by lzp **/
	ComponentName component;
       if(FeatureOption.CENON_MULTILANGUAGE || android.provider.Settings.Global.getInt(context.getContentResolver(),android.provider.Settings.Global.SOFT_MODE, -1) == 1){
			mTitleByPackageName.put("com.ant4.n4getpaid", R.string.app_metodolist);
			mTitleByPackageName.put("com.yahoo.mobile.client.android.finance", R.string.app_stock);
			mTitleByPackageName.put("com.spotify.mobile.android.ui", R.string.app_iTunes);	
			mTitleByPackageName.put("com.runtastic.android.pro2", R.string.app_health);
           mTitleByPackageName.put("com.android.vending",R.string.app_appstore);
	         component = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
	        mTitleByComponentName.put(component, R.string.app_map);
	         mTitleByPackageName.put("com.yahoo.mobile.client.android.weather", R.string.app_weather);
        }
		else{
			mTitleByPackageName.put("org.espier.reminder", R.string.app_metodolist);
			mTitleByPackageName.put("com.android.iphonestock", R.string.app_stock);
			mTitleByPackageName.put("com.hupu.joggers", R.string.app_health);
			mTitleByPackageName.put("com.ts.zys", R.string.app_health);
			mTitleByPackageName.put("com.yidian.health", R.string.app_health);
			 mTitleByPackageName.put("com.andreader.prein", R.string.app_ibook);
			 mTitleByPackageName.put("com.shuqi.controller", R.string.app_ibook);			 
			 mTitleByPackageName.put("com.youku.phone", R.string.app_boke);
			mTitleByPackageName.put("com.renren.mini.android", R.string.app_findfriends); //added by chenfobao
			mTitleByPackageName.put("com.sand.airdroid", R.string.app_findphone); //added by chenfobao	
			 mTitleByPackageName.put("com.cmcc.mobilevideo", R.string.app_boke);
			if(!FeatureOption.CENON_DW5)
			  mTitleByPackageName.put("com.duomi.android", R.string.app_iTunes);	
		}	
       
       if(FeatureOption.CENON_WJ) {
			mTitleByPackageName.put("com.shootbubble.bubbledexlue", R.string.app_bubble);
			mTitleByPackageName.put("com.droidhen.fruit", R.string.app_fruit);
			mTitleByPackageName.put("com.UCMobile", R.string.app_ucbrowser_title);
			mTitleByPackageName.put("com.june.game.doudizhu", R.string.app_doudizhu);
	   }

	if(FeatureOption.CENON_MULTILANGUAGE || android.provider.Settings.Global.getInt(context.getContentResolver(),android.provider.Settings.Global.SOFT_MODE, -1) == 1){
		 component = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		 mIconByComponentName.put(component, R.drawable.app_map);
		 component = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.LatitudeActivity");
		 mIconByComponentName.put(component, R.drawable.app_latitude);
		 component = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.PlacesActivity");
		 mIconByComponentName.put(component, R.drawable.app_places);
		 component = new ComponentName("com.google.android.apps.maps", "com.google.android.maps.driveabout.app.DestinationActivity");
		 mIconByComponentName.put(component, R.drawable.app_navigation);		 
		 mIconByPackageName.put("com.facebook.katana", R.drawable.app_facebook);
		 mIconByPackageName.put("com.runtastic.android.pro2", R.drawable.health_icon);
		 mIconByPackageName.put("com.yahoo.mobile.client.android.finance", R.drawable.app_caijing);
		 mIconByPackageName.put("com.ant4.n4getpaid", R.drawable.app_inotes);    //   modify  by   heyue   20120910
		 mIconByPackageName.put("com.android.vending", R.drawable.app_appstore);
		 mIconByPackageName.put("com.yahoo.mobile.client.android.weather", R.drawable.app_weather);
		 mIconByPackageName.put("com.spotify.mobile.android.ui", R.drawable.app_duomi);
	}
	else{
		mIconByPackageName.put("com.autonavi.minimap", R.drawable.app_map);
		mIconByPackageName.put("com.baidu.BaiduMap", R.drawable.app_map);
		mIconByPackageName.put("com.android.iphonestock", R.drawable.app_caijing);
		mIconByPackageName.put("org.espier.reminder", R.drawable.app_inotes);    //   modify  by   heyue   20120910
		mIconByPackageName.put("com.tofly.ios.appstore", R.drawable.app_appstore);
		mIconByPackageName.put("com.cehz.ios.appstore", R.drawable.app_appstore);
		mIconByPackageName.put("com.duomi.android", R.drawable.app_duomi);
        mIconByPackageName.put("com.hupu.joggers", R.drawable.health_icon);
        mIconByPackageName.put("com.ts.zys", R.drawable.health_icon);
        	mIconByPackageName.put("com.yidian.health", R.drawable.health_icon);
		mIconByPackageName.put("com.andreader.prein", R.drawable.ibook_icon);
		mIconByPackageName.put("com.shuqi.controller", R.drawable.ibook_icon);		
		mIconByPackageName.put("com.youku.phone", R.drawable.boke_icon);
		mIconByPackageName.put("com.renren.mini.android", R.drawable.findfriends_icon); //added by chenfobao	
		mIconByPackageName.put("com.sand.airdroid", R.drawable.findphone_icon); //added by chenfobao	
		mIconByPackageName.put("com.cmcc.mobilevideo", R.drawable.boke_icon);
        mIconByPackageName.put("com.sztuyue.app.store", R.drawable.app_appstore);

        mIconByPackageName.put("com.hskj.iphonecalculator", R.drawable.calculator);
        mIconByPackageName.put("com.mediatek.filemanager", R.drawable.app_filemanager);
        component = new ComponentName("com.android.gallery3d", "com.android.camera.CameraLauncher");
        mIconByComponentName.put(component, R.drawable.launcher_camera);

        component = new ComponentName("com.android.contacts", "com.android.contacts.activities.PeopleActivity");
        mIconByComponentName.put(component, R.drawable.launcher_contacts);

    }
	 if(FeatureOption.CENON_DB) { 
		 mIconByPackageName.put("com.longcheertel.dbk", R.drawable.app_dbly);
		 component = new ComponentName("com.cncoman.customerService", "com.cncoman.customerService.activity.MainActivity");
		 mIconByComponentName.put(component, R.drawable.app_dbkf);		 
		 component = new ComponentName("com.cncoman.customerService", "com.cncoman.customerService.activity.WoDeDianZiBaoKaActivity");
		 mIconByComponentName.put(component, R.drawable.app_dbdzbk);	
	 }
	 
	 if(FeatureOption.CENON_WJ) {
		 mIconByPackageName.put("com.tencent.android.pad", R.drawable.app_qq);
		 mIconByPackageName.put("com.shootbubble.bubbledexlue", R.drawable.app_bubble);
		 mIconByPackageName.put("com.droidhen.fruit", R.drawable.app_fruit);
	 }
	 
	mIconByPackageName.put("com.joy7.safari", R.drawable.app_sfa);
	 /**Begin: modifie by lzp **/
	 
        mHandler = new Handler();

        final Resources res = context.getResources();

        // get the icon size we want -- on tablets, we use bigger icons
        boolean isTablet = res.getBoolean(R.bool.config_recents_interface_for_tablets);
        if (isTablet) {
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            mIconDpi = activityManager.getLauncherLargeIconDensity();
        } else {
            mIconDpi = res.getDisplayMetrics().densityDpi;
        }

        // Render default icon (just a blank image)
        int defaultIconSize = res.getDimensionPixelSize(com.android.internal.R.dimen.app_icon_size);
        int iconSize = (int) (defaultIconSize * mIconDpi / res.getDisplayMetrics().densityDpi);
        mDefaultIconBackground = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);

        // Render the default thumbnail background
        int thumbnailWidth =
                (int) res.getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_width);
        int thumbnailHeight =
                (int) res.getDimensionPixelSize(com.android.internal.R.dimen.thumbnail_height);
        int color = res.getColor(R.drawable.status_bar_recents_app_thumbnail_background);

        mDefaultThumbnailBackground =
                Bitmap.createBitmap(thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(mDefaultThumbnailBackground);
        c.drawColor(color);
    }

    public void setRecentsPanel(RecentsPanelView newRecentsPanel, RecentsPanelView caller) {
        // Only allow clearing mRecentsPanel if the caller is the current recentsPanel
        if (newRecentsPanel != null || mRecentsPanel == caller) {
            mRecentsPanel = newRecentsPanel;
            if (mRecentsPanel != null) {
                mNumTasksInFirstScreenful = mRecentsPanel.numItemsInOneScreenful();
            }
        }
    }

    public Bitmap getDefaultThumbnail() {
        return mDefaultThumbnailBackground;
    }

    public Bitmap getDefaultIcon() {
        return mDefaultIconBackground;
    }

    public ArrayList<TaskDescription> getLoadedTasks() {
        return mLoadedTasks;
    }

    public void remove(TaskDescription td) {
        mLoadedTasks.remove(td);
    }

    public boolean isFirstScreenful() {
        return mFirstScreenful;
    }

    private boolean isCurrentHomeActivity(ComponentName component, ActivityInfo homeInfo) {
        if (homeInfo == null) {
            final PackageManager pm = mContext.getPackageManager();
            homeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .resolveActivityInfo(pm, 0);
        }
        return homeInfo != null
            && homeInfo.packageName.equals(component.getPackageName())
            && homeInfo.name.equals(component.getClassName());
    }

    // Create an TaskDescription, returning null if the title or icon is null
    TaskDescription createTaskDescription(int taskId, int persistentTaskId, Intent baseIntent,
            ComponentName origActivity, CharSequence description) {
        Intent intent = new Intent(baseIntent);
        if (origActivity != null) {
            intent.setComponent(origActivity);
        }
        final PackageManager pm = mContext.getPackageManager();
        intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        if (resolveInfo != null) {
            final ActivityInfo info = resolveInfo.activityInfo;
            String title = info.loadLabel(pm).toString();

            if (title != null && title.length() > 0) {
                if (DEBUG) Log.v(TAG, "creating activity desc for id="
                        + persistentTaskId + ", label=" + title);

                TaskDescription item = new TaskDescription(taskId,
                        persistentTaskId, resolveInfo, baseIntent, info.packageName,
                        description);
		  String customtitle = (String)getCustomerTitle(mContext,baseIntent);//add by zqs 20130723 for custom title
		  if(customtitle == null)
                	item.setLabel(title);
		  else
		  	item.setLabel(customtitle);

                return item;
            } else {
                if (DEBUG) Log.v(TAG, "SKIPPING item " + persistentTaskId);
            }
        }
        return null;
    }

    private boolean getIsUninstall(ResolveInfo info) {
        boolean isUninstall = false;
        int flags = info.activityInfo.applicationInfo.flags;
        if ((flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
        	isUninstall = true;
        } else if ((flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
        	isUninstall = true;
        }
        return isUninstall;
    }	

    public static Drawable mergeDrawble(Context context, Drawable src) {
    	
    	Drawable background = context.getResources().getDrawable(R.drawable.icon_thirdpart);
	int defaultIconSize = context.getResources().getDimensionPixelSize(com.android.internal.R.dimen.app_icon_size);
    	Bitmap bitmap = Bitmap.createBitmap(defaultIconSize, defaultIconSize, background
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
    	Canvas canvas = new Canvas(bitmap);
    	background.setBounds(0, 0, defaultIconSize-2, defaultIconSize-1);
    	background.draw(canvas);
    	
    	int srcWidth = src.getIntrinsicWidth();
    	int srcHeight = src.getIntrinsicHeight();		
    	src.setBounds(0, 0, defaultIconSize-4, defaultIconSize-2);   
    	src.draw(canvas);
    	return new BitmapDrawable(context.getResources(), bitmap);
    }	

    void loadThumbnailAndIcon(TaskDescription td) {
        final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final PackageManager pm = mContext.getPackageManager();
        Bitmap thumbnail = am.getTaskTopThumbnail(td.persistentTaskId);
        Drawable icon = getFullResIcon(td.resolveInfo, pm);

	 if(getIsUninstall(td.resolveInfo)){
 		Log.v(TAG, "zqs is uninstall package :"+td.intent);
		icon = mergeDrawble(mContext,icon);
	 }

        if (DEBUG) Log.v(TAG, "Loaded bitmap for task "
                + td + ": " + thumbnail);
        synchronized (td) {
            if (thumbnail != null) {
                td.setThumbnail(thumbnail);
            } else {
                td.setThumbnail(mDefaultThumbnailBackground);
            }
            if (icon != null) {
		  Drawable customicon = getCustomerIcon(mContext,td.intent);//add by zqs 20130723 for custom icon
		  if(customicon == null)
                	td.setIcon(icon);
		  else
		  	td.setIcon(customicon);
            }
            td.setLoaded(true);
        }
    }

    Drawable getFullResDefaultActivityIcon() {
        return getFullResIcon(Resources.getSystem(),
                com.android.internal.R.mipmap.sym_def_app_icon);
    }

    Drawable getFullResIcon(Resources resources, int iconId) {
        try {
            return resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            return getFullResDefaultActivityIcon();
        }
    }

    private Drawable getFullResIcon(ResolveInfo info, PackageManager packageManager) {
        Resources resources;
        try {
            resources = packageManager.getResourcesForApplication(
                    info.activityInfo.applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            resources = null;
        }
        if (resources != null) {
            int iconId = info.activityInfo.getIconResource();
            if (iconId != 0) {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }

    Runnable mPreloadTasksRunnable = new Runnable() {
            public void run() {
                loadTasksInBackground();
            }
        };

    // additional optimization when we have software system buttons - start loading the recent
    // tasks on touch down
    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            preloadRecentTasksList();
        } else if (action == MotionEvent.ACTION_CANCEL) {
            cancelPreloadingRecentTasksList();
        } else if (action == MotionEvent.ACTION_UP) {
            // Remove the preloader if we haven't called it yet
            mHandler.removeCallbacks(mPreloadTasksRunnable);
            if (!v.isPressed()) {
                cancelLoadingThumbnailsAndIcons();
            }

        }
        return false;
    }

    public void preloadRecentTasksList() {
        mHandler.post(mPreloadTasksRunnable);
    }

    public void cancelPreloadingRecentTasksList() {
        cancelLoadingThumbnailsAndIcons();
        mHandler.removeCallbacks(mPreloadTasksRunnable);
    }

    public void cancelLoadingThumbnailsAndIcons(RecentsPanelView caller) {
        // Only oblige this request if it comes from the current RecentsPanel
        // (eg when you rotate, the old RecentsPanel request should be ignored)
        if (mRecentsPanel == caller) {
            cancelLoadingThumbnailsAndIcons();
        }
    }


    private void cancelLoadingThumbnailsAndIcons() {
        if (mTaskLoader != null) {
            mTaskLoader.cancel(false);
            mTaskLoader = null;
        }
        if (mThumbnailLoader != null) {
            mThumbnailLoader.cancel(false);
            mThumbnailLoader = null;
        }
        mLoadedTasks = null;
        if (mRecentsPanel != null) {
            mRecentsPanel.onTaskLoadingCancelled();
        }
        mFirstScreenful = false;
        mState = State.CANCELLED;
    }

    private void clearFirstTask() {
        synchronized (mFirstTaskLock) {
            mFirstTask = null;
            mFirstTaskLoaded = false;
        }
    }

    public void preloadFirstTask() {
        Thread bgLoad = new Thread() {
            public void run() {
                TaskDescription first = loadFirstTask();
                synchronized(mFirstTaskLock) {
                    if (mCancelPreloadingFirstTask) {
                        clearFirstTask();
                    } else {
                        mFirstTask = first;
                        mFirstTaskLoaded = true;
                    }
                    mPreloadingFirstTask = false;
                }
            }
        };
        synchronized(mFirstTaskLock) {
            if (!mPreloadingFirstTask) {
                clearFirstTask();
                mPreloadingFirstTask = true;
                bgLoad.start();
            }
        }
    }

    public void cancelPreloadingFirstTask() {
        synchronized(mFirstTaskLock) {
            if (mPreloadingFirstTask) {
                mCancelPreloadingFirstTask = true;
            } else {
                clearFirstTask();
            }
        }
    }

    boolean mPreloadingFirstTask;
    boolean mCancelPreloadingFirstTask;
    public TaskDescription getFirstTask() {
        while(true) {
            synchronized(mFirstTaskLock) {
                if (mFirstTaskLoaded) {
                    return mFirstTask;
                } else if (!mFirstTaskLoaded && !mPreloadingFirstTask) {
                    mFirstTask = loadFirstTask();
                    mFirstTaskLoaded = true;
                    return mFirstTask;
                }
            }
            try {
                Thread.sleep(3);
            } catch (InterruptedException e) {
            }
        }
    }

    public TaskDescription loadFirstTask() {
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasksForUser(
                1, ActivityManager.RECENT_IGNORE_UNAVAILABLE, UserHandle.CURRENT.getIdentifier());
        TaskDescription item = null;
        if (recentTasks.size() > 0) {
            ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(0);

            Intent intent = new Intent(recentInfo.baseIntent);
            if (recentInfo.origActivity != null) {
                intent.setComponent(recentInfo.origActivity);
            }

            // Don't load the current home activity.
            if (isCurrentHomeActivity(intent.getComponent(), null)) {
                return null;
            }

            // Don't load ourselves
            if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                return null;
            }

            item = createTaskDescription(recentInfo.id,
                    recentInfo.persistentId, recentInfo.baseIntent,
                    recentInfo.origActivity, recentInfo.description);
            if (item != null) {
                loadThumbnailAndIcon(item);
            }
            return item;
        }
        return null;
    }

    public void loadTasksInBackground() {
        loadTasksInBackground(false);
    }
    public void loadTasksInBackground(final boolean zeroeth) {
        if (mState != State.CANCELLED) {
            return;
        }
        mState = State.LOADING;
        mFirstScreenful = true;

        final LinkedBlockingQueue<TaskDescription> tasksWaitingForThumbnails =
                new LinkedBlockingQueue<TaskDescription>();
        mTaskLoader = new AsyncTask<Void, ArrayList<TaskDescription>, Void>() {
            @Override
            protected void onProgressUpdate(ArrayList<TaskDescription>... values) {
                if (!isCancelled()) {
                    ArrayList<TaskDescription> newTasks = values[0];
                    // do a callback to RecentsPanelView to let it know we have more values
                    // how do we let it know we're all done? just always call back twice
                    if (mRecentsPanel != null) {
                        mRecentsPanel.onTasksLoaded(newTasks, mFirstScreenful);
                    }
                    if (mLoadedTasks == null) {
                        mLoadedTasks = new ArrayList<TaskDescription>();
                    }
                    mLoadedTasks.addAll(newTasks);
                    mFirstScreenful = false;
                }
            }
            @Override
            protected Void doInBackground(Void... params) {
                // We load in two stages: first, we update progress with just the first screenful
                // of items. Then, we update with the rest of the items
                final int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                final PackageManager pm = mContext.getPackageManager();
                final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);

                final List<ActivityManager.RecentTaskInfo> recentTasks =
                        am.getRecentTasks(MAX_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);
                int numTasks = recentTasks.size();
                ActivityInfo homeInfo = new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);

                boolean firstScreenful = true;
                ArrayList<TaskDescription> tasks = new ArrayList<TaskDescription>();

                // skip the first task - assume it's either the home screen or the current activity.
                final int first = 0;
                for (int i = first, index = 0; i < numTasks && (index < MAX_TASKS); ++i) {
                    if (isCancelled()) {
                        break;
                    }
                    final ActivityManager.RecentTaskInfo recentInfo = recentTasks.get(i);

                    Intent intent = new Intent(recentInfo.baseIntent);
                    if (recentInfo.origActivity != null) {
                        intent.setComponent(recentInfo.origActivity);
                    }

                    // Don't load the current home activity.
                    if (isCurrentHomeActivity(intent.getComponent(), homeInfo)) {
                        continue;
                    }

                    // Don't load ourselves
                    if (intent.getComponent().getPackageName().equals(mContext.getPackageName())) {
                        continue;
                    }

                    TaskDescription item = createTaskDescription(recentInfo.id,
                            recentInfo.persistentId, recentInfo.baseIntent,
                            recentInfo.origActivity, recentInfo.description);

                    if (item != null) {
                        while (true) {
                            try {
                                tasksWaitingForThumbnails.put(item);
                                break;
                            } catch (InterruptedException e) {
                            }
                        }
                        tasks.add(item);
                        if (firstScreenful && tasks.size() == mNumTasksInFirstScreenful) {
                            publishProgress(tasks);
                            tasks = new ArrayList<TaskDescription>();
                            firstScreenful = false;
                            //break;
                        }
                        ++index;
                    }
                }

                if (!isCancelled()) {
                    publishProgress(tasks);
                    if (firstScreenful) {
                        // always should publish two updates
                        publishProgress(new ArrayList<TaskDescription>());
                    }
                }

                while (true) {
                    try {
                        tasksWaitingForThumbnails.put(new TaskDescription());
                        break;
                    } catch (InterruptedException e) {
                    }
                }

                Process.setThreadPriority(origPri);
                return null;
            }
        };
        mTaskLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        loadThumbnailsAndIconsInBackground(tasksWaitingForThumbnails);
    }

    private void loadThumbnailsAndIconsInBackground(
            final BlockingQueue<TaskDescription> tasksWaitingForThumbnails) {
        // continually read items from tasksWaitingForThumbnails and load
        // thumbnails and icons for them. finish thread when cancelled or there
        // is a null item in tasksWaitingForThumbnails
        mThumbnailLoader = new AsyncTask<Void, TaskDescription, Void>() {
            @Override
            protected void onProgressUpdate(TaskDescription... values) {
                if (!isCancelled()) {
                    TaskDescription td = values[0];
                    if (td.isNull()) { // end sentinel
                        mState = State.LOADED;
                    } else {
                        if (mRecentsPanel != null) {
                            mRecentsPanel.onTaskThumbnailLoaded(td);
                        }
                    }
                }
            }
            @Override
            protected Void doInBackground(Void... params) {
                final int origPri = Process.getThreadPriority(Process.myTid());
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                while (true) {
                    if (isCancelled()) {
                        break;
                    }
                    TaskDescription td = null;
                    while (td == null) {
                        try {
                            td = tasksWaitingForThumbnails.take();
                        } catch (InterruptedException e) {
                        }
                    }
                    if (td.isNull()) { // end sentinel
                        publishProgress(td);
                        break;
                    }
                    loadThumbnailAndIcon(td);

                    publishProgress(td);
                }

                Process.setThreadPriority(origPri);
                return null;
            }
        };
        mThumbnailLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

	//add by zqs begin 20130723 for custom title and icon
	private static Drawable getCustomerIcon(Context context, Intent intent){
    	ComponentName component = intent.getComponent();
    	int resourceId;
    	if(mIconByComponentName.get(component) == null){
    		String packageName = component.getPackageName();
    		if(mIconByPackageName.get(packageName) == null){
    			return null;
    		} else {
    			resourceId = mIconByPackageName.get(packageName);
    			return context.getResources().getDrawable(resourceId);
    		}
    	} else {
    		resourceId = mIconByComponentName.get(component);
			return context.getResources().getDrawable(resourceId);
    	}
    }
    
    private static Map<String, Integer> mIconByPackageName = new HashMap<String, Integer>();
    private static Map<ComponentName, Integer> mIconByComponentName = new HashMap<ComponentName, Integer>();
    
    static{
	ComponentName component;
        
        mIconByPackageName.put("com.youku.phone", R.drawable.app_tv);
	
	mIconByPackageName.put(FeatureOption.MTK_LCA_RAM_OPTIMIZE?"com.jr.gamecenter":"cn.ninegame.gamemanager", R.drawable.app_game);
	mIconByPackageName.put(FeatureOption.MTK_LCA_RAM_OPTIMIZE?"com.jr.gamecenter":"com.skymobi.opensky.androidho", R.drawable.app_game);
	mIconByPackageName.put("com.chaozh.iReaderFree15", R.drawable.app_ibooks);//modified by liuzepeng 	
	mIconByPackageName.put("com.chaozh.iReaderFree", R.drawable.app_ibooks);//modified by liuzepeng 	
	mIconByPackageName.put("com.UCMobile", R.drawable.app_ucbrowser);
        mIconByPackageName.put("com.skymobi.gallery", R.drawable.app_gallery);//add by liuzepeng
        mIconByPackageName.put("com.dianping.v1", R.drawable.app_passbook);//modified by wangyouyou
        mIconByPackageName.put("com.tencent.mobileqq", R.drawable.app_qq);
      mIconByPackageName.put("com.tencent.mm", R.drawable.app_weixin);
	
    }

	private static CharSequence getCustomerTitle(Context context, Intent intent){
    	ComponentName component = intent.getComponent();
	int stringId;
	if(component == null)
		return null;
    	if(mTitleByComponentName.get(component) == null){
    		String packageName = component.getPackageName();
		if(packageName == null)
			return null;
    		if(mTitleByPackageName.get(packageName) == null){
    			return null;
    		} else {
    			stringId = mTitleByPackageName.get(packageName);
    			return context.getResources().getString(stringId);
    		}
    	} else {
    			stringId = mTitleByComponentName.get(component);
			return context.getResources().getString(stringId);
    	}
    }
    
    private static Map<String, Integer> mTitleByPackageName = new HashMap<String, Integer>();
    private static Map<ComponentName, Integer> mTitleByComponentName = new HashMap<ComponentName, Integer>();
    
    static{
       ComponentName component;		
       mTitleByPackageName.put("com.skymobi.gallery", R.string.gallery);//add by liuzepeng
		if(!FeatureOption.CENON_DW5)	
			mTitleByPackageName.put("com.tofly.ios.appstore", R.string.app_appstore);
			mTitleByPackageName.put("com.cehz.ios.appstore", R.string.app_appstore);
        mTitleByPackageName.put("com.youku.phone", R.string.app_tv);
		mTitleByPackageName.put("com.autonavi.minimap", R.string.app_map);	
		mTitleByPackageName.put("com.baidu.BaiduMap", R.string.app_map);	

		mTitleByPackageName.put("com.chaozh.iReaderFree15", R.string.app_iReader);//modified by liuzepeng 	
		mTitleByPackageName.put("com.chaozh.iReaderFree", R.string.app_iReader);//modified by liuzepeng 	
		mTitleByPackageName.put(FeatureOption.MTK_LCA_RAM_OPTIMIZE?"com.jr.gamecenter":"cn.ninegame.gamemanager", R.string.app_game);
       	mTitleByPackageName.put(FeatureOption.MTK_LCA_RAM_OPTIMIZE?"com.jr.gamecenter":"com.skymobi.opensky.androidho", R.string.app_game);
		mTitleByPackageName.put("com.dianping.v1", R.string.app_wallet);//modified by wangyouyou
	
    }	

    //add by end begin 20130723 for custom title and icon
}
