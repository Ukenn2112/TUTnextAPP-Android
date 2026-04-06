package com.meikenn.tama.domain.model

enum class RouteType(val displayName: String, val jsonKey: String) {
    FROM_SEISEKI_TO_SCHOOL("聖蹟桜ヶ丘駅発", "fromSeisekiToSchool"),
    FROM_NAGAYAMA_TO_SCHOOL("永山駅発", "fromNagayamaToSchool"),
    FROM_SCHOOL_TO_SEISEKI("聖蹟桜ヶ丘駅行", "fromSchoolToSeiseki"),
    FROM_SCHOOL_TO_NAGAYAMA("永山駅行", "fromSchoolToNagayama");

    /** True if departing FROM school (school is the origin) */
    val isFromSchool: Boolean
        get() = this == FROM_SCHOOL_TO_SEISEKI || this == FROM_SCHOOL_TO_NAGAYAMA
}
