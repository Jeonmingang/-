# UltimatePixelmonRating v1.7.0 – Migration Notes

This drop replaces broken sources (ellipsis `...` fragments) with a clean, Java 8–compatible implementation for 1.16.5 / CatServer.

## What changed
- **Commands fixed**
  - `/레이팅 참가`, `/레이팅 취소`, `/레이팅 전적 [닉]`
  - `/레이팅 아레나 생성 <이름>`
  - `/레이팅 아레나 스폰 <이름> a|b`
  - `/레이팅 아레나 활성화 <이름> <true|false>`

- **Arena storage**
  - `plugins/UltimatePixelmonRating/arenas.yml` with `arenas: {}` schema.
  - `arena-required: true` by default (set `false` to use `stage-location`).

- **Matchmaking**
  - Simple queue: pairs first two players and calls `/pokebattle <p1> <p2>`.

- **Battle result detection**
  - Packet hook via ProtocolLib (optional). Falls back to chat event.
  - Korean patterns added (see `config.yml > detection`).

- **Elo**
  - Classic Elo with K-factor brackets, YAML player store `players.yml`.

## How to migrate
1. Stop server.
2. Backup old plugin folder.
3. Drop the built JAR from this project (after `mvn package`).
4. Copy `config.yml` / `arenas.yml` if you need custom values.
5. Start server and create at least one arena:
   ```
   /레이팅 아레나 생성 A1
   /레이팅 아레나 스폰 A1 a
   /레이팅 아레나 스폰 A1 b
   /레이팅 아레나 활성화 A1 true
   ```

## Notes
- If you **don’t** install ProtocolLib, detection uses chat lines only and may miss actionbar/battle-log text. For best results, put ProtocolLib for 1.16.5 in `plugins/`.
- Patterns use the phrases from your screenshots. Adjust regex in `config.yml` for your language pack.

