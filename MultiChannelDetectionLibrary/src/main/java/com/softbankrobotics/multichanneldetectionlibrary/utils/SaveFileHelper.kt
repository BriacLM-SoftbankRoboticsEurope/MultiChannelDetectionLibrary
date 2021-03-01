package com.softbankrobotics.multichanneldetectionlibrary.utils

import android.util.Log
import com.aldebaran.qi.sdk.`object`.streamablebuffer.StreamableBuffer
import com.aldebaran.qi.sdk.`object`.streamablebuffer.StreamableBufferFactory.fromFunction
import com.aldebaran.qi.sdk.util.copyToStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer

class SaveFileHelper {

    companion object {

        private const val TAG = "MSI_SaveFileHelper"
    }
        /**
         * Save the map data as StreamableBuffer in file
         *
         * @param filesDirectoryPath The directory where to save the map
         * @param fileName The name of file in which save the map
         * @param data The map to save
         */
        fun writeStreamableBufferToFile(
            filesDirectoryPath: String?,
            fileName: String?,
            data: StreamableBuffer
        ) {
            var fos: FileOutputStream? = null
            try {
                Log.d(TAG, "writeMapDataToFile: started")
                val fileDirectory = File(filesDirectoryPath, "")
                if (!fileDirectory.exists()) {
                    if (!fileDirectory.mkdirs()) return
                }
                val file = File(fileDirectory, fileName!!)
                fos = FileOutputStream(file)

                //fos = applicationContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                data.copyToStream(fos)
            } catch (e: IOException) {
                Log.d("Exception", "File write failed: " + e.message, e)
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            Log.d(TAG, "writeMapDataToFile:  Finished")
        }


        /**
         * Get the map data as StreamableBuffer from file
         *
         * @param filesDirectoryPath The directory from which load the map
         * @param fileName The name of file from which load the map
         * @return A StreamableBuffer of map data
         */
        fun readStreamableBufferFromFile(
            filesDirectoryPath: String?,
            fileName: String?
        ): StreamableBuffer? {
            val data: StreamableBuffer
            val f: File
            try {
                f = File(filesDirectoryPath, fileName!!)
                Log.d(TAG, "readStreamableBufferFromFile: f.length : " + f.length())
                if (f.length() == 0L) return null
                data = fromFile(f)
                return data
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Build the map data from file as StreamableBuffer
         *
         * @param file The name of file from which load the map
         * @return A StreamableBuffer of map data
         */
        private fun fromFile(file: File): StreamableBuffer {
            return fromFunction(file.length()) { offset: Long?, size: Long ->
                try {
                    RandomAccessFile(file, "r").use { randomAccessFile ->
                        val byteArray = ByteArray(size.toInt())
                        randomAccessFile.seek(offset!!)
                        randomAccessFile.read(byteArray)
                        return@fromFunction ByteBuffer.wrap(byteArray)
                    }
                } catch (e: IOException) {
                    return@fromFunction ByteBuffer.wrap(ByteArray(0))
                }
            }
        }


}