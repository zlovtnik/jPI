package com.churchapp.entity.enums

enum class DonationType(val description: String) {
    TITHE("Regular tithe donation"),
    OFFERING("General offering"),
    BUILDING_FUND("Building fund contribution"),
    MISSIONS("Missions support"),
    SPECIAL("Special occasion donation"),
    OTHER("Other donation type")
}
