package com.registro.alimentario

import com.registro.alimentario.model.TipoComida
import com.registro.alimentario.repository.PhotoRepository
import com.registro.alimentario.repository.RegistroRepository
import com.registro.alimentario.viewmodel.RegistroViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegistroViewModelTest {

    private lateinit var registroRepository: RegistroRepository
    private lateinit var photoRepository: PhotoRepository
    private lateinit var viewModel: RegistroViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        registroRepository = mockk(relaxed = true)
        photoRepository = mockk(relaxed = true)
        viewModel = RegistroViewModel(registroRepository, photoRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `save without meal type shows validation error`() = runTest {
        viewModel.updateField { copy(descripcion = "algo que comí") } // no tipoComida
        viewModel.save("patient123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `save without description shows validation error`() = runTest {
        viewModel.updateField { copy(tipoComida = TipoComida.ALMUERZO) } // no descripcion
        viewModel.save("patient123")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.formState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `updateField updates form state correctly`() {
        viewModel.updateField { copy(tipoComida = TipoComida.DESAYUNO, descripcion = "Tostadas") }

        val state = viewModel.formState.value
        assertTrue(state.tipoComida == TipoComida.DESAYUNO)
        assertTrue(state.descripcion == "Tostadas")
    }

    @Test
    fun `addPhotoUri enforces max 5 photos`() {
        repeat(5) {
            viewModel.updateField {
                val uri = android.net.Uri.parse("file:///fake/photo$it.jpg")
                copy(fotosUris = fotosUris + uri)
            }
        }

        val uriToAdd = android.net.Uri.parse("file:///fake/photo6.jpg")
        viewModel.addPhotoUri(uriToAdd)

        val state = viewModel.formState.value
        assertTrue(state.fotosUris.size + state.fotosUrls.size <= 5)
    }

    @Test
    fun `resetForm clears all form state`() {
        viewModel.updateField {
            copy(tipoComida = TipoComida.CENA, descripcion = "Pasta")
        }
        viewModel.resetForm()

        val state = viewModel.formState.value
        assertTrue(state.tipoComida == null)
        assertTrue(state.descripcion.isBlank())
    }

    @Test
    fun `atracon conditional field visible only when SI`() {
        viewModel.updateField { copy(fueAtracon = com.registro.alimentario.model.FueAtracon.SI) }
        assertTrue(viewModel.formState.value.fueAtracon == com.registro.alimentario.model.FueAtracon.SI)

        viewModel.updateField { copy(fueAtracon = com.registro.alimentario.model.FueAtracon.NO) }
        assertTrue(viewModel.formState.value.fueAtracon == com.registro.alimentario.model.FueAtracon.NO)
    }
}
