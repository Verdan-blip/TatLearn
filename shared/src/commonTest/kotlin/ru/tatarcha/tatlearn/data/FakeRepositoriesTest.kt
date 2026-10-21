package ru.tatarcha.tatlearn.data

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import ru.tatarcha.tatlearn.core.common.ErrorCodes
import ru.tatarcha.tatlearn.core.common.errorCode
import ru.tatarcha.tatlearn.data.fake.FakeAuthRepository
import ru.tatarcha.tatlearn.data.fake.FakeBackendConfig
import ru.tatarcha.tatlearn.data.local.SessionStorage
import ru.tatarcha.tatlearn.domain.model.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeRepositoriesTest {
    @Test fun sessionStorageRoundTripsAndClears() {
        val settings = MapSettings()
        SessionStorage(settings).save(UserSession("Алия", "a@b.ru"))
        val restored = SessionStorage(settings)
        assertEquals(UserSession("Алия", "a@b.ru"), restored.session.value)
        restored.clear()
        assertNull(restored.session.value)
        assertNull(SessionStorage(settings).session.value)
    }

    @Test fun signInDerivesNameFromEmailAndPersists() = runTest {
        val storage = SessionStorage(MapSettings())
        val auth = FakeAuthRepository(FakeBackendConfig(), storage)
        val session = auth.signIn("aliya@mail.ru", "secret1").getOrThrow()
        assertEquals("Aliya", session.name)
        assertEquals(session, auth.session.value)
        auth.signOut()
        assertNull(auth.session.value)
    }

    @Test fun simulatedErrorsFailRequestsWithTimeoutCode() = runTest {
        val config = FakeBackendConfig().apply { setSimulateErrors(true) }
        val storage = SessionStorage(MapSettings())
        val result = FakeAuthRepository(config, storage).signIn("a@b.ru", "secret1")
        assertTrue(result.isFailure)
        assertEquals(ErrorCodes.NET_TIMEOUT, result.exceptionOrNull()!!.errorCode())
        assertNull(storage.session.value)
        config.setSimulateErrors(false)
        assertNotNull(FakeAuthRepository(config, storage).signUp("a@b.ru", "secret1").getOrNull())
    }
}
