package com.example.myapplication

import android.content.Context

class UseCaseWithAnalytics(var analytics: Analytics, sdr: WebSDRUSeCase, yandexTranslateUseCase: YandexTranslateUseCase, context: Context) : UseCase(sdr, yandexTranslateUseCase, context) {

    override suspend fun invoke(): Signal {
        analytics.logLogicBeingCalled()
        return super.invoke()
    }
}

interface Analytics {
    fun logLogicBeingCalled()
}
