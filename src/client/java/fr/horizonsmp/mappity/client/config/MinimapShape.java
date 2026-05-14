package fr.horizonsmp.mappity.client.config;

public enum MinimapShape {
    SQUARE("mappity.shape.square"),
    CIRCLE("mappity.shape.circle");

    private final String translationKey;

    MinimapShape(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }

    public MinimapShape next() {
        MinimapShape[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
