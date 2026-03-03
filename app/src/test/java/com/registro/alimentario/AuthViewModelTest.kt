package com.registro.alimentario

import app.cash.turbine.test
import com.registro.alimentario.model.User
import com.registro.alimentario.model.UserRole
import com.registro.alimentario.repository.AuthRepository
import com.registro.alimentario.viewmodel.AuthUiState
import com.registro.alimentario.viewmodel.AuthViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        every { authRepository.currentUser } returns flowOf(null)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with empty fields shows validation error`() = runTest {
        viewModel.login("", "")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    @Test
    fun `login success updates state`() = runTest {
        val user = User(uid = "123", email = "test@test.com", role = UserRole.PACIENTE)
        coEvery { authRepository.login(any(), any()) } returns Result.success(user)
        coEvery { authRepository.getCurrentUserRole() } returns UserRole.PACIENTE

        viewModel.uiState.test {
            assertEquals(AuthUiState.Idle, awaitItem())
            viewModel.login("test@test.com", "password123")
            assertEquals(AuthUiState.Loading, awaitItem())
            val success = awaitItem()
            assertTrue(success is AuthUiState.Success)
            assertEquals(user, (success as AuthUiState.Success).user)
        }
    }

    @Test
    fun `login failure shows error message`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Auth failed"))

        viewModel.login("test@test.com", "wrongpassword")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    @Test
    fun `register with short password shows validation error`() = runTest {
        viewModel.register("test@test.com", "short", "Test User")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertTrue((state as AuthUiState.Error).message.contains("8"))
    }

    @Test
    fun `register with empty fields shows validation error`() = runTest {
        viewModel.register("", "", "")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
    }

    @Test
    fun `logout resets state`() = runTest {
        coEvery { authRepository.logout() } returns Unit

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthUiState.Idle, viewModel.uiState.value)
    }
}
