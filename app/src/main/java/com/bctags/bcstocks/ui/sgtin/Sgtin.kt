package com.bctags.bcstocks.ui.sgtin

object Sgtin  {

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
}