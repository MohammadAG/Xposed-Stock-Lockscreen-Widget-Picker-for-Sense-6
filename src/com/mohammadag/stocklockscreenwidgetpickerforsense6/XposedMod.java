package com.mohammadag.stocklockscreenwidgetpickerforsense6;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {
	private static int REQUEST_PICK_APPWIDGET;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.htc.lockscreen"))
			return;

		Class<?> HtcKeyguardAppWidgetPickActivity = XposedHelpers.findClass("com.htc.lockscreen.HtcKeyguardAppWidgetPickActivity",
				lpparam.classLoader);
		try {
			REQUEST_PICK_APPWIDGET = XposedHelpers.getStaticIntField(HtcKeyguardAppWidgetPickActivity, "REQUEST_PICK_APPWIDGET");
		} catch (Throwable t) {
			// Fall back to what we knew when this module was written
			REQUEST_PICK_APPWIDGET = 126;
		}

		XposedHelpers.findAndHookMethod(HtcKeyguardAppWidgetPickActivity,
				"launchWidgetPicker", new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
				Intent localIntent = new Intent();
				localIntent.setComponent(new ComponentName("com.android.settings", "com.android.settings.KeyguardAppWidgetPickActivity"));
				localIntent.putExtra("appWidgetId", XposedHelpers.getIntField(param.thisObject, "mAppWidgetId"));
				try {
					((Activity) param.thisObject).startActivityForResult(localIntent, REQUEST_PICK_APPWIDGET);
				} catch (SecurityException e) {
					e.printStackTrace();
					Toast.makeText(((Activity) param.thisObject), "No permission to launch widget picker",
							Toast.LENGTH_SHORT).show();
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
					Toast.makeText(((Activity) param.thisObject), "HTC broke this module",
							Toast.LENGTH_SHORT).show();
				} catch (Throwable e) {
					e.printStackTrace();
					Toast.makeText(((Activity) param.thisObject), "Failed to laucnh widget picker",
							Toast.LENGTH_SHORT).show();
				}
				return null;
			}
		});
	}
}
