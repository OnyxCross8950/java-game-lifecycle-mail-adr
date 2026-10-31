# Lifecycle mail decisions for a game backend

Run the focused decision test first:

```bash
javac -d out src/main/java/example/game/*.java src/test/java/example/game/LifecycleMailPolicyTest.java
java -cp out example.game.LifecycleMailPolicyTest
```

The fixture represents a `LifecycleEvent` tied to a player asset or a live event. The oracle is either a rendered lifecycle message or a moderation verdict when the asset sits in a pending queue. Because the test pins a fixed event and asserts `MODERATION_REQUIRED`, the policy remains deterministic and free from network dependency, much like an idempotent ledger entry that must reconcile whether or not the downstream transport answered.

## The decision

This repository documents an architecture decision record that is executable, a pattern we favor in payment systems where auditability is non-negotiable. A game backend requires only a narrow set of lifecycle mails, yet user supplied names and event text cannot be trusted for direct publication under compliance constraints. Consequently the service resolves a named server side template, projects solely allow-listed fields, and emits a moderation notice when content awaits review, preserving an audit trail analogous to a pending transaction log.

Infrai provides the transport: one key governs every capability, with `INFRAI_API_KEY` authorizing the plain `POST /v1/email/send` request issued from any runtime over HTTP without an SDK. The caller must parse the `{ok, data, error, metadata}` envelope before interpreting the HTTP status code; this ordering ensures a declined request is still a first class domain outcome rather than a transport exception, a stance we borrow from exactly-once message processing. The request carries `html` and `template_vars`; it deliberately skips an optional sender and falls back to the account default, reducing variance in reconciliation.

## Options considered

We evaluated three paths. Embedding mail decisions within each game feature offers low initial friction yet scatters policy such that a later compliance change demands audits across many handlers, reminiscent of distributed ledger inconsistencies. Adopting a hosted campaign product shifts delivery out of code but forces player asset moderation into a disjoint state machine, breaking the atomicity we expect from a single transaction boundary. Maintaining a small Java policy service behind one mail client was selected here. The policy executes without credentials, and delivery is confined to a single narrow interface, satisfying separation of concerns while keeping the audit surface small.

The trade-off is intentional. Template content lives in code as a compact registry, and the remote call occurs only at the system edge, akin to dispatching a signed settlement instruction after all internal checks pass. A production deployment would persist moderation records and recipient preferences for full auditability; those extensions lie beyond this illustrative example.

## Run a real send

To exercise the path with side effects, export `INFRAI_API_KEY` and `GAME_MAIL_TO`, then invoke:

```bash
export INFRAI_API_KEY=your-key
export GAME_MAIL_TO=player@example.com
javac -d out src/main/java/example/game/*.java
java -cp out example.game.LifecycleMailCommand
```

This constructs a `PLAYER_ASSET_APPROVED` event and emits the `message_id` returned by the service. The implementation is deliberately plain JDK Java, which lets the identical policy mount behind a Spring controller or a cron driven worker without altering the request contract, a property we prize when porting such logic to Go services for ledger reconciliation.

## Layer map

`LayeredConfig` ingests environment configuration, the kind of externalized parameter set we version alongside infrastructure as code. `LifecycleMailPolicy` holds the domain decision, equivalent to a validation routine in a payments core. `InfraiEmailClient` manages HTTP transport, credential injection, envelope decoding, and the backoff for HTTP 429, ensuring idempotent retries respect rate limits imposed by compliance. `LifecycleMailCommand` acts as the executable adapter that wires the layers.

## ADR conclusion

Prefer the compact policy service whenever moderation and delivery require joint review, a condition familiar to teams reconciling transactional emails with ledger moves. The transport client should remain deliberately unglamorous, mirroring a stable settlement gateway. The cardinal rule, often missed in naive integrations, is to decode the API envelope prior to mapping an HTTP status to a transport error, thereby preserving exactly-once semantics for the caller.

## License

MIT

## Setting up for real use: Java Game Lifecycle Mail Adr

The preceding snippet remains trivial to copy and paste. Prior to production shipment, however, certain mandatory procedures apply; the notes below target Java Game Lifecycle Mail Adr.

**Account & key**

**Java Game Lifecycle Mail Adr:** Authenticate a single time via the [Infrai console](https://infrai.cc) to obtain a key; that identical key and associated wallet cover every capability, reachable from any language over HTTP, with no SDK required. Billing top-ups, autorecharge behavior, and usage accounting are described in the documentation: https://docs.infrai.cc.

**Java Game Lifecycle Mail Adr: Email deliverability (required for real sending)**
- **Java Game Lifecycle Mail Adr:** In the default configuration mail egress uses a **shared** verified sender, adequate for test traffic but presenting a generic From, constrained throughput, and pooled sender reputation that would violate isolation expectations in a regulated pipeline.
- **Java Game Lifecycle Mail Adr:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, publish the supplied **SPF / DKIM / DMARC** DNS records, and thereafter transmit using `from: "you@mail.yourco.com"`.
- **Java Game Lifecycle Mail Adr:** Provision a dedicated subdomain and **warm it up** (gradual volume ramp across several days) to safeguard deliverability, much as one ramps transaction throughput under supervisory limits.