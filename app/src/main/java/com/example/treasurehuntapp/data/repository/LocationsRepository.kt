package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.LocationsApi
import com.example.treasurehuntapp.data.source.remote.dto.locations.CreateLocationDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.UpdateLocationDto
import javax.inject.Inject

class LocationsRepository @Inject constructor(
    private val api: LocationsApi
) {

    suspend fun createLocation(dto: CreateLocationDto): LocationDto {
        return api.create(dto).data
    }

    suspend fun getLocationsByHunt(treasureHuntId: String): List<LocationDto> {
        return api.getByHunt(treasureHuntId).data
    }

    suspend fun getLocationById(locationId: String): LocationDto {
        return api.getById(locationId).data
    }

    suspend fun updateLocation(locationId: String, dto: UpdateLocationDto): LocationDto {
        return api.update(locationId, dto).data
    }

    suspend fun deleteLocation(locationId: String) {
        api.delete(locationId)
    }
}
