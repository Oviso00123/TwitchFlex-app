package com.example.p2p

import java.io.DataInputStream
import java.io.DataOutputStream

enum class PacketType(val code: Byte) {
    VIDEO_FRAME(1),
    JSON_EVENT(2)
}

object P2PStreamProtocol {

    fun sendFrame(output: DataOutputStream, type: PacketType, data: ByteArray) {
        synchronized(output) {
            output.writeByte(type.code.toInt())
            output.writeInt(data.size)
            output.write(data)
            output.flush()
        }
    }

    fun readPacket(input: DataInputStream, onPacket: (PacketType, ByteArray) -> Unit) {
        val typeByte = input.readByte()
        val length = input.readInt()
        if (length in 1..20_000_000) {
            val buffer = ByteArray(length)
            input.readFully(buffer)
            val type = when (typeByte.toInt()) {
                1 -> PacketType.VIDEO_FRAME
                2 -> PacketType.JSON_EVENT
                else -> null
            }
            type?.let { onPacket(it, buffer) }
        }
    }
}
