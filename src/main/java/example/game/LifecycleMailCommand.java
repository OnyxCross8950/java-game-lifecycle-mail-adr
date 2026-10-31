package example.game;

public final class LifecycleMailCommand {
    public static void main(String[] args) throws Exception {
        LayeredConfig config = LayeredConfig.fromEnvironment();
        LifecycleEvent event = new LifecycleEvent("player-42", "Solar Fox", "asset-approved", LifecycleEvent.ModerationStatus.APPROVED);
        LifecycleMailPolicy.Decision decision = new LifecycleMailPolicy().decide(event);
        if (!decision.action().equals("SEND")) throw new IllegalStateException("event is not sendable");
        String result = new InfraiEmailClient(config.apiKey()).send(config.recipient(), "Your asset was approved", "<p>Asset: " + event.assetName() + "</p>", decision.templateName());
        System.out.println(result);
    }
}
