/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2020-2024 Nextcloud GmbH and Nextcloud contributors
 * SPDX-FileCopyrightText: 2020-2021 Stefan Niedermann <info@niedermann.it>
 * SPDX-FileCopyrightText: 2020 Christoph Loy <loy.christoph@gmail.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package it.niedermann.owncloud.notes;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import it.niedermann.owncloud.notes.branding.BrandedActivity;
import it.niedermann.owncloud.notes.exception.ExceptionHandler;

public abstract class LockedActivity extends BrandedActivity {

    private static final String TAG = LockedActivity.class.getSimpleName();

    private static final int REQUEST_CODE_UNLOCK = 100;
    private View privacyCover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.currentThread().setUncaughtExceptionHandler(new ExceptionHandler(this));
        updateSecureFlag();

        if (NotesApplication.isLocked()) {
            showPrivacyCover();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateSecureFlag();
        if (NotesApplication.isLockEnabled()) {
            showPrivacyCover();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSecureFlag();

        if (!NotesApplication.isLocked()) {
            hidePrivacyCover();
            return;
        }

        showPrivacyCover();
        if (isFinishing()) {
            return;
        }

        if (NotesApplication.isLockFlowActive()) {
            return;
        }

        askToUnlock();
    }

    private void updateSecureFlag() {
        final var prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean preventScreenCapture = prefs.getBoolean(getString(R.string.pref_key_prevent_screen_capture), false);
        if (NotesApplication.isLockEnabled() || preventScreenCapture) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE_UNLOCK) {
            return;
        }

        NotesApplication.setLockFlowActive(false);

        if (resultCode == RESULT_OK) {
            Log.v(TAG, "Successfully unlocked device");
            NotesApplication.unlock();
            hidePrivacyCover();
        } else {
            Log.e(TAG, "Result code of unlocking was " + resultCode);
            finish();
        }
    }

    private void askToUnlock() {
        final KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager == null) {
            Log.e(TAG, "Keyguard manager is null");
            continueUnlocked();
            return;
        }

        final Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.unlock_notes), null);
        if (intent == null) {
            Log.e(TAG, "Keyguard intent is null");
            continueUnlocked();
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NotesApplication.setLockFlowActive(true);
        startActivityForResult(intent, REQUEST_CODE_UNLOCK);
    }

    private void continueUnlocked() {
        NotesApplication.unlock();
        hidePrivacyCover();
    }

    private void showPrivacyCover() {
        if (privacyCover != null) {
            return;
        }

        final TypedValue backgroundColor = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.colorBackground, backgroundColor, true);

        privacyCover = new View(this);
        privacyCover.setBackgroundColor(backgroundColor.data);
        privacyCover.setClickable(true);
        ((ViewGroup) getWindow().getDecorView()).addView(privacyCover,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void hidePrivacyCover() {
        if (privacyCover == null) {
            return;
        }

        ((ViewGroup) getWindow().getDecorView()).removeView(privacyCover);
        privacyCover = null;
    }
}
