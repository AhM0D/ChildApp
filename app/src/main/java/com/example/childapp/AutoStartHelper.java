package com.example.childapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class AutoStartHelper {

    /***
     * Xiaomi
     */
    private final String BRAND_XIAOMI = "xiaomi";
    private String PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter";
    private String PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity";

    /***
     * Letv
     */
    private final String BRAND_LETV = "letv";
    private String PACKAGE_LETV_MAIN = "com.letv.android.letvsafe";
    private String PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity";

    /***
     * ASUS ROG
     */
    private final String BRAND_ASUS = "asus";
    private String PACKAGE_ASUS_MAIN = "com.asus.mobilemanager";
    private String PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings";

    /***
     * Honor
     */
    private final String BRAND_HONOR = "honor";
    private String PACKAGE_HONOR_MAIN = "com.huawei.systemmanager";
    private String PACKAGE_HONOR_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity";

    /**
     * Oppo
     */
    private final String BRAND_OPPO = "oppo";
    private String PACKAGE_OPPO_MAIN = "com.coloros.safecenter";
    private String PACKAGE_OPPO_FALLBACK = "com.oppo.safe";
    private String PACKAGE_OPPO_COMPONENT = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK = "com.oppo.safe.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK_A = "com.coloros.safecenter.startupapp.StartupAppListActivity";

    /**
     * Vivo
     */

    private final String BRAND_VIVO = "vivo";
    private String PACKAGE_VIVO_MAIN = "com.iqoo.secure";
    private String PACKAGE_VIVO_FALLBACK = "com.vivo.perm;issionmanager";
    private String PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK_A = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager";

    /**
     * Nokia
     */

    private final String BRAND_NOKIA = "nokia";
    private String PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3";
    private String PACKAGE_NOKIA_COMPONENT = "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity";


    private AutoStartHelper() {
    }

    public static AutoStartHelper getInstance() {
        return new AutoStartHelper();
    }


    public void getAutoStartPermission(Context context) {

        String build_info = Build.BRAND.toLowerCase();
        String manufacturer = android.os.Build.MANUFACTURER;
        try {
            Intent intent = new Intent();
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                context.startActivity(intent);
            } else  {
                switch (build_info) {
                    case BRAND_ASUS:
                        autoStartAsus(context);
                        break;
                    case BRAND_XIAOMI:
                        autoStartXiaomi(context);
                        break;
                    case BRAND_LETV:
                        autoStartLetv(context);
                        break;
                    case BRAND_HONOR:
                        autoStartHonor(context);
                        break;
                    case BRAND_OPPO:
                        autoStartOppo(context);
                        break;
                    case BRAND_VIVO:
                        autoStartVivo(context);
                        break;
                    case BRAND_NOKIA:
                        autoStartNokia(context);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoStartAsus(final Context context) {
        if (isPackageExists(context, PACKAGE_ASUS_MAIN)) {

            showAlert(context, (dialog, which) -> {
                try {
                    startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            });

        }


    }

    private void showAlert(Context context, DialogInterface.OnClickListener onClickListener) {

        new AlertDialog.Builder(context).setTitle("Allow AutoStart")
                .setMessage("Please enable auto start in settings.")
                .setPositiveButton("Allow", onClickListener).show().setCancelable(false);
    }

    private void autoStartXiaomi(final Context context) {
        if (isPackageExists(context, PACKAGE_XIAOMI_MAIN)) {
            showAlert(context, (dialog, which) -> {
                try {
                    startIntent(context, PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


        }
    }

    private void autoStartLetv(final Context context) {
        if (isPackageExists(context, PACKAGE_LETV_MAIN)) {
            showAlert(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        startIntent(context, PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }


    private void autoStartHonor(final Context context) {
        if (isPackageExists(context, PACKAGE_HONOR_MAIN)) {
            showAlert(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        startIntent(context, PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }

    private void autoStartOppo(final Context context) {
        if (isPackageExists(context, PACKAGE_OPPO_MAIN) || isPackageExists(context, PACKAGE_OPPO_FALLBACK)) {
            showAlert(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            startIntent(context, PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            try {
                                startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A);
                            } catch (Exception exx) {
                                exx.printStackTrace();
                            }

                        }

                    }
                }
            });


        }
    }

    private void autoStartVivo(final Context context) {
        if (isPackageExists(context, PACKAGE_VIVO_MAIN) || isPackageExists(context, PACKAGE_VIVO_FALLBACK)) {
            showAlert(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            startIntent(context, PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            try {
                                startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A);
                            } catch (Exception exx) {
                                exx.printStackTrace();
                            }

                        }

                    }

                }
            });
        }
    }

    private void autoStartNokia(final Context context) {
        if (isPackageExists(context, PACKAGE_NOKIA_MAIN)) {
            showAlert(context, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    try {
                        startIntent(context, PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    private void startIntent(Context context, String packageName, String componentName) throws Exception {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, componentName));
            context.startActivity(intent);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw var5;
        }
    }

    private Boolean isPackageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo :
                packages) {
            if (packageInfo.packageName.equals(targetPackage)) {
                return true;
            }
        }

        return false;
    }
}