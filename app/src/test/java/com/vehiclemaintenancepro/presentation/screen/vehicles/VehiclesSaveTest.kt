package com.vehiclemaintenancepro.presentation.screen.vehicles

import app.cash.turbine.test
import com.vehiclemaintenancepro.MainDispatcherRule
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class VehiclesSaveTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test
    fun `save waits for success and ignores a second submit`() = runTest {
        val repository = mockk<VehicleRepository>(relaxed = true)
        val request = mockk<VehicleCreateRequest>()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val finished = CompletableDeferred<Unit>()
        coEvery { repository.addVehicle(request) } coAnswers {
            entered.complete(Unit)
            release.await()
            1L
        }
        val model = VehiclesViewModel(repository)
        model.saveState.test {
            assertFalse(awaitItem().isSaving)
            model.addVehicle(request) { finished.complete(Unit) }
            assertTrue(awaitItem().isSaving)
            entered.await()
            model.addVehicle(request) { fail("Second submit must not complete") }
            assertFalse(finished.isCompleted)
            release.complete(Unit)
            assertFalse(awaitItem().isSaving)
            finished.await()
            coVerify(exactly = 1) { repository.addVehicle(request) }
        }
    }

    @Test
    fun `failed save exposes an error and does not close the form`() = runTest {
        val repository = mockk<VehicleRepository>(relaxed = true)
        val request = mockk<VehicleCreateRequest>()
        coEvery { repository.addVehicle(request) } throws IllegalStateException("disk failure")
        val model = VehiclesViewModel(repository)
        model.saveState.test {
            awaitItem()
            model.addVehicle(request) { fail("A failed save must keep the form open") }
            assertTrue(awaitItem().isSaving)
            val error = awaitItem()
            assertFalse(error.isSaving)
            assertNotNull(error.errorMessage)
        }
    }
}
