package com.bctags.bcstocks.util

import java.io.Serializable
import java.text.ParseException
import java.util.Locale
import java.util.logging.Level
import java.util.logging.Logger


/**
 *
 * @author chpressler
 */
class EPCTools : Serializable {
    init {
        init()
    }

    private fun init() {
        /**
         * The header determines the EPC tag standard. The following are a few
         * of the defined header standards bound to the representing HEX Value
         * in the Header
         */
        headerEncodings = HashMap()
        //        headerEncodings.put(206L, ENCODING.DOD_64);
//        headerEncodings.put(207L, ENCODING.DOD_96);
        headerEncodings!![54L] = ENCODING.SGTIN_198
        headerEncodings!![48L] = ENCODING.SGTIN_96
        headerEncodings!![49L] = ENCODING.SSCC
        //        headerEncodings.put(50L, ENCODING.GLN_96);
//        headerEncodings.put(51L, ENCODING.GRAI_96);
//        headerEncodings.put(52L, ENCODING.GIAI_96);
//        headerEncodings.put(53L, ENCODING.GID_96);
        giaiPartitionTableCompPrefix = HashMap()
        giaiPartitionTableCompPrefix!![0L] = 40L
        giaiPartitionTableCompPrefix!![1L] = 37L
        giaiPartitionTableCompPrefix!![2L] = 34L
        giaiPartitionTableCompPrefix!![3L] = 30L
        giaiPartitionTableCompPrefix!![4L] = 27L
        giaiPartitionTableCompPrefix!![5L] = 24L
        giaiPartitionTableCompPrefix!![6L] = 20L
        giaiPartitionTableSerialReference = HashMap()
        giaiPartitionTableSerialReference!![0L] = 18L
        giaiPartitionTableSerialReference!![1L] = 21L
        giaiPartitionTableSerialReference!![2L] = 24L
        giaiPartitionTableSerialReference!![3L] = 28L
        giaiPartitionTableSerialReference!![4L] = 31L
        giaiPartitionTableSerialReference!![5L] = 34L
        giaiPartitionTableSerialReference!![6L] = 38L
        giaiPartitionTableIndividualAssetRef = HashMap()
        giaiPartitionTableIndividualAssetRef!![0L] = 42L
        giaiPartitionTableIndividualAssetRef!![1L] = 45L
        giaiPartitionTableIndividualAssetRef!![2L] = 48L
        giaiPartitionTableIndividualAssetRef!![3L] = 52L
        giaiPartitionTableIndividualAssetRef!![4L] = 55L
        giaiPartitionTableIndividualAssetRef!![5L] = 58L
        giaiPartitionTableIndividualAssetRef!![6L] = 62L
    }

    /**
     * Depending on the EPC tag standard these are the tag data constructs
     *
     */
    enum class TAG_DATA {
        HEADER, FILTER, PARTITION, COMPANY_PREFIX, ITEM_REFERENCE, SERIAL_REFERENCE, SERIAL_NUMBER, UNALLOCATED, INDIVIDUAL_ASSET_REFERENCE, CAGE_CODE
    }

    /*public enum ENCODING {
        DOD_64, DOD_96, SGTIN_96, SSCC, GLN_96, GRAI_96, GIAI_96, GID_96, SGTIN_198
    }*/
    enum class ENCODING {
        SGTIN_96, SGTIN_198, SSCC
    }

    private fun fillLeftWithZeros(s: String, digits: Int): String {
        val sb = StringBuilder()
        sb.append(s)
        while (sb.length < digits) {
            sb.insert(0, "0")
        }
        return sb.toString()
    }

    private fun fillRightWithZeros(s: String, digits: Int): String {
        val sb = StringBuilder()
        sb.append(s)
        while (sb.length < digits) {
            sb.append("0")
        }
        return sb.toString()
    }

    private fun hexToBinaryString(hex: String): String {
        val sb = StringBuilder()
        for (b in hex.toByteArray()) {
            sb.append(
                fillLeftWithZeros(
                    Integer.toBinaryString(String(byteArrayOf(b)).toInt(16)),
                    4
                )
            )
        }
        return sb.toString()
    }

    private fun binaryStringToHex(bin: String): String {
        return java.lang.Long.toHexString(binStringToLong(bin))
    }

    @Throws(Exception::class)
    fun getSerialNumber(data: HashMap<TAG_DATA?, String>): String? {
        return if (data[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString() || data[TAG_DATA.HEADER] == ENCODING.SGTIN_198.toString()) {
            data[TAG_DATA.SERIAL_NUMBER]
        } else if (data[TAG_DATA.HEADER] == ENCODING.SSCC.toString()) {
            data[TAG_DATA.SERIAL_REFERENCE]
        } else {
            throw Exception("unsupported Header: " + data[TAG_DATA.HEADER])
        }
    }

    fun getCompanyPrefix(data: HashMap<TAG_DATA?, String>): String? {
        return data[TAG_DATA.COMPANY_PREFIX]
    }

    @Throws(Exception::class)
    fun getGTIN(data: HashMap<TAG_DATA?, String>): String? {
        return if (data[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString() || data[TAG_DATA.HEADER] == ENCODING.SGTIN_198.toString()) {
            val gtinNoChksum = data[TAG_DATA.ITEM_REFERENCE]!!
                .substring(0, 1) + data[TAG_DATA.COMPANY_PREFIX] + data[TAG_DATA.ITEM_REFERENCE]!!
                .substring(1)
            gtinNoChksum + getChecksum(gtinNoChksum)
        } else {
            null
        }
    }

    @Throws(Exception::class)
    fun getGTIN(epcHex: String): String? {
        val data = parseHexString(epcHex)
        return if (data[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString() || data[TAG_DATA.HEADER] == ENCODING.SGTIN_198.toString()) {
            val gtinNoChksum = data[TAG_DATA.ITEM_REFERENCE]!!
                .substring(0, 1) + data[TAG_DATA.COMPANY_PREFIX] + data[TAG_DATA.ITEM_REFERENCE]!!
                .substring(1)
            gtinNoChksum + getChecksum(gtinNoChksum)
        } else {
            null
        }
    }

    @Throws(Exception::class)
    fun getCompanyPrefix(epcHex: String): String? {
        val map = parseHexString(epcHex)
        return map[TAG_DATA.COMPANY_PREFIX]
    }

    @Throws(Exception::class)
    fun getItemReference(epcHex: String): String? {
        val map = parseHexString(epcHex)
        return map[TAG_DATA.ITEM_REFERENCE]
    }

    @Throws(Exception::class)
    fun getSerialNumber(epcHex: String): String? {
        val map = parseHexString(epcHex)
        return getSerialNumber(map)
    }

    @Throws(Exception::class)
    fun getItemReference(data: HashMap<TAG_DATA?, String>): String? {
        return if (data[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString()) {
            data[TAG_DATA.ITEM_REFERENCE]
        } else {
            null
        }
    }

    @Throws(Exception::class)
    fun getSSCC(epcHex: String): String? {
        return getSSCC(parseHexString(epcHex))
    }

    @Throws(Exception::class)
    fun getSSCC(data: HashMap<TAG_DATA?, String>): String? {
        return if (data[TAG_DATA.HEADER] == ENCODING.SSCC.toString()) {
            val compPrefix = data[TAG_DATA.COMPANY_PREFIX]
            val serialRef = data[TAG_DATA.SERIAL_REFERENCE]
            val sscc = serialRef!!.substring(0, 1) + compPrefix + serialRef.substring(1)
            sscc + getChecksum(sscc)
        } else {
            null
        }
    }

    @Throws(Exception::class)
    fun createSGTIN_96HexEPC(
        filter: Int,
        partition: Int,
        compPrefix: String?,
        itemRef: String?,
        serialNumber: String
    ): String {
        try {
            if (serialNumber.toLong() > 274877906944L) { //SGTIN-96 reserves 38 bit for sn. 2 ^ 38 = 274877906944 max value for sn in SGTIN-96
                throw Exception("serialnumber ($serialNumber) out of range for SGTIN-96. Maximum Serialnumber value is 274877906944.")
            }
        } catch (e: NumberFormatException) {
            throw Exception(
                "returned serialnumber ($serialNumber) could not be parsed. Serialnumber must be numeric positive value. Max supported value is: 9223372036854775807.",
                e
            )
        }
        if (filter > 7) {
            throw Exception("filter value can not be bigger than 8")
        }
        val b_header = fillLeftWithZeros(Integer.toBinaryString(48), 8) //8 bit header
        val b_filter = fillLeftWithZeros(Integer.toBinaryString(filter), 3) //3 bit filter
        val b_partition = fillLeftWithZeros(Integer.toBinaryString(partition), 3) //3 bit partition
        val compPrefixLength = giaiPartitionTableCompPrefix!![partition.toLong()]!!
        if (java.lang.Long.toBinaryString(compPrefix!!.toLong()).length > compPrefixLength) {
            throw Exception("comp prefix length for partition: $partition is too big. Max Length: $compPrefixLength.")
        }
        val b_compPrefix = fillLeftWithZeros(
            java.lang.Long.toBinaryString(
                compPrefix.toLong()
            ), compPrefixLength.toInt()
        )
        val itemRefLength = giaiPartitionTableIndividualAssetRef!![partition.toLong()]!! - 38
        if (Integer.toBinaryString(itemRef!!.toInt()).length > itemRefLength) {
            throw Exception("item reference length for partition: $partition is too big. Max Length: $itemRefLength.")
        }
        val b_itemRef =
            fillLeftWithZeros(Integer.toBinaryString(itemRef.toInt()), itemRefLength.toInt())
        val b_serialNumber =
            fillLeftWithZeros(java.lang.Long.toBinaryString(serialNumber.toLong()), 38) //38 bit sn
        val bin = b_header + b_filter + b_partition + b_compPrefix + b_itemRef + b_serialNumber
        val hex = StringBuilder()
        var offs = 0
        while (offs < bin.length) {
            hex.append(binaryStringToHex(bin.substring(offs, 4.let { offs += it; offs })))
        }
        return hex.toString().uppercase(Locale.getDefault())
    }

    @Throws(Exception::class)
    fun createSGTIN_198HexEPC(
        filter: Int,
        partition: Int,
        compPrefix: String,
        itemRef: String,
        serialNumber: String
    ): String {
        if (serialNumber.length > 20) { //up to 20 alphanumeric digits
            throw Exception("serialnumber length can not be higher than 20")
        }
        if (filter > 7) {
            throw Exception("filter value can not be bigger than 8")
        }
        val b_header = fillLeftWithZeros(Integer.toBinaryString(54), 8) //8 bit header
        val b_filter = fillLeftWithZeros(Integer.toBinaryString(filter), 3) //3 bit filter
        val b_partition = fillLeftWithZeros(Integer.toBinaryString(partition), 3) //3 bit partition
        val compPrefixLength = giaiPartitionTableCompPrefix!![partition.toLong()]!!
        if (java.lang.Long.toBinaryString(compPrefix.toLong()).length > compPrefixLength) {
            throw Exception("comp prefix length for partition: $partition is too big. Max Length: $compPrefixLength.")
        }
        val b_compPrefix = fillLeftWithZeros(
            java.lang.Long.toBinaryString(compPrefix.toLong()),
            compPrefixLength.toInt()
        )
        val itemRefLength = giaiPartitionTableIndividualAssetRef!![partition.toLong()]!! - 38
        if (Integer.toBinaryString(itemRef.toInt()).length > itemRefLength) {
            throw Exception("item reference length for partition: $partition is too big. Max Length: $itemRefLength.")
        }
        val b_itemRef =
            fillLeftWithZeros(Integer.toBinaryString(itemRef.toInt()), itemRefLength.toInt())
        val b_serialNumber = fillRightWithZeros(
            toBinaryString(serialNumber),
            140
        ) //140 bit sn - alphanumeric (140 bit / 20 digits = 7bit - ASCI 128 per digit)
        val bin = b_header + b_filter + b_partition + b_compPrefix + b_itemRef + b_serialNumber
        val hex = StringBuilder()
        var offs = 0
        while (offs < bin.length) {
            var tmp = bin.substring(
                offs,
                if (4.let { offs += it; offs } > bin.length) bin.length else offs)
            if (tmp.length < 4) {
                tmp = fillRightWithZeros(tmp, 4)
            }
            hex.append(binaryStringToHex(tmp))
        }
        return hex.toString().uppercase(Locale.getDefault())
    }

    private fun toBinaryString(s: String): String {
        val ba: ByteArray
        val ret = StringBuilder()
        ba = s.toByteArray()
        for (b in ba) {
            ret.append(fillLeftWithZeros(Integer.toBinaryString(b.toInt()), 7))
        }
        return ret.toString()
    }

    @Throws(Exception::class)
    fun createSSCCHexEPC(
        filter: Int,
        partition: Int,
        compPrefix: String,
        extensionCode: String,
        serialRef: String
    ): String {
        if (filter > 7) {
            throw Exception("filter value can not be bigger than 8")
        }
        val b_header = fillLeftWithZeros(Integer.toBinaryString(49), 8) //8 bit header
        val b_filter = fillLeftWithZeros(Integer.toBinaryString(filter), 3) //3 bit filter
        val b_partition = fillLeftWithZeros(Integer.toBinaryString(partition), 3) //3 bit partition
        val compPrefixLength = giaiPartitionTableCompPrefix!![partition.toLong()]!!
        val b_compPrefix = fillLeftWithZeros(
            java.lang.Long.toBinaryString(compPrefix.toLong()),
            compPrefixLength.toInt()
        )
        val serialRefBitLength = giaiPartitionTableSerialReference!![partition.toLong()]!!
        val serialRefDigits = partition + 5
        var b_serialRef = java.lang.Long.toBinaryString(
            (extensionCode + fillLeftWithZeros(
                serialRef,
                serialRefDigits - 1
            )).toLong()
        )
        b_serialRef = fillLeftWithZeros(b_serialRef, serialRefBitLength.toInt())
        val b_unallocated = fillLeftWithZeros("0", 24) //24 bit unallocated
        val bin = b_header + b_filter + b_partition + b_compPrefix + b_serialRef + b_unallocated
        val hex = StringBuilder()
        var offs = 0
        while (offs < 96) {
            hex.append(binaryStringToHex(bin.substring(offs, 4.let { offs += it; offs })))
        }
        return hex.toString().uppercase(Locale.getDefault())
    }
    var re = Regex(".*[a-zA-Z]+.*")
    @Throws(Exception::class)
    fun createEPCPureIdentityURI(epcHex: String): String {
        val map = parseHexString(epcHex)
        return if (map[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString() || map[TAG_DATA.HEADER] == ENCODING.SGTIN_198.toString()) {
            var sn = map[TAG_DATA.SERIAL_NUMBER]
            if (sn!!.matches(re)) {
                sn = sn.replaceFirst("^0*".toRegex(), "")
            } else {
                try {
                    sn = java.lang.Long.toString(sn.toLong())
                } catch (e: Exception) {
                }
            }
            "urn:epc:id:sgtin:" + map[TAG_DATA.COMPANY_PREFIX] + "." + map[TAG_DATA.ITEM_REFERENCE] + "." + sn
        } else if (map[TAG_DATA.HEADER] == ENCODING.SSCC.toString()) {
            var sn = map[TAG_DATA.SERIAL_REFERENCE]
            if (sn!!.matches(re)) {
                sn = sn.replaceFirst("^0*".toRegex(), "")
            } else {
                try {
                    sn = java.lang.Long.toString(sn.toLong())
                } catch (e: Exception) {
                }
            }
            "urn:epc:id:sscc:" + map[TAG_DATA.COMPANY_PREFIX] + "." + sn
        } else {
            throw Exception("unsupported Header: " + map[TAG_DATA.HEADER])
        }
    }

    @Throws(Exception::class)
    fun createEPCTagIdURI(epcHex: String): String {
        val map = parseHexString(epcHex)
        if (map[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString()) {
            var sn = map[TAG_DATA.SERIAL_NUMBER]
            if (sn!!.matches(re)) {
                sn = sn.replaceFirst("^0*".toRegex(), "")
            } else {
                try {
                    sn = java.lang.Long.toString(sn.toLong())
                } catch (e: Exception) {
                }
            }
            return "urn:epc:tag:sgtin-96:" + map[TAG_DATA.FILTER] + "." + map[TAG_DATA.COMPANY_PREFIX] + "." + map[TAG_DATA.ITEM_REFERENCE] + "." + sn
        }
        return if (map[TAG_DATA.HEADER] == ENCODING.SGTIN_198.toString()) {
            var sn = map[TAG_DATA.SERIAL_NUMBER]
            if (sn!!.matches(re)) {
                sn = sn.replaceFirst("^0*".toRegex(), "")
            } else {
                try {
                    sn = java.lang.Long.toString(sn.toLong())
                } catch (e: Exception) {
                }
            }
            "urn:epc:tag:sgtin-198:" + map[TAG_DATA.FILTER] + "." + map[TAG_DATA.COMPANY_PREFIX] + "." + map[TAG_DATA.ITEM_REFERENCE] + "." + sn
        } else if (map[TAG_DATA.HEADER] == ENCODING.SSCC.toString()) {
            var sn = map[TAG_DATA.SERIAL_REFERENCE]
            if (sn!!.matches(re)) {
                sn = sn.replaceFirst("^0*".toRegex(), "")
            } else {
                try {
                    sn = java.lang.Long.toString(sn.toLong())
                } catch (e: Exception) {
                }
            }
            "urn:epc:tag:sscc-96:" + map[TAG_DATA.FILTER] + "." + map[TAG_DATA.COMPANY_PREFIX] + "." + sn
        } else {
            throw Exception("unsupported Header: " + map[TAG_DATA.HEADER])
        }
    }

    @Throws(Exception::class)
    fun createSGTIN_96HexEPC(epc: String, serialNumber: String): String {
        val map = parseHexString(epc)
        val filter = map[TAG_DATA.FILTER]!!.toInt()
        val partition = map[TAG_DATA.PARTITION]!!.toInt()
        val companyPrefix = getCompanyPrefix(map)
        val itemReference = getItemReference(map)
        return createSGTIN_96HexEPC(filter, partition, companyPrefix, itemReference, serialNumber)
    }

    @Throws(Exception::class)
    fun createSGTIN_96HexEPC(packLevel: Int, gtin: String, sn: String): String {
        return createSGTIN_96HexEPC(
            packLevel,
            6,
            gtin.substring(1, 7),
            gtin.substring(0, 1) + gtin.substring(7, 13),
            sn
        )
    }

    @Throws(Exception::class)
    fun createSGTIN_198HexEPC(packLevel: Int, gtin: String, sn: String): String {
        return createSGTIN_198HexEPC(
            packLevel,
            6,
            gtin.substring(1, 7),
            gtin.substring(0, 1) + gtin.substring(7, 13),
            sn
        )
    }

    @Throws(Exception::class)
    fun createSSCCHexEPC(
        packLevel: Int,
        companyPrefix: String,
        extensionCode: String,
        serialRef: String
    ): String {
        val partition = if (companyPrefix.length < 6) 6 else 12 - companyPrefix.length
        return createSSCCHexEPC(packLevel, partition, companyPrefix, extensionCode, serialRef)
    }

    fun getEncoding(epcHex: String): ENCODING? {
        val binaryData = hexToBinaryString(epcHex)
        return headerEncodings!![binStringToLong(binaryData.substring(0, 8))]
    }

    fun isSGTIN96(epcHex: String): Boolean {
        val binaryData = hexToBinaryString(epcHex)
        return headerEncodings!![binStringToLong(binaryData.substring(0, 8))] == ENCODING.SGTIN_96
    }

    fun isSGTIN198(epcHex: String): Boolean {
        val binaryData = hexToBinaryString(epcHex)
        return headerEncodings!![binStringToLong(binaryData.substring(0, 8))] == ENCODING.SGTIN_198
    }

    fun isSSCC(epcHex: String): Boolean {
        val binaryData = hexToBinaryString(epcHex)
        return headerEncodings!![binStringToLong(binaryData.substring(0, 8))] == ENCODING.SSCC
    }

    /**
     * @param hexData the Data as Hex String
     *
     * @return a HashMap with all included TagData and values
     * @throws Exception
     */
    @Throws(Exception::class)
    fun parseHexString(hexData: String): HashMap<TAG_DATA?, String> {
//		if(hexData.length() != 24) {
//			throw new Exception("invalid input hex string");
//		}
        val binaryData = hexToBinaryString(hexData)
        var offs = 0
        val lHeader = binStringToLong(binaryData.substring(0, 8.let { offs += it; offs }))
        val header = headerEncodings!![lHeader]
            ?: throw Exception("unsupported Header: $lHeader")
        val data = HashMap<TAG_DATA?, String>()
        data[TAG_DATA.HEADER] = header.toString()
        val filter = binStringToLong(binaryData.substring(offs, 3.let { offs += it; offs }))
        data[TAG_DATA.FILTER] = java.lang.Long.toString(filter)
        //data.get(TAG_DATA.PARTITION);
        val partitionValue = binStringToLong(binaryData.substring(offs, 3.let { offs += it; offs }))
        data[TAG_DATA.PARTITION] = java.lang.Long.toString(partitionValue)
        val compPrefixBinaryLength = giaiPartitionTableCompPrefix!![partitionValue]!!
        val compPrefixDecimalLength = (12 - partitionValue).toInt()
        val compPrefix = binStringToLong(
            binaryData.substring(
                offs,
                compPrefixBinaryLength.let { offs += it.toInt(); offs })
        )
        data[TAG_DATA.COMPANY_PREFIX] =
            fillLeftWithZeros(java.lang.Long.toString(compPrefix), compPrefixDecimalLength)
        if (data[TAG_DATA.HEADER] != ENCODING.SSCC.toString()) {
            val itemreference = binStringToLong(
                binaryData.substring(
                    offs,
                    (giaiPartitionTableIndividualAssetRef!![partitionValue]!! - 38).let { offs += it.toInt(); offs })
            )
            val itemRef = fillLeftWithZeros(
                java.lang.Long.toString(itemreference),
                (13 - compPrefixDecimalLength)
            )
            data[TAG_DATA.ITEM_REFERENCE] = itemRef
        }
        if (data[TAG_DATA.HEADER] == ENCODING.SGTIN_96.toString()) {
            val serial = binStringToLong(binaryData.substring(offs))
            data[TAG_DATA.SERIAL_NUMBER] =
                fillLeftWithZeros(java.lang.Long.toString(serial), 12)
        } else if (data[TAG_DATA.HEADER] == ENCODING.SSCC.toString()) {
            val serialreference = binStringToLong(
                binaryData.substring(
                    offs,
                    giaiPartitionTableSerialReference!![partitionValue].let { offs += it!!.toInt(); offs })
            )
            data[TAG_DATA.SERIAL_REFERENCE] =
                fillLeftWithZeros(
                    java.lang.Long.toString(serialreference),
                    (17 - compPrefixDecimalLength)
                )
            val unallocated = binStringToLong(binaryData.substring(offs))
            data[TAG_DATA.UNALLOCATED] = java.lang.Long.toString(unallocated)
        } else if (data[TAG_DATA.HEADER] == ENCODING.SGTIN_198.toString()) {
            data[TAG_DATA.SERIAL_NUMBER] =
                fillLeftWithZeros(
                    binStringTo7bitASCII(
                        binaryData.substring(
                            offs,
                            binaryData.length
                        )
                    ), 20
                )
        }
        return data
    }

    private fun binStringTo7bitASCII(s: String): String {
        var offset = 0
        var length: Int
        var ret = ""
        while (offset < s.length) {
            length = if (offset + 7 > s.length) s.length - offset else 7
            val l = binStringToLong(s.substring(offset, offset + length))
            if (l > 9) { //skip control chars
                ret += Char(binStringToLong(s.substring(offset, offset + length)).toUShort())
            }
            offset += 7
        }
        return ret
    }

    private fun binStringToLong(s: String): Long {
        val c = s.toCharArray()
        var z: Long = 0
        var erg: Long = 0
        for (i in c.indices.reversed()) {
            if (z == 0L && c[i] == '0') {
                z++
                continue
            }
            erg += Math.pow((if (c[i] != '0') 2 else 0).toDouble(), z.toDouble()).toLong()
            z++
        }
        return erg
    }

    @Throws(ParseException::class)
    private fun getChecksum(sscc: String): Int {
        var realCC = -1
        try {
            var checksum = 0
            var factor = 3
            for (i in sscc.length downTo 1) {
                checksum += sscc[i - 1].toString().toInt() * factor
                factor = 4 - factor
            }
            //(realCC = 1000 - checksum) % 10
            realCC = (1000 - checksum) % 10
        } catch (e: Exception) {
            Logger.getLogger(EPCTools::class.java.name).log(Level.SEVERE, null, e)
            throw ParseException("sscc checksum error!", 1)
        }
        return realCC
    }

    fun isValidGTIN(gtin: String?): Boolean {
        if (gtin == null || gtin.isEmpty()) {
            return false
        }
        var factor = 3
        var checksum = 0
        try {
            val currentCC = gtin[gtin.length - 1].toString().toInt()
            for (i in gtin.length - 1 downTo 1) {

                checksum += gtin[i - 1].toString().toInt() * factor
                factor = 4 - factor
            }
            val realCC = (1000 - checksum) % 10
            if (realCC == currentCC) {
                return true
            }
        } catch (e: Exception) {
            Logger.getLogger(EPCTools::class.java.name).log(Level.SEVERE, null, e)
            return false
        }
        return false
    }

    companion object {
        private var headerEncodings: HashMap<Long, ENCODING>? = null
        private var giaiPartitionTableCompPrefix: HashMap<Long, Long>? = null
        private var giaiPartitionTableIndividualAssetRef: HashMap<Long, Long>? = null
        private var giaiPartitionTableSerialReference: HashMap<Long, Long>? = null
    }
}