package com.kalab.chess.enginesupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


public class ChessEngineResolver {

    private static final String ENGINE_PROVIDER_MARKER = "intent.chess.provider.ENGINE";
    private static final String TAG = ChessEngineResolver.class.getSimpleName();
    private Context context;
    private String target;

    public ChessEngineResolver(Context context) {
        super();
        this.context = context;
        this.target = Build.CPU_ABI;
        sanitizeArmV6Target();
    }

    private void sanitizeArmV6Target() {
        if (this.target.startsWith("armeabi-v6")) {
            this.target = "armeabi";
        }
    }

    public List<ChessEngine> resolveEngines() {
        List<ChessEngine> result = new ArrayList<>();
        final Intent intent = new Intent(ENGINE_PROVIDER_MARKER);
        List<ResolveInfo> list = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.GET_META_DATA);
        for (ResolveInfo resolveInfo : list) {
            String packageName = resolveInfo.activityInfo.packageName;
            result = resolveEnginesForPackage(result, resolveInfo, packageName);
        }
        return result;
    }

    private List<ChessEngine> resolveEnginesForPackage(
            List<ChessEngine> result, ResolveInfo resolveInfo,
            String packageName) {
        if (packageName != null) {
            Log.d(TAG, "found engine provider, packageName=" + packageName);
            Bundle bundle = resolveInfo.activityInfo.metaData;
            if (bundle != null) {
                String authority = bundle
                        .getString("chess.provider.engine.authority");
                Log.d(TAG, "authority=" + authority);
                if (authority != null) {
                    try {
                        Resources resources = context
                                .getPackageManager()
                                .getResourcesForApplication(
                                        resolveInfo.activityInfo.applicationInfo);
                        int resId = resources.getIdentifier("enginelist",
                                "xml", packageName);
                        XmlResourceParser parser = resources.getXml(resId);
                        parseEngineListXml(parser, authority, result, packageName);
                    } catch (NameNotFoundException e) {
                        Log.e(TAG, e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return result;
    }

    private void parseEngineListXml(XmlResourceParser parser, String authority,
            List<ChessEngine> result, String packageName) {
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                String name = null;
                try {
                    if (eventType == XmlResourceParser.START_TAG) {
                        name = parser.getName();
                        if (name.equalsIgnoreCase("engine")) {
                            String fileName = parser.getAttributeValue(null,
                                    "filename");
                            String title = parser.getAttributeValue(null,
                                    "name");
                            String targetSpecification = parser
                                    .getAttributeValue(null, "target");
                            String[] targets = targetSpecification.split("\\|");
                            for (String cpuTarget : targets) {
                                if (target.equals(cpuTarget)) {
                                    result.add(new ChessEngine(title, fileName,
                                            authority, packageName));
                                }
                            }
                        }
                    }
                    eventType = parser.next();
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    /**
     * Don't use this in production - this method is only for testing. Set the
     * cpu target.
     * 
     * @param target
     *            the cpu target to set
     */
    public void setTarget(String target) {
        this.target = target;
        sanitizeArmV6Target();
    }
}
