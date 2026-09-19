package com.hackathon.finni.core.di

import com.hackathon.finni.core.presentation.MainViewModel
import com.hackathon.finni.core.ui.overlay.OverlayImagesViewModel
import com.hackathon.finni.features.auth.AuthViewModel
import com.hackathon.finni.features.auth.RegisterViewModel
import com.hackathon.finni.features.home.HomeViewModel
import com.hackathon.finni.features.noteeditor.NoteEditorViewModel
import com.hackathon.finni.features.projects.ContextPickerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::RegisterViewModel)
    viewModel {
        NoteEditorViewModel(
            it.getOrNull(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModelOf(::HomeViewModel)
    viewModelOf(::ContextPickerViewModel)
    viewModelOf(::OverlayImagesViewModel)
}