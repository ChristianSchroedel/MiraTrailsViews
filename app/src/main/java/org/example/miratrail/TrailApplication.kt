package org.example.miratrail

import android.app.Application
import org.example.miratrail.data.InMemoryWalkRepository

class TrailApplication : Application() {
    val repository = InMemoryWalkRepository()
}
