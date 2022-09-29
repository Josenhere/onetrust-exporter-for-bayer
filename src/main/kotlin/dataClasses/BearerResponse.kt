package dataClasses


import com.fasterxml.jackson.annotation.JsonProperty

data class BearerResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
)