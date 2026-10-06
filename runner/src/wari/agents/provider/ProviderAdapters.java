package wari.agents.provider;

public final class ProviderAdapters {
    private ProviderAdapters() {}
    public static ProviderAdapter select(String name) {
        return switch (name) {
            case "codex" -> new CodexAdapter();
            default -> throw new IllegalArgumentException("Proveedor no disponible/certificado: " + name);
        };
    }
}
