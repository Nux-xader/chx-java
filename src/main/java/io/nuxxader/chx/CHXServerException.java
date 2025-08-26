package io.nuxxader.chx;

import java.io.IOException;

public class CHXServerException extends IOException {
    public CHXServerException(String message) {
        super(message);
    }

    public CHXServerException(String message, Throwable cause) {
        super(message, cause);
    }
}