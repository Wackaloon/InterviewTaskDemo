package com.example.myapplication

import kotlinx.coroutines.test.runTest
import org.junit.Test

import org.junit.Assert.*
import java.util.*

class ExampleUnitTest {
    val useCase = UseCase(WebSDRUSeCaseTestImpl(), YandexTranslateUseCaseTestImpl())

    @Test
    fun sdrWorks() = runTest {
        val result = useCase.invoke()
        assertArrayEquals(expected, result.signal.toFloatArray(), 0.00000001f)
    }

    companion object {
        val expected = arrayListOf(
            4.737946, 4.7358956, 4.727019, 4.70315, 4.652905, 4.5617304, 4.4119635, 4.1829076, 3.8509192, 3.38951, 2.769456, 1.9589273, 0.9236189, -0.37309635, -1.9700112, -3.9079204, -6.2294335, -8.978794, -12.201683, -15.945012
        )
            .map {
                it.toFloat()
            }.toFloatArray()
    }

}

class WebSDRUSeCaseTestImpl : WebSDRUSeCase {
    override fun getMorningData(f: Float): LinkedList<Pair<Float, Float>> {
        if (f == 846000f) {
            var result = LinkedList<Pair<Float, Float>>()
            val amplitude = 1
            val amplitudeModulated = 1
            val fc = 100f
            val fcmodulated = 20f
            var car_phase = 0f
            var mod_phase = 0f
            for (i in 0 until 20) {
                if (car_phase >= 2*Math.PI)
                    car_phase -= 2*Math.PI.toFloat()
                if (mod_phase >= 2*Math.PI)
                    mod_phase -= 2*Math.PI.toFloat()
                car_phase += fc/31250f
                mod_phase += fcmodulated/31250f
                val sinus_carrier = Math.sin(2 * Math.PI * (car_phase))
                val sinus_am = Math.sin(2 * Math.PI * (mod_phase))
                val real = (sinus_am * sinus_carrier).toFloat()
                val img = 0f
                result.add(real to img)
            }
            return result
        }
        throw IllegalArgumentException("Wrong freq")
    }
}

class YandexTranslateUseCaseTestImpl : YandexTranslateUseCase {
    override fun translate(signal: Signal, from: Locale, to: Locale): Signal {
        val newSignal = Signal()
        newSignal.signal = signal.signal.map { -it }
        newSignal.sampleRate = signal.sampleRate
        return newSignal
    }

}
