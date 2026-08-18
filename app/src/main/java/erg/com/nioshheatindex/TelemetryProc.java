package erg.com.nioshheatindex;

import android.os.Build;
import com.adobe.marketing.mobile.MobileCore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class TelemetryProc
{
    public static void appLaunch(String section, String pageName, String type) {

        final String sType = type;
        final String sSection = section;
        final String sPageName = pageName;

        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    int versionCode = BuildConfig.VERSION_CODE;
                    String versionName = BuildConfig.VERSION_NAME;
                    String appVersion = versionName + "." + String.valueOf(versionCode);
                    String device = Build.DEVICE;
                    String model = Build.MODEL;
                    String product = Build.PRODUCT;
                    String manufacturer = Build.MANUFACTURER;
                    String deviceType = device + ";" + model + ";" + product + ";" + manufacturer;
                    String osVersion = Build.VERSION.RELEASE;
                    String appName = "Heat Safety Tool";
                    String language = Locale.getDefault().getISO3Language();
                    String event = null;
                    // types are either app or nav
                    if("app".equals(sType))
                    {
                        event = "Application: Launch";
                    }
                    else
                    {
                        event = "Navigation: Section";
                    }
                    Map<String, String> contextData = new HashMap<String, String>();
                    contextData.put("&&channel", "NIOSH");
                    contextData.put("gov.cdc.language", language);
                    contextData.put("gov.cdc.appframework","Standalone");
                    contextData.put("gov.cdc.appversion", appVersion);
                    contextData.put("gov.cdc.appname", appName);
                    contextData.put("gov.cdc.osname", "Android");
                    contextData.put("gov.cdc.osversion", osVersion);
                    contextData.put("gov.cdc.devicetype", deviceType);
                    contextData.put("gov.cdc.status", "1");
                    contextData.put("gov.cdc.eventname", event);
                    contextData.put("gov.cdc.sectionname", sSection);

                    if(!contextData.isEmpty()){
                        //MobileCore.trackAction(sPageName,  contextData);
                        MobileCore.trackState(sPageName,  contextData);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
