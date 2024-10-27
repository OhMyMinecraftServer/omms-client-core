package icu.takeneko.omms.client.session.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private long version;
    private String token;
}
