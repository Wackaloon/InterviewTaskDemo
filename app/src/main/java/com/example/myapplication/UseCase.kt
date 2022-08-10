package com.example.myapplication

import android.content.Context
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.*
import java.util.*

//todo think of an appropriate name
open class UseCase (var sdr: WebSDRUSeCase, var yandexTranslateUseCase: YandexTranslateUseCase, var context: Context) {
    var lastMax = 1f
    var avg = 0f
    @Synchronized
    suspend open fun invoke(): Signal {
        return sdr.getMorningData(846000f).run {
            var outputSignal = Signal()
            var realNumbers: List<Float> = this.map { it.first }
            var imaginaryNumbers: List<Float> = this.map { it.second }
            var resultSignal = LinkedList(realNumbers)
            for (i in 0 until this.size) {
                lastMax *= 0.95f
                resultSignal[i] = realNumbers[i] * realNumbers[i] + imaginaryNumbers[i] * imaginaryNumbers[i]
                avg += resultSignal[i]
                if (resultSignal[i] > lastMax) lastMax = resultSignal[i]
            }
            avg = avg / this.size
            val gain: Float = 0.75f / lastMax
            val signal = resultSignal.asFlow()
                .map {
                    flow {
                        emit((it - avg) * gain)
                    }
                }
                .flattenConcat()
                .toList()
            outputSignal.sampleRate = QUADRATURE_RATE
            outputSignal.signal = signal
            return outputSignal.apply {
                var amplified = LinkedList(this.signal)
                for (i in 0 until this.signal.size) {
                    amplified[i] = this.signal[i]*10_000
                }
                this.signal = amplified
                yandexTranslateUseCase.translate(this, Locale.CHINESE, Locale(context.getString(R.string.target_locale_small),context.getString(R.string.target_locale_big)))
            }
        }
    }
    companion object {
        var QUADRATURE_RATE =  2* Const.MIDI_RATE // AM
    }
}

object Const {
    var MIDI_RATE = 31250f
}

class Signal {
    lateinit var signal: List<Float>
    var sampleRate: Float = 0f
}

interface WebSDRUSeCase {
    @WorkerThread // do not call from different threads at the same time!
    fun getMorningData(f: Float): LinkedList<Pair<Float, Float>>
}

interface YandexTranslateUseCase {
    @WorkerThread // do not call from different threads at the same time!
    fun translate(signal: Signal, from: Locale, to: Locale): Signal
}
