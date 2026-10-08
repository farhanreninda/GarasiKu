package com.vehiclemaintenancepro.data.local

import androidx.test.platform.app.InstrumentationRegistry
import com.vehiclemaintenancepro.data.repository.SettingsRepositoryImpl
import com.vehiclemaintenancepro.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeSettingsTest {
    @Test fun themePersistsAcrossRepositoryInstancesWithoutChangingProfile() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = SettingsRepositoryImpl(context)
        val original = repository.observeSettings().first()
        try {
            for (mode in ThemeMode.entries) {
                repository.updateThemeMode(mode)
                val restored = SettingsRepositoryImpl(context).observeSettings().first()
                assertEquals(mode, restored.themeMode)
                assertEquals(original.userName, restored.userName)
            }
        } finally {
            repository.updateThemeMode(original.themeMode)
        }
    }
}
