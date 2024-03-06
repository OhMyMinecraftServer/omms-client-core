# OMMS Client Core

OMMS Client Core provides a way to communicate
with [`OMMS Central Server`](https://github.com/OhMyMinecraftServer/omms-central)
via `Session API`

## Session API Implementation Status

- [ ] Whitelist Management
    - [x] Add, Remove Players
    - [x] Retrieve Whitelist from Server
    - [x] Query Player
    - [ ] Create/Delete Whitelists
- [ ] Controller Management
    - [x] Controller Console
    - [x] Controller Remote Command
    - [x] Controller Status
    - [ ] Create/Delete Controllers
- [ ] Announcement Management
    - [ ] Create/Delete Announcements
    - [x] Retrieve Announcements from Server
- [x] Chatbridge
    - [x] Chat History
    - [x] Send Chat Message
- [x] Server Management
    - [x] Server System Status (loadavg, memory usage, disk usage etc.)
    - [ ] Run Command
    - [ ] System Terminal
- [ ] OMMS Central Server Permission

### Compatibility

- Requires Java 8+
- Works on Android