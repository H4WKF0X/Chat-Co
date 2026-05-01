# Chat-Co GUI Implementation Plan

## Phase 1 — Model & Stub Layer

### New enums
| Enum | Values |
|---|---|
| `UserStatus` | `ACTIVE`, `INACTIVE`, `AWAY` |
| `ParticipantStatus` | `INVITED`, `ACCEPTED`, `DECLINED` |
| `UserRole` | `ADMINISTRATOR`, `MITARBEITER`, `GAST` |

### New records
| Record | Fields |
|---|---|
| `Room` | `Long id, String name, int capacity, String location` |
| `Meeting` | `Long id, String title, String description, OffsetDateTime startAt, OffsetDateTime endAt, String locationOrLink, Room room, Conversation conversation` |
| `MeetingParticipant` | `Meeting meeting, AppUser user, ParticipantStatus status` |

### Updated records
| Record | Addition |
|---|---|
| `AppUser` | `UserStatus status`, `UserRole role` |
| `Message` | `OffsetDateTime deletedAt` |
| `FileAttachment` | `String storedName`, `String storagePath`, `AppUser uploadedBy` |

### New service interfaces + stubs
- `RoomService` — `getAll()`, `findById(Long)`
- `MeetingService` — `getAll()`, `findById(Long)`, `getByUser(Long)`, `create(...)`
- `StubDataStore` extended: 3 rooms, 4 meetings with participants, each meeting linked to its own conversation

---

## Phase 2 — Main Shell Rework

### Rail (MainLayout)
- Icon buttons for: **Chat**, **Meetings**, **Admin**
- Active icon highlighted with accent colour
- Settings gear pinned at the bottom
- Admin icon only rendered when `currentUser.role == ADMINISTRATOR` (MAX only in stubs)

### Sidebar (SidebarComponent)
- **"+" button** next to each section title (Channels, Groups, Direct Messages)
- **Unread badge** on each nav item (stub: static numbers from StubDataStore)
- User footer is now **clickable** → navigates to Settings view

---

## Phase 3 — Conversation View Improvements

| Feature | Detail |
|---|---|
| Own message highlight | Messages where `sender == currentUser` get `cc-msg--own` CSS class (subtle tinted background) |
| Reply strip | When `replyTo != null`, a quoted block is shown above the message (sender name + first ~80 chars) |
| Date separators | Divider line between messages from different days: "Today", "Yesterday", or the date |
| Message hover menu | Appears on hover for **own messages only**: Edit + Delete buttons |
| Edit message | Replaces message content with an inline text field; Save / Cancel buttons |
| Delete message | Sets `deletedAt`; message renders as greyed italic "[Message deleted]" |
| Members panel | Slides in from the right when "Members" button in header is clicked; lists all conversation members with avatar, name, status dot; own user shows "(You)"; clicking a member opens User Detail Dialog |

---

## Phase 4 — Dialogs & Overlays

### User Detail Dialog
Triggered by clicking any user name or avatar anywhere in the app.
- Large avatar (initials), display name, `@username`, email, status chip
- **"Send Message"** button → navigates to existing DM or stub-creates one

### New DM Dialog
Triggered by "+" next to Direct Messages in sidebar.
- Live search through all users as you type
- Click a user → navigate to existing DM or stub-create + navigate

### New Channel Dialog
Triggered by "+" next to Channels.
- Channel name field, description textarea, private toggle
- Submit → stub-creates `Conversation(CHANNEL)` and navigates to it

### New Group Dialog
Triggered by "+" next to Groups.
- Group name field, multi-user search/select
- Submit → stub-creates `Conversation(GROUP)` and navigates to it

---

## Phase 5 — Settings View

Route: `settings`  
Accessed from: gear icon in rail OR clicking the user footer.

### Profile tab
- Avatar placeholder + "Change photo" stub button
- Display name (editable text field)
- Username and email (read-only, greyed)
- Status dropdown: Active / Away / Inactive
- Save button → shows success toast (stub)

### Appearance tab
- Dark / Light theme toggle (actually switches Lumo variant)
- 6 accent colour swatches (changes `--cc-accent` CSS variable on the page)

### Notifications tab
- Toggle: Sound on new message (stub)
- Toggle: Desktop notifications (stub)

### Account tab
- "Change Password" button → opens stub dialog
- "Deactivate Account" danger button → opens confirmation dialog

---

## Phase 6 — Meetings View

Route: `meetings`  
Accessed via Meetings icon in the rail.

### Layout
- Page header "Meetings" + "New Meeting" button
- Two tabs: **Meetings** | **Rooms**

### Meetings tab
- Upcoming section (expanded) and Past section (collapsed by default)
- Each **meeting card** shows: title, time range, room name or "No room", up to 4 participant avatars + overflow count, own invite status chip (Invited / Accepted / Declined)
- Clicking a card opens the **Meeting Detail Dialog**

### Meeting Detail Dialog
- Title, description, date + time range, location or link, room name
- Participant list: avatar + name + status chip; own user marked "(You)"
- **"Open Chat"** button → navigates to the meeting's linked conversation
- **"Accept" / "Decline"** buttons for current user's invite (stub: updates status in StubDataStore)
- Organiser-only: Edit button (opens edit form), Cancel button (confirmation dialog)

### New Meeting Dialog
- Title, description, start + end datetime pickers, location or link field
- Room dropdown (optional, from RoomService)
- Participant multi-select search
- Submit → stub-creates Meeting + MeetingParticipants with INVITED status

### Rooms tab
- Card grid (2–3 columns per row)
- Each **room card**: name, capacity badge, location, availability indicator
- **"Book"** button → opens New Meeting Dialog pre-filled with that room

---

## Phase 7 — Admin Dashboard

Route: `admin`  
Rail icon only shown when `currentUser.role == ADMINISTRATOR`.

### Users tab
- Table: username, display name, email, role, status, created date
- Row actions: toggle active/inactive, Edit (role dropdown + status dropdown in dialog)
- "Add User" button → stub dialog (username, display name, email, role)
- "Export CSV" button → stub toast

### Channels tab
- Table: name, type, member count, creator, created date
- Row actions: Archive, Delete (with confirmation dialog)

### System tab
- **Logs section**: styled log panel (placeholder, not connected)
- **Backup section**: last backup timestamp placeholder + "Create Backup" button (progress-style toast)

---

## CSS Additions (all in `styles.css`)

| Class | Purpose |
|---|---|
| `cc-msg--own` | Tinted background for own messages |
| `cc-msg-reply-strip` | Quoted reply block above a message |
| `cc-date-separator` | Day divider line + label |
| `cc-msg-deleted` | Greyed italic "[Message deleted]" |
| `cc-member-panel` | Right-side slide-in members panel |
| `cc-rail-btn`, `cc-rail-btn--active` | Rail icon button states |
| `cc-meeting-card`, `cc-room-card` | Meeting and room card layouts |
| `cc-status-chip` | Invite status chip (Invited / Accepted / Declined) |
| `cc-settings-*` | Settings view layout classes |
| `cc-admin-*` | Admin dashboard layout classes |

---

## Not implemented (stubs / placeholders only)

- Real file upload (button present, shows "not implemented" toast)
- Emoji picker (button present, shows toast)
- Real-time WebSocket message push
- LDAP authentication / login screen
- Actual CSV file download
- Avatar photo upload
- Desktop notifications / sounds