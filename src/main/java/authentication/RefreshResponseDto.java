package authentication;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshResponseDto(
        @JsonProperty("refreshToken") String refreshToken
) {}