package com.bctags.bcstocks.ui.sgtin

import com.bctags.bcstocks.ui.sgtin.Sgtin.SgtinFilter
import com.bctags.bcstocks.ui.sgtin.Sgtin.getCompanyPrefixBits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getCompanyPrefixDigits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getItemReferenceBits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getItemReferenceDigits
import com.bctags.bcstocks.ui.sgtin.Sgtin.getPartition
import com.bctags.bcstocks.ui.sgtin.Sgtin
import org.epctagcoder.util.Converter
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.BitSet
import java.util.Locale
import java.util.function.Supplier
import java.util.stream.Collectors



/**
 * The Serialised Global Trade Item Number EPC scheme is used to assign a unique
 * identity to an instance of a trade item, such as a specific instance
 * of a product or SKU.
 */
class Sgtin198  (
    filter: Int,
    companyPrefixDigits: Int,
    companyPrefix: Long,
    itemReference: Int,
    serial: String
)  {
    //Sgtin-198 epc is 52 hex chars long
    var epc: String? = null
        get() {
            if (field == null) {
                val epc = BitSet(52 * 4) //Sgtin-198 epc is 52 hex chars long
                var i = serialSize + padding - 1
                for (b in serial.toByteArray()) {
                    var j = 6
                    while (j >= 0) {
                        epc[i] = b.toInt() shr j and 1 == 1
                        j--
                        i--
                    }
                }
                i = serialSize + padding
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
                val sb = StringBuffer(52)
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
    public val partition: Byte
    public val filter: SgtinFilter
    val companyPrefix: Long
    val itemReference: Int
    val serial: String
    var uri: String? = null
        get() {
            if (field == null) field = uriHeader + filter.value.toString() + "." + String.format(
                "%0" + getCompanyPrefixDigits(partition.toInt()) + "d", companyPrefix
            ) + "." + String.format(
                "%0" + getItemReferenceDigits(partition.toInt()) + "d",
                itemReference
            ) + "." + serial.chars().mapToObj { c: Int ->
                getUriSerialChar(c.toChar())
            }.collect(Collectors.joining())
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
        require(serial.length <= serialMaxChars) { "Serial must at most $serialMaxChars alphanumeric characters long" }
        for (ch in serial.toCharArray()) require(
            !(ch.code < 0x21 || ch.code > 0x7A || invalidTableA1Chars.contains(
                ch
            ))
        ) { "Invalid serial character" }
        this.serial = serial
    }

    fun getFilter(): Int {
        return filter.value
    }

    override fun toString(): String {
        return uri!!
    }

    override fun equals(o: Any?): Boolean {
        return if (o !is Sgtin198) false else o.uri == uri
    }

    /**
     * Table A-1 for the encoding
     */
    public fun getUriSerialChar(ch: Char): String {
        require(!(ch.code < 0x21 || ch.code > 0x7A || invalidTableA1Chars.contains(ch))) { "Wrong char" }
        return when (ch) {
            '"', '%', '&', '/', '<', '>', '?' -> "%" + String.format("%02x", ch.code).uppercase(
                Locale.getDefault()
            )

            else -> ch.toString()
        }
    }

    fun getCompanyPrefixBits(partition: Int): Byte {
        return when (partition) {
            0 -> 40
            1 -> 37
            2 -> 34
            3 -> 30
            4 -> 27
            5 -> 24
            6 -> 20
            else -> throw IllegalArgumentException("Invalid Partition: $partition (0-6)")
        }
    }

    fun getItemReferenceBits(partition: Int): Byte {
        return when (partition) {
            0 -> 4
            1 -> 7
            2 -> 10
            3 -> 14
            4 -> 17
            5 -> 20
            6 -> 24
            else -> throw IllegalArgumentException("Invalid Partition: $partition (0-6)")
        }
    }


    fun getPartition(companyPrefixDigits: Int): Int {
        return 12 - companyPrefixDigits
    }

    fun getCompanyPrefixDigits(partition: Int): Int {
        return 12 - partition
    }

    fun getItemReferenceDigits(partition: Int): Int {
        return partition + 1
    }

    public enum class SgtinFilter(val value: Int) {
        all_others_0(0), pos_item_1(1), case_2(2), inner_pack_4(4), reserved_3(3), reserved_5(5), unit_load_6(
            6
        ),
        component_7(7);

    }

    companion object {
        public const val epcHeader: Byte = 54
        public const val serialSize = 140
        public const val padding = 10
        public const val serialMaxChars: Byte = 20
        public const val uriHeader = "urn:epc:tag:sgtin-198:"

        // Table A-1 specifies the valid characters in serials, this set is to make the validators more maintenable
        public val invalidTableA1Chars =
            listOf(0x23, 0x24, 0x40, 0x5B, 0x5C, 0x5D, 0x5E, 0x60).stream()
                .map { obj: Int? ->
                    Char::class.java.cast(
                        obj
                    )
                }.collect(
                    Collectors.toCollection(
                        Supplier { HashSet() })
                )

        fun fromFields(
            filter: Int,
            companyPrefixDigits: Int,
            companyPrefix: Long,
            itemReference: Int,
            serial: String
        ): Sgtin198 {
            return Sgtin198(filter, companyPrefixDigits, companyPrefix, itemReference, serial)
        }

        fun fromGs1Key(
            filter: Int,
            companyPrefixDigits: Int,
            ai01: String,
            ai21: String
        ): Sgtin198 {
            require(!(ai01.length < 14 || !Converter.isNumeric(ai01))) { "GTIN must be 14 digits long" }
            return Sgtin198(
                filter,
                companyPrefixDigits,
                ai01.substring(1, companyPrefixDigits + 1).toLong(),
                (ai01[0].toString() + ai01.substring(companyPrefixDigits + 1, 14 - 1)).toInt(),
                ai21
            )
        }

        fun fromUri(uri: String): Sgtin198 {
            require(uri.startsWith(uriHeader)) { "Decoding error: wrong URI header, expected $uriHeader" }
            val uriParts = uri.substring(uriHeader.length).split("\\.".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val filter = uriParts[0].toInt()
            val partition = getPartition(uriParts[1].length).toByte()
            val companyPrefix = uriParts[1].toLong()
            val itemReference = uriParts[2].toInt()
            val serial = uriParts[3]
            val sb = StringBuilder()
            val serialSplit = serial.split("%".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            sb.append(serialSplit[0])
            for (i in 1 until serialSplit.size) {
                sb.append(serialSplit[i].substring(0, 2).toInt(16).toChar())
                sb.append(serialSplit[i].substring(2))
            }
            val sgtin198 = fromFields(
                filter,
                getCompanyPrefixDigits(partition.toInt()),
                companyPrefix,
                itemReference,
                sb.toString()
            )
            sgtin198.uri = uri
            return sgtin198
        }

        fun fromEpc(epc: String): Sgtin198 {
            val a = ArrayList<String>()
            run {
                var i = 0
                while (i < epc.length) {
                    a.add(epc.substring(i, i + 2))
                    i += 2
                }
            }
            val bb = ByteBuffer.allocate(26)
            for (i in a.indices.reversed()) bb.put(a[i].toInt(16).toByte())
            bb.rewind()
            val bs = BitSet.valueOf(bb)
            var i: Int
            var tmp: Long
            tmp = 0
            i = 208
            while (bs.previousSetBit(i - 1).also { i = it } > 208 - 8 - 1) {
                tmp += 1L shl i - (208 - 8)
            }
            require(tmp == epcHeader.toLong()) { "Invalid header" } //maybe the decoder could choose the structure from the header?
            tmp = 0
            i = 208 - 8
            while (bs.previousSetBit(i - 1).also { i = it } > 208 - 8 - 3 - 1) {
                tmp += 1L shl i - (208 - 8 - 3)
            }
            val filter = tmp.toInt()
            tmp = 0
            i = 208 - 8 - 3
            while (bs.previousSetBit(i - 1).also { i = it } > 208 - 8 - 3 - 3 - 1) {
                tmp += 1L shl i - (208 - 8 - 3 - 3)
            }
            val partition = tmp.toByte()
            val cpb = getCompanyPrefixBits(partition.toInt())
            tmp = 0
            i = 208 - 8 - 3 - 3
            while (bs.previousSetBit(i - 1).also { i = it } > 208 - 8 - 3 - 3 - cpb - 1) {
                tmp += 1L shl i - (208 - 8 - 3 - 3 - cpb)
            }
            val companyPrefix = tmp
            val irb = getItemReferenceBits(partition.toInt())
            tmp = 0
            i = 208 - 8 - 3 - 3 - cpb
            while (bs.previousSetBit(i - 1).also { i = it } > 208 - 8 - 3 - 3 - cpb - irb - 1) {
                tmp += 1L shl i - (208 - 8 - 3 - 3 - cpb - irb)
            }
            val itemReference = tmp.toInt()
            val serialBuilder = StringBuilder("")
            var tmpba: ByteArray?
            i =
                208 - 58 //buffer size - epcheader.size - filter.size - partition.size - getCompanyPrefixBits(partition) - getItemReferenceBits(partition)
            while (bs[i - 7, i].toByteArray().also { tmpba = it }.size != 0) {
                serialBuilder.append(String(tmpba!!))
                i -= 7
            }
            val serial = serialBuilder.toString()
            return try {
                val sgtin198 = Sgtin198(
                    filter,
                    getCompanyPrefixDigits(partition.toInt()),
                    companyPrefix,
                    itemReference,
                    serial
                )
                sgtin198.epc = epc
                sgtin198
            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Invalid EPC: " + e.message)
            }
        }
    }
}