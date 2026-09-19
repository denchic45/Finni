package com.hackathon.finni.core.ui.overlay

import androidx.lifecycle.ViewModel
import com.hackathon.finni.core.ui.navigation.router.LatestPhotoIndex
import com.hackathon.finni.core.ui.navigation.router.Router

class OverlayImagesViewModel(
    private val router: Router
) : ViewModel() {

    fun onDismiss(currentImageIndex: Int) {
        router.popWithResult(LatestPhotoIndex(currentImageIndex))
    }
}