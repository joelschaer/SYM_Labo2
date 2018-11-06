package ch.heigvd.sym.template;

import okhttp3.MediaType;

public enum MedType {
    JSON(MediaType.parse("application/json; charset=utf-8")),
    TEXT(MediaType.parse("text/plain; charset=utf-8"))
    ;

    private final MediaType type;

    MedType(MediaType type) {
        this.type = type;
    }

    public MediaType getMediaType() {
        return this.type;
    }
}
