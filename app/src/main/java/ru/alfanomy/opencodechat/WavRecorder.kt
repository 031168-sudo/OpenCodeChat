package ru.alfanomy.opencodechat

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Запись голоса в WAV 16 кГц / моно / 16 бит — в формате, который
 * принимает whisper-server.
 */
class WavRecorder(private val context: Context) {
    companion object {
        const val SAMPLE_RATE = 16000
        const val MAX_SECONDS = 120
    }

    private var record: AudioRecord? = null
    private var worker: Thread? = null
    private var data: ByteArrayOutputStream? = null
    @Volatile private var running = false
    @Volatile private var failed = false

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun start(): Boolean {
        if (!hasPermission() || running) return false
        val min = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (min <= 0) return false
        val bufferSize = maxOf(min * 2, 4096)
        val rec = runCatching {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
        }.getOrNull() ?: return false
        if (rec.state != AudioRecord.STATE_INITIALIZED) {
            rec.release()
            return false
        }
        val ok = runCatching { rec.startRecording() }.isSuccess
        if (!ok) {
            rec.release()
            return false
        }
        record = rec
        data = ByteArrayOutputStream()
        failed = false
        running = true
        worker = Thread {
            val buf = ByteArray(bufferSize)
            val maxBytes = SAMPLE_RATE * 2 * MAX_SECONDS
            try {
                while (running) {
                    val n = rec.read(buf, 0, buf.size)
                    if (n > 0) {
                        data?.write(buf, 0, n)
                        if ((data?.size() ?: 0) >= maxBytes) {
                            running = false
                            break
                        }
                    } else if (n < 0) {
                        if (running) failed = true
                        running = false
                        break
                    }
                }
            } catch (_: Exception) {
                if (running) failed = true
                running = false
            }
        }.also { it.start() }
        return true
    }

    fun stop(): ByteArray? {
        if (!running && worker == null) return null
        running = false
        val rec = record
        record = null
        runCatching { rec?.stop() }
        val t = worker
        worker = null
        runCatching { t?.join(1000) }
        runCatching { rec?.release() }
        val pcm = data?.toByteArray() ?: return null
        data = null
        if (failed || pcm.size < SAMPLE_RATE / 2) return null
        return wav(pcm)
    }

    fun cancel() {
        running = false
        val rec = record
        record = null
        runCatching { rec?.stop() }
        runCatching { worker?.join(500) }
        worker = null
        runCatching { rec?.release() }
        data = null
    }

    private fun wav(pcm: ByteArray): ByteArray {
        val channels = 1
        val bits = 16
        val byteRate = SAMPLE_RATE * channels * bits / 8
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(36 + pcm.size)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16)
        header.putShort(1.toShort())
        header.putShort(channels.toShort())
        header.putInt(SAMPLE_RATE)
        header.putInt(byteRate)
        header.putShort((channels * bits / 8).toShort())
        header.putShort(bits.toShort())
        header.put("data".toByteArray())
        header.putInt(pcm.size)
        val out = ByteArrayOutputStream(44 + pcm.size)
        out.write(header.array())
        out.write(pcm)
        return out.toByteArray()
    }
}
