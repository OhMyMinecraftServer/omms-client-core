package icu.takeneko.omms.client.session.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequest {
    private long version;
    private String token;
}
