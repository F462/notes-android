/*
 * Nextcloud Notes - Android Client
 *
 * SPDX-FileCopyrightText: 2026 Nextcloud GmbH and Nextcloud contributors
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package it.niedermann.owncloud.notes

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import it.niedermann.owncloud.notes.main.MainActivity

class UnlockActivity : LockedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        this.installSplashScreen()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        if (!NotesApplication.isLocked()) {
            openMainActivity()
        }
    }

    override fun applyBrand(color: Int) = Unit

    private fun openMainActivity() {
        if (isFinishing) {
            return
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
