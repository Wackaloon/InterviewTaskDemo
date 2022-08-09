package com.example.myapplication

class UseCaseWithAnalytics(var analytics: Analytics, sdr: WebSDRUSeCase, yandexTranslateUseCase: YandexTranslateUseCase) : UseCase(sdr, yandexTranslateUseCase) {

    override suspend fun invoke(): Signal {
        analytics.logLogicBeingCalled()
        return super.invoke()
    }
}

interface Analytics {
    fun logLogicBeingCalled()
}
