# SELLO-087 — Store goals and their movements

Goals are stored (schema 4) as their recorded history, with commands, receipts, the shared undo, a 10-second Deshacer after completion and reads; `GoalPolicy` works out saved, %, stamps and pace. No screen uses them, and backups, restore and erase-all do not include goals until SELLO-088.
