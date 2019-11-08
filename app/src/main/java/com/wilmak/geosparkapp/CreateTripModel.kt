package com.wilmak.geosparkapp

class CreateTripModel {
    data class Events(
        val trip_id: String? = null,
        val user_id: String? = null,
        val created_at: String? = null,
        val event_type: String? = null
    )
    data class Data(
        val events: Array<Events>
    )
    data class ResponseData(
        val code: Int? = null,
        val msg: String? = null,
        val status: String? = null,
        val data: Array<Data>
    )

    data class RequestUserId(
        val user_id: String? = null
    )
}

