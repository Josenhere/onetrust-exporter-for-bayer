package extracts

import dataClasses.ExtractSetting

object ExtractFactory {
    fun create(extractSetting: ExtractSetting): Extract {
        return when(extractSetting.typeOfExtract){
            Extract.TypeOfExtract.STANDARD_EXTRACT -> StandardExtract(extractSetting)
            Extract.TypeOfExtract.PENDING_EXTRACT -> PendingExtract(extractSetting)
            Extract.TypeOfExtract.SPECIAL_EXTRACT -> SpecialExtract(extractSetting)
            Extract.TypeOfExtract.PURPOSE_BY_ROW_EXTRACT -> PurposeByRowExtract(extractSetting)
        }
    }
}