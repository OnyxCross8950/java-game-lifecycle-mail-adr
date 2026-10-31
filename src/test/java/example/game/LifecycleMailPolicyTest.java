package example.game;

public final class LifecycleMailPolicyTest {
    public static void main(String[] args) {
        LifecycleEvent pending = new LifecycleEvent("p-7", "Night Map", "asset-submitted", LifecycleEvent.ModerationStatus.PENDING);
        LifecycleMailPolicy.Decision decision = new LifecycleMailPolicy().decide(pending);
        if (!decision.action().equals("MODERATION_REQUIRED")) throw new AssertionError(decision);
        if (!decision.templateName().equals("PLAYER_ASSET_REVIEW")) throw new AssertionError(decision);
        System.out.println("PASS: pending player assets require moderation");
    }
}
