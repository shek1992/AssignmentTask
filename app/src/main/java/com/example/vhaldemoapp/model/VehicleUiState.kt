package com.example.vhaldemoapp.model

import com.example.vhaldemoapp.data.CarInfoData

data class VehicleUiState(
    val isCarConnected: Boolean = false,
    val fanSpeed: Int? = 1,
    val maxFanSpeed: Int = 6,
    val isHvacPowerOn: Boolean = true,
    val isAcOn: Boolean = true,
    val isAutoClimateOn: Boolean = false,
    val hvacTemperatureC: Float? = 21.5f,
    val gearSelection: Int? = 4, // 4 = P
    val carInfo: CarInfoData? = null,
    val errorMessage: String? = null,
    val speedKmh: Float? = 88f,
    val fuelLevelPercent: Float? = 100f,
    val driveMode: String = "SONIC",
    val rangeKm: Int = 62,
    val odoKm: Int = 1324,
    val cruiseSpeedKmh: Int = 72,
    val isCruiseActive: Boolean = true,
    val speedLimitKmh: Int = 80,
    val isSpeedExceeded: Boolean = true,
    val isSuperHoldActive: Boolean = true,
    val isRapidBrakeActive: Boolean = true,
    val leftTurnSignal: Boolean = false,
    val rightTurnSignal: Boolean = false,
    val isHighBeamOn: Boolean = true,
    val isParkingActive: Boolean = false
)
