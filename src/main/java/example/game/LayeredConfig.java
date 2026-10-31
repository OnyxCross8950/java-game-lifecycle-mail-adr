package example.game;

public record LayeredConfig(String apiKey, String recipient) {
    public static LayeredConfig fromEnvironment() {
        String key = System.getenv("INFRAI_API_KEY");
        String recipient = System.getenv("GAME_MAIL_TO");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        if (recipient == null || recipient.isBlank()) throw new IllegalStateException("GAME_MAIL_TO is required");
        return new LayeredConfig(key, recipient);
    }
}
