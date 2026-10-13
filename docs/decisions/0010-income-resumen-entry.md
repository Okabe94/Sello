# 0010 — Working income entry point before full Resumen

**Ticket:** SELLO-021 · **Date:** 2026-10-13
**Status:** Product scope approved by the project owner

## Context and alternatives

The design opens income history from Resumen's Entró section. Full Resumen belongs
to SELLO-022. A temporary entry in Recibo or only in the income form would depart
from the design. A blank summary destination would violate truthful working navigation.

## Decision

The owner approved a minimal Resumen in SELLO-021: shared month selection, the real
exact income total, loading/error/retry states and a working link to income history.
Do not populate unfinished summary sections with fixtures, zeroes or disabled controls.
The expense-first dock and Gasto / Ingreso entry choice remain unchanged in meaning.

## Consequences and verification

This is an explicit exception to waiting for the complete Resumen before exposing
its tab. SELLO-022 still owns net flow, expense actuals, budgets, verdict and charts.
Recibo does not depend on income. Verify tab/back-stack restoration, shared month,
real income totals, read failures and navigation in host/device tests and SELLO-021 G2.
