# SELLO-077 — Make the sandbox scenario replay tests reliable

The sandbox replay tests no longer fail intermittently: a restarted runner relisting its history overwrote the result of a step already taken. Both tests passed 20 runs in a row.
