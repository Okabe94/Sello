package com.software.sello.domain

import android.os.Bundle

// Deliberate violation: demonstrates that a failing quality check blocks merging.
fun forbidden(): Bundle = Bundle()
