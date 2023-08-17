package com.bctags.bcstocks.ui.sgtin

import com.bctags.bcstocks.ui.sgtin.Sgtin.SgtinFilter
import com.bctags.bcstocks.ui.sgtin.Sgtin.getCompanyPrefixBits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getCompanyPrefixDigits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getItemReferenceBits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getItemReferenceDigits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getPartition
import org.epctagcoder.util.Converter
import java.nio.ByteBuffer
import java.util.BitSet

/**
 * The Serialised Global Trade Item Number EPC scheme is used to assign a unique
 * identity to an instance of a trade item, such as a specific instance
 * of a product or SKU.
 */
 class Sgtin96 constructor(
    filter: Int,
    companyPrefixDigits: Int,
    companyPrefix: Long,
    itemReference: Int,
    serial: Long
)  {
    var epc: String? = null
        get() {
            if (field == null) {
                val epc = BitSet(96)
                var i = 0
                run {
                    var j = 0
                    while (j < serialBitSize) {
                        epc[i] = serial shr j and 1L == 1L
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < getItemReferenceBits(partition.toInt())) {
                        epc[i] = itemReference shr j and 1 == 1
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < getCompanyPrefixBits(partition.toInt())) {
                        epc[i] = companyPrefix shr j and 1L == 1L
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < 3) {
                        epc[i] = partition.toInt() shr j and 1 == 1
                        j++
                        i++
                    }
                }
                run {
                    var j = 0
                    while (j < 3) {
                        epc[i] = filter.value shr j and 1 == 1
                        j++
                        i++
                    }
                }
                var j = 0
                while (j < 8) {
                    epc[i] = epcHeader.toInt() shr j and 1 == 1
                    j++
                    i++
                }
                val epcba = epc.toByteArray()
                val sb = StringBuffer(epcba.size * 2)
                i = epcba.size - 1
                while (i >= 0) {
                    sb.append(String.format("%02X", epcba[i]))
                    i--
                }
                field = sb.toString()
            }
            return field
        }
        public set
     val filter: SgtinFilter
     val partition: Byte
    val companyPrefix: Long
    val itemReference: Int
    val serial: Long
    var uri: String? = null
        get() {
            if (field == null) field = uriHeader + filter.value.toString() + "." + String.format(
                "%0" + getCompanyPrefixDigits(partition.toInt()) + "d", companyPrefix
            ) + "." + String.format(
                "%0" + getItemReferenceDigits(partition.toInt()) + "d",
                itemReference
            ) + "." + serial.toString()
            return field
        }
        public set

    init {
        this.filter = SgtinFilter.values()[filter]
        partition = getPartition(companyPrefixDigits).toByte()
        require(!(companyPrefix >= 1L shl getCompanyPrefixBits(partition.toInt()).toInt())) {
            "Company Prefix too large, max value (exclusive):" + (1L shl getCompanyPrefixBits(
                partition.toInt()
            ).toInt())
        }
        this.companyPrefix = companyPrefix
        require(!(itemReference >= 1L shl getItemReferenceBits(partition.toInt()).toInt())) {
            "Item Prefix too large, max value (exclusive):" + (1L shl getItemReferenceBits(
                partition.toInt()
            ).toInt())
        }
        this.itemReference = itemReference
        require(!(serial >= 1L shl serialBitSize.toInt())) { "Serial too big, max value: " + ((1L shl serialBitSize.toInt()) - 1) }
        this.serial = serial
    }

    fun getFilter(): Int {
        return filter.value
    }

    override fun toString(): String {
        return uri!!
    }

    override fun equals(o: Any?): Boolean {
        return if (o !is Sgtin96) false else o.uri == uri
    }

    companion object {
         const val epcHeader: Byte = 48
         const val serialBitSize: Byte = 38
         const val uriHeader = "urn:epc:tag:sgtin-96:"
        fun fromFields(
            filter: Int,
            companyPrefixDigits: Int,
            companyPrefix: Long,
            itemReference: Int,
            serial: Long
        ): Sgtin96 {
            return Sgtin96(filter, companyPrefixDigits, companyPrefix, itemReference, serial)
        }

        fun fromGs1Key(filter: Int, companyPrefixDigits: Int, ai01: String, ai21: Long): Sgtin96 {
            require(!(ai01.length != 14 || !Converter.isNumeric(ai01))) { "GTIN must be 14 digits long" }
            return Sgtin96(
                filter,
                companyPrefixDigits,
                ai01.substring(1, companyPrefixDigits + 1).toLong(),
                ai01.substring(companyPrefixDigits + 1, 14 - 1).toInt(),
                ai21
            )
        }

        fun fromUri(uri: String): Sgtin96 {
            require(uri.startsWith(uriHeader)) { "Decoding error: wrong URI header, expected $uriHeader" }
            val uriParts = uri.substring(uriHeader.length).split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val filter = uriParts[0].toInt()
            val partition = getPartition(uriParts[1].length).toByte()
            val companyPrefix = uriParts[1].toLong()
            val itemReference = uriParts[2].toInt()
            val serial = uriParts[3].toLong()
            val sgtin96 = fromFields(
                filter,
                getCompanyPrefixDigits(partition.toInt()),
                companyPrefix,
                itemReference,
                serial
            )
            sgtin96.uri = uri
            return sgtin96
        }

        fun fromEpc(epc: String): Sgtin96 {
            val a = ArrayList<String>()
            run {
                var i = 0
                while (i < epc.length) {
                    a.add(epc.substring(i, i + 2))
                    i += 2
                }
            }
            val bb = ByteBuffer.allocate(96 / 8)
            for (i in a.indices.reversed()) bb.put(a[i].toInt(16).toByte())
            bb.rewind()
            val bs = BitSet.valueOf(bb)
            var i: Int
            var tmp: Long
            tmp = 0
            i = 96
            while (bs.previousSetBit(i - 1).also { i = it } > 96 - 8 - 1) {
                tmp += 1L shl i - (96 - 8)
            }
            require(tmp == epcHeader.toLong()) { "Invalid header" } //maybe the decoder could choose the structure from the header?
            tmp = 0
            i = 96 - 8
            while (bs.previousSetBit(i - 1).also { i = it } > 96 - 8 - 3 - 1) {
                tmp += 1L shl i - (96 - 8 - 3)
            }
            val filter = tmp.toInt()
            tmp = 0
            i = 96 - 8 - 3
            while (bs.previousSetBit(i - 1).also { i = it } > 96 - 8 - 3 - 3 - 1) {
                tmp += 1L shl i - (96 - 8 - 3 - 3)
            }
            val partition = tmp.toByte()
            val cpb = getCompanyPrefixBits(partition.toInt())
            tmp = 0
            i = 96 - 8 - 3 - 3
            while (bs.previousSetBit(i - 1).also { i = it } > 96 - 8 - 3 - 3 - cpb - 1) {
                tmp += 1L shl i - (96 - 8 - 3 - 3 - cpb)
            }
            val companyPrefix = tmp
            val irb = getItemReferenceBits(partition.toInt())
            tmp = 0
            i = 96 - 8 - 3 - 3 - cpb
            while (bs.previousSetBit(i - 1).also { i = it } > 96 - 8 - 3 - 3 - cpb - irb - 1) {
                tmp += 1L shl i - (96 - 8 - 3 - 3 - cpb - irb)
            }
            val itemReference = tmp.toInt()

            //for the remainder, which is the serial, we can use fixed values
            tmp = 0
            i = serialBitSize.toInt()
            while (bs.previousSetBit(i - 1).also { i = it } > -1) {
                tmp += 1L shl i
            }
            val serial = tmp
            return try {
                val sgtin96 = Sgtin96(
                    filter,
                    getCompanyPrefixDigits(partition.toInt()),
                    companyPrefix,
                    itemReference,
                    serial
                )
                sgtin96.epc = epc
                sgtin96
            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Invalid EPC: " + e.message)
            }
        }
    }
}