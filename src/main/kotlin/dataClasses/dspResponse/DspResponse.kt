package dataClasses.dspResponse


data class DspResponse(
    val content: List<DataSubject>,
    val pageable: Pageable,
    val totalPages: Int,
    val last: Boolean,
    val totalElements: Int,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val empty: Boolean
)