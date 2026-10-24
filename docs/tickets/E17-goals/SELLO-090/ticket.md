# SELLO-090 — Create, edit and delete goals

- **Type:** Task
- **Priority:** P1
- **Status:** Backlog
- **Depends on:** SELLO-089, SELLO-004
- **Gate:** G2

### Outcome
A person can create a goal with the Sello pad, edit it and delete it with confirmation.

### Deliverables
The create/edit screen with projected months only while editing, and deletion with a confirm slip.
Reuses `IconChoiceStrip`, `FormField`, `AmountKeypad`, `MilestoneSpec` captions, `DangerSlip` and `ConfirmSlip` ([gap list](../../../design/v2/gaps/metas.md)); adds the large icon circle beside the name and the pad that slides up over the form.

### Acceptance criteria
- Target amount uses the Sello pad; target date optional.
- Projected months state their assumed pace and use whole pesos.
- A completed goal can only be deleted.

### Tests
ViewModel and screen tests including process-death restoration; G2.

### Working checkpoint
A goal can be created, edited and deleted on a phone.

### Context and starting points
v2 create page; approved examples.

### Implementation plan
1. Create and edit.
2. Projection display.
3. Deletion.

### Concrete cases and pitfalls
- Never promise a completion date.

### Verification recipe
Focused tests, then `./scripts/verify-ticket SELLO-090 --retain`.
