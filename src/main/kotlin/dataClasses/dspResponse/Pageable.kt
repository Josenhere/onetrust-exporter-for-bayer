package dataClasses.dspResponse


data class Pageable(
    val offset: Int,
    val pageSize: Int,
    val pageNumber: Int,
    val paged: Boolean,
    val unpaged: Boolean
)