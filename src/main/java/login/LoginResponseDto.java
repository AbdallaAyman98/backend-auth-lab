package login;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponseDto(
        @JsonProperty("accessToken")  String accessToken,
        @JsonProperty("refreshToken") String refreshToken
) {}