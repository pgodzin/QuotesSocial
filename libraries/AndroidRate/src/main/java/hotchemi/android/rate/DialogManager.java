package hotchemi.android.rate;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

final class DialogManager {
    private static final String GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending";

    private DialogManager() {
    }

    static MaterialDialog.Builder create(final Context context, final boolean isShowNeutralButton,
                                         final boolean isShowTitle, final OnClickButtonListener listener,
                                         final View view) {

        final MaterialDialog.Builder builder = new MaterialDialog.Builder(context);

        builder.title(R.string.rate_dialog_title)
                .content(R.string.rate_dialog_message)
                .positiveText(R.string.rate_dialog_ok)
                .negativeText(R.string.rate_dialog_remind_later)
                .theme(Theme.LIGHT)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String packageName = context.getPackageName();
                        Intent intent = new Intent(Intent.ACTION_VIEW, UriHelper.getGooglePlay(packageName));
                        if (UriHelper.isPackageExists(context, GOOGLE_PLAY_PACKAGE_NAME)) {
                            intent.setPackage(GOOGLE_PLAY_PACKAGE_NAME);
                        }
                        context.startActivity(intent);
                        PreferenceHelper.setAgreeShowDialog(context, false);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        PreferenceHelper.setRemindInterval(context);
                    }
                });
        ;

        if (view != null) {
            builder.customView(view, true);
        }

        return builder;
    }

}