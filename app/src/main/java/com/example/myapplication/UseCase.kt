package com.example.myapplication

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.*
import java.util.*

//todo think of an appropriate name
open class UseCase (var sdr: WebSDRUSeCase, var yandexTranslateUseCase: YandexTranslateUseCase) {
    var lastMax = 1f
    var avg = 0f
    @Synchronized
    suspend open fun invoke(): Signal {
        return sdr.getMorningData(846000f).run {
            val outputSignal = Signal()
            val realNumbers: List<Float> = this.map { it.first }
            val imaginaryNumbers: List<Float> = this.map { it.second }
            val resultSignal: MutableList<Float> = LinkedList<Float>(realNumbers)
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
            outputSignal.sampleRate = Const.QUADRATURE_RATE
            outputSignal.signal = signal
            return outputSignal.apply {
                val amplified = ArrayList(this.signal)
                for (i in 0 until this.signal.size) {
                    amplified[i] = this.signal[i]*10_000
                }
                this.signal = amplified
                yandexTranslateUseCase.translate(this, Locale.CHINESE, Locale("ru","RU"))
            }
        }
    }
}

object Const {
    var MIDI_RATE = 31250f
    var QUADRATURE_RATE =  2*MIDI_RATE // AM
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
