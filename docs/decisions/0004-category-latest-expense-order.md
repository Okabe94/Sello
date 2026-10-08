# 0004 — Category recency follows expense additions

**Status:** Accepted product policy; implementation pending · **Date:** 2026-10-08
**Owner/approver:** project owner (user) · **Ticket:** SELLO-001

## Decision and alternatives

During O01–O08 review, the owner changed the latest-activity option from category/
budget configuration modification to the category most recently receiving an
expense through addition. Expense edits are not additions. This supersedes the
old O06 configuration-time expectation; other approved ordering rules remain.

Rejected: keeping configuration modification as this metric or treating any edit
as a new expense. Latest addition must not be inferred from mutable updatedAt.

## Approved comparator and consequences

[D06](0001-mvp-contract.md#d06--ordering) owns the approved refinement, confirmed by
the user on 2026-10-08: Último gasto agregado uses selected-month nondeleted
expenses and each category's newest original real creation Instant, not its newest
effective date. Backdated additions count in their effective month; edits and
restore preserve original addition times. Deleting the newest expense falls back
to the next surviving addition. Categories without eligible expenses stay last
within their finite/unlimited partition in either direction. Reverse comparison
of the newest-per-category metric, not selection of the oldest expense. Existing
alphabetical/stable-ID ties and unlimited-last rules are unchanged.

SELLO-023 must test O01–O14 addition/edit/backdating/delete/restore/empty behavior,
reversible direction, stable ties and unlimited-last. Financial clock stepping is
not a new addition; fixed-scope fixtures must prove this without using live time.
No code/schema/backup changes or independent review have occurred by recording
this direction. Persistence/portable timestamp impacts belong to dependent tickets.
