package com.example.audio

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-quality procedural audio synthesizer for creating crystal-clear musical
 * ringtones, notification chimes, and ambient alarms without external network dependencies.
 */
object AudioSynthEngine {

    enum class ToneType {
        CYBER_PULSE,      // Neon 80s synth bass & arpeggio
        ZEN_BELL,         // Meditative singing bowl with harmonic shimmer
        COSMIC_ORBIT,     // Ethereal space chime & ambient pad
        MARIMBA_SUNRISE,  // Warm acoustic wooden marimba melody
        RETRO_8BIT,       // Classic chiptune arpeggio
        NEON_RING,        // High-energy electronic modern phone ring
        CRYSTAL_ALERT,    // Crisp glassy notification drop
        LOFI_CHIME,       // Soft Rhodes-like mellow bell
        HYPERDRIVE_ALARM, // Futuristic alert sweep
        GENTLE_ACOUSTIC   // Warm acoustic nylon plucked harp
    }

    private const val SAMPLE_RATE = 44100

    /**
     * Generates or retrieves the cached WAV file for a given ToneType.
     */
    fun getOrGenerateWavFile(context: Context, toneType: ToneType): File {
        val toneDir = File(context.cacheDir, "ringtones").apply { mkdirs() }
        val targetFile = File(toneDir, "${toneType.name.lowercase()}.wav")
        if (targetFile.exists() && targetFile.length() > 1000) {
            return targetFile
        }

        val pcmData = generatePcm(toneType)
        val wavBytes = wrapWavHeader(pcmData, SAMPLE_RATE, 1)

        FileOutputStream(targetFile).use { fos ->
            fos.write(wavBytes)
            fos.flush()
        }
        return targetFile
    }

    private fun generatePcm(toneType: ToneType): ShortArray {
        return when (toneType) {
            ToneType.CYBER_PULSE -> generateCyberPulse()
            ToneType.ZEN_BELL -> generateZenBell()
            ToneType.COSMIC_ORBIT -> generateCosmicOrbit()
            ToneType.MARIMBA_SUNRISE -> generateMarimba()
            ToneType.RETRO_8BIT -> generateRetro8Bit()
            ToneType.NEON_RING -> generateNeonRing()
            ToneType.CRYSTAL_ALERT -> generateCrystalAlert()
            ToneType.LOFI_CHIME -> generateLofiChime()
            ToneType.HYPERDRIVE_ALARM -> generateHyperdriveAlarm()
            ToneType.GENTLE_ACOUSTIC -> generateGentleAcoustic()
        }
    }

    private fun generateCyberPulse(): ShortArray {
        val durationSec = 7.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        // Bass synth notes (A minor pentatonic: A2=110, C3=130.8, D3=146.8, E3=164.8, G3=196)
        val bassNotes = doubleArrayOf(110.0, 110.0, 130.81, 146.83, 110.0, 164.81, 146.83, 130.81)
        val stepSec = 0.22
        val stepSamples = (SAMPLE_RATE * stepSec).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val stepIndex = (i / stepSamples) % bassNotes.size
            val freq = bassNotes[stepIndex]
            val localT = (i % stepSamples).toDouble() / SAMPLE_RATE

            // Sawtooth-like harmonic combo + sub-bass
            val sub = sin(2.0 * PI * (freq / 2) * t) * 0.4
            val fundamental = sin(2.0 * PI * freq * t) * 0.35
            val harmonic2 = sin(2.0 * PI * freq * 2.0 * t) * 0.18
            val harmonic3 = sin(2.0 * PI * freq * 3.0 * t) * 0.08

            // Pluck envelope per step
            val env = exp(-localT * 8.0)
            // Lead arp overlay on higher octaves
            val leadFreq = freq * 4.0
            val lead = sin(2.0 * PI * leadFreq * t) * 0.15 * exp(-((i % (stepSamples / 2)).toDouble() / SAMPLE_RATE) * 12.0)

            val mixed = (sub + (fundamental + harmonic2 + harmonic3) * env + lead) * 0.8
            buffer[i] = (mixed.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateZenBell(): ShortArray {
        val durationSec = 6.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        // Fundamental 432 Hz with peaceful singing bowl harmonics
        val f0 = 432.0
        val harmonics = doubleArrayOf(1.0, 2.76, 5.4, 8.9)
        val weights = doubleArrayOf(0.55, 0.25, 0.12, 0.08)
        val decays = doubleArrayOf(0.7, 1.2, 2.0, 3.5)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            for (h in harmonics.indices) {
                val freq = f0 * harmonics[h]
                // Beating effect
                val beat = 1.0 + 0.08 * sin(2.0 * PI * 1.5 * t)
                val decay = exp(-t * decays[h])
                sample += sin(2.0 * PI * freq * t) * weights[h] * decay * beat
            }

            // Secondary softer strike at 2.8s
            if (t >= 2.8) {
                val t2 = t - 2.8
                val fSecond = 540.0
                sample += sin(2.0 * PI * fSecond * t2) * 0.35 * exp(-t2 * 1.0)
            }

            buffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateCosmicOrbit(): ShortArray {
        val durationSec = 7.5
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 880.0, 783.99, 659.25) // C5, E5, G5, C6, A5, G5, E5
        val noteDur = 0.95
        val noteSamples = (SAMPLE_RATE * noteDur).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val currentNoteIdx = ((i / noteSamples) % notes.size)
            val noteFreq = notes[currentNoteIdx]
            val localT = (i % noteSamples).toDouble() / SAMPLE_RATE

            // Bell chime note
            val chimeEnv = exp(-localT * 3.2)
            val chime = (sin(2.0 * PI * noteFreq * t) + 0.3 * sin(2.0 * PI * noteFreq * 2.0 * t)) * chimeEnv * 0.45

            // Deep space drone pad (C3 + G3)
            val pad = (sin(2.0 * PI * 130.81 * t) * 0.2 + sin(2.0 * PI * 196.0 * t) * 0.15) *
                    (0.8 + 0.2 * sin(2.0 * PI * 0.3 * t))

            val mixed = chime + pad
            buffer[i] = (mixed.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateMarimba(): ShortArray {
        val durationSec = 6.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        // Uplifting major melody: C4, G4, E4, A4, G4, C5, B4, G4
        val melody = doubleArrayOf(261.63, 392.00, 329.63, 440.00, 392.00, 523.25, 493.88, 392.00)
        val noteDur = 0.32
        val noteSamples = (SAMPLE_RATE * noteDur).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (i / noteSamples) % melody.size
            val freq = melody[noteIdx]
            val localT = (i % noteSamples).toDouble() / SAMPLE_RATE

            // Marimba has strong fundamental + warm 4th harmonic resonant body
            val env = exp(-localT * 9.0)
            val tone = sin(2.0 * PI * freq * t) * 0.6 +
                    sin(2.0 * PI * freq * 3.9 * t) * 0.2 * exp(-localT * 18.0)

            // Warmth
            val warm = tone * env * 0.75
            buffer[i] = (warm.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateRetro8Bit(): ShortArray {
        val durationSec = 5.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        val notes = doubleArrayOf(330.0, 392.0, 494.0, 659.0, 784.0, 988.0, 1318.0, 988.0)
        val noteDur = 0.12
        val noteSamples = (SAMPLE_RATE * noteDur).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (i / noteSamples) % notes.size
            val freq = notes[noteIdx]
            val localT = (i % noteSamples).toDouble() / SAMPLE_RATE

            // Square wave (classic NES / Gameboy)
            val phase = (t * freq) % 1.0
            val square = if (phase < 0.5) 0.35 else -0.35
            val env = (1.0 - (localT / noteDur) * 0.3).coerceAtLeast(0.1)

            buffer[i] = ((square * env).coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateNeonRing(): ShortArray {
        val durationSec = 6.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            // Pulse cadence: 1.5s ring, 1.0s silence
            val cycleT = t % 2.5
            var sample = 0.0

            if (cycleT < 1.4) {
                // Dual electronic telephone frequencies (750 Hz + 950 Hz) modulated with 25 Hz tremolo
                val tremolo = 0.5 + 0.5 * sin(2.0 * PI * 22.0 * t)
                val tone1 = sin(2.0 * PI * 750.0 * t) * 0.35
                val tone2 = sin(2.0 * PI * 950.0 * t) * 0.35
                val harmony = sin(2.0 * PI * 1500.0 * t) * 0.1
                sample = (tone1 + tone2 + harmony) * tremolo
            }

            buffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateCrystalAlert(): ShortArray {
        val durationSec = 1.6
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        // Dual chime ascending: E6 (1318 Hz) then B6 (1975 Hz)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0

            // Strike 1
            if (t < 0.8) {
                val env1 = exp(-t * 9.0)
                sample += (sin(2.0 * PI * 1318.5 * t) + 0.3 * sin(2.0 * PI * 2637.0 * t)) * env1 * 0.5
            }
            // Strike 2
            if (t >= 0.15) {
                val t2 = t - 0.15
                val env2 = exp(-t2 * 7.0)
                sample += (sin(2.0 * PI * 1975.5 * t2) + 0.25 * sin(2.0 * PI * 3951.0 * t2)) * env2 * 0.65
            }

            buffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateLofiChime(): ShortArray {
        val durationSec = 4.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        val chords = doubleArrayOf(440.0, 554.37, 659.25, 830.61) // A major 7th chime
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            var sample = 0.0
            val env = exp(-t * 1.5)
            // Subtle vinyl crackle / flutter
            val flutter = 1.0 + 0.015 * sin(2.0 * PI * 5.0 * t)

            for (f in chords) {
                sample += sin(2.0 * PI * f * flutter * t) * 0.18
            }
            sample *= env

            buffer[i] = (sample.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateHyperdriveAlarm(): ShortArray {
        val durationSec = 3.5
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val cycle = (t % 0.6) / 0.6
            // Frequency sweep from 600Hz up to 1400Hz
            val freq = 600.0 + (cycle * 800.0)
            val tone = sin(2.0 * PI * freq * t) * 0.5
            val sub = sin(2.0 * PI * (freq / 2.0) * t) * 0.2
            val env = if (cycle < 0.8) 1.0 else (1.0 - (cycle - 0.8) / 0.2)
            buffer[i] = (((tone + sub) * env).coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun generateGentleAcoustic(): ShortArray {
        val durationSec = 5.0
        val totalSamples = (SAMPLE_RATE * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        // Plucked arpeggio (D major: D4=293.66, F#4=369.99, A4=440.0, D5=587.33)
        val notes = doubleArrayOf(293.66, 369.99, 440.00, 587.33, 440.00, 369.99)
        val stepSec = 0.45
        val stepSamples = (SAMPLE_RATE * stepSec).toInt()

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / SAMPLE_RATE
            val noteIdx = (i / stepSamples) % notes.size
            val freq = notes[noteIdx]
            val localT = (i % stepSamples).toDouble() / SAMPLE_RATE

            val env = exp(-localT * 4.5)
            val tone = sin(2.0 * PI * freq * t) * 0.45 +
                    sin(2.0 * PI * freq * 2.0 * t) * 0.2 * exp(-localT * 6.0) +
                    sin(2.0 * PI * freq * 3.0 * t) * 0.08 * exp(-localT * 8.0)

            buffer[i] = ((tone * env).coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
        }
        return buffer
    }

    private fun wrapWavHeader(pcmData: ShortArray, sampleRate: Int, channels: Int): ByteArray {
        val byteDataLength = pcmData.size * 2
        val totalDataLen = byteDataLength + 36
        val byteRate = sampleRate * channels * 2

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(totalDataLen)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size for PCM
        header.putShort(1.toShort()) // AudioFormat 1 = PCM
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort((channels * 2).toShort()) // block align
        header.putShort(16.toShort()) // bits per sample
        header.put("data".toByteArray())
        header.putInt(byteDataLength)

        val out = ByteArrayOutputStream(44 + byteDataLength)
        out.write(header.array())

        val pcmByteBuffer = ByteBuffer.allocate(byteDataLength).order(ByteOrder.LITTLE_ENDIAN)
        for (sample in pcmData) {
            pcmByteBuffer.putShort(sample)
        }
        out.write(pcmByteBuffer.array())

        return out.toByteArray()
    }
}
