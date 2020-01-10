package it.niedermann.owncloud.notes.android.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.helper.VersionCheckHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.niedermann.owncloud.notes.R;

public class ExceptionActivity extends AppCompatActivity {

    Throwable throwable;

    @BindView(R.id.message)
    TextView message;
    @BindView(R.id.stacktrace)
    TextView stacktrace;

    public static final String KEY_THROWABLE = "T";

    @SuppressLint("SetTextI18n") // only used for logging
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_exception);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));
        throwable.printStackTrace();
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.simple_error));
        this.message.setText(throwable.getMessage());


        String debugInfo = "";

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            debugInfo += "App Version: " + pInfo.versionName;
            debugInfo += "\nApp Version Code: " + pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            debugInfo += "\nApp Version: " + e.getMessage();
            e.printStackTrace();
        }

        try {
            debugInfo += "\nFiles App Version Code: " + VersionCheckHelper.getNextcloudFilesVersionCode(this);
        } catch (PackageManager.NameNotFoundException e) {
            debugInfo += "\nFiles App Version Code: " + e.getMessage();
            e.printStackTrace();
        }

        debugInfo += "\n\n---\n";
        debugInfo += "\nOS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        debugInfo += "\nOS API Level: " + android.os.Build.VERSION.SDK_INT;
        debugInfo += "\nDevice: " + android.os.Build.DEVICE;
        debugInfo += "\nModel (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";

        debugInfo += "\n\n---";

        this.stacktrace.setText(debugInfo + "\n\n" + getStacktraceOf(throwable));
    }

    private String getStacktraceOf(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


    @OnClick(R.id.copy)
    void copyStacktraceToClipboard() {
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getString(R.string.simple_exception), "```\n" + this.stacktrace.getText() + "\n```");
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.close)
    void close() {
        finish();
    }
}