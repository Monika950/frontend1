package com.example.treasurehuntapp.features.login

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.treasurehuntapp.R
import com.example.treasurehuntapp.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: LoginViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var application: Application

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock dependencies
        authRepository = mock()
        application = mock()
        
        // Mock string resources
        whenever(application.getString(R.string.error_email_password_required))
            .thenReturn("Email and password are required.")
        whenever(application.getString(R.string.error_login_failed))
            .thenReturn("Login failed.")
        
        // Create ViewModel
        viewModel = LoginViewModel(authRepository, application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be empty`() {
        // Then
        val state = viewModel.state.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `onEmailChange should update email and clear error`() {
        // Given
        val email = "test@example.com"
        
        // When
        viewModel.onEmailChange(email)
        
        // Then
        assertEquals(email, viewModel.state.value.email)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `onPasswordChange should update password and clear error`() {
        // Given
        val password = "password123"
        
        // When
        viewModel.onPasswordChange(password)
        
        // Then
        assertEquals(password, viewModel.state.value.password)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login with empty email should show error`() = runTest {
        // Given
        viewModel.onPasswordChange("password123")
        
        // When
        var successCalled = false
        viewModel.login(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(successCalled)
        assertEquals("Email and password are required.", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with empty password should show error`() = runTest {
        // Given
        viewModel.onEmailChange("test@example.com")
        
        // When
        var successCalled = false
        viewModel.login(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(successCalled)
        assertEquals("Email and password are required.", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with blank email should show error`() = runTest {
        // Given
        viewModel.onEmailChange("   ")
        viewModel.onPasswordChange("password123")
        
        // When
        var successCalled = false
        viewModel.login(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(successCalled)
        assertEquals("Email and password are required.", viewModel.state.value.error)
    }

    @Test
    fun `login with valid credentials should succeed`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        // Mock successful login
        whenever(authRepository.login(email.trim(), password)).thenReturn(Unit)
        
        // When
        var successCalled = false
        viewModel.login(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertTrue(successCalled)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        verify(authRepository).login(email, password)
    }

    @Test
    fun `login should trim email whitespace`() = runTest {
        // Given
        val email = "  test@example.com  "
        val password = "password123"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        // Mock successful login
        whenever(authRepository.login(email.trim(), password)).thenReturn(Unit)
        
        // When
        viewModel.login(onSuccess = {})
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        verify(authRepository).login("test@example.com", password)
    }

    @Test
    fun `login with repository exception should show error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val errorMessage = "Network error"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        // Mock failed login
        whenever(authRepository.login(email, password))
            .thenThrow(RuntimeException(errorMessage))
        
        // When
        var successCalled = false
        viewModel.login(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(successCalled)
        assertEquals(errorMessage, viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login with exception without message should show default error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        // Mock failed login with no message
        whenever(authRepository.login(email, password))
            .thenThrow(RuntimeException())
        
        // When
        var successCalled = false
        viewModel.login(onSuccess = { successCalled = true })
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assertFalse(successCalled)
        assertEquals("Login failed.", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `login should set loading state during execution`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        viewModel.onEmailChange(email)
        viewModel.onPasswordChange(password)
        
        // Mock successful login
        whenever(authRepository.login(email, password)).thenReturn(Unit)
        
        // When
        viewModel.login(onSuccess = {})
        
        // Then - loading should be true initially
        assertTrue(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
        
        // Advance to completion
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - loading should be false after completion
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `changing email after error should clear error`() {
        // Given - set an error state
        viewModel.login(onSuccess = {})
        assertNotNull(viewModel.state.value.error)
        
        // When
        viewModel.onEmailChange("new@example.com")
        
        // Then
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `changing password after error should clear error`() {
        // Given - set an error state
        viewModel.login(onSuccess = {})
        assertNotNull(viewModel.state.value.error)
        
        // When
        viewModel.onPasswordChange("newpassword")
        
        // Then
        assertNull(viewModel.state.value.error)
    }
}