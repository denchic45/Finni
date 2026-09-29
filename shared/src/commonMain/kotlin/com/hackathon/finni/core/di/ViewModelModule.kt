package com.hackathon.finni.core.di

import com.hackathon.finni.core.presentation.MainViewModel
import com.hackathon.finni.core.ui.overlay.OverlayImagesViewModel
import com.hackathon.finni.features.levels.LevelsViewModel
import com.hackathon.finni.features.main.MainScreenViewModel
import com.hackathon.finni.features.tasks.TaskRunnerViewModel
import com.hackathon.finni.features.tetris.TetrisViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::TaskRunnerViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::OverlayImagesViewModel)
    viewModelOf(::MainScreenViewModel)
    viewModelOf(::LevelsViewModel)
    viewModelOf(::TetrisViewModel)
}
