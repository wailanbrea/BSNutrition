package com.bsnutrition.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NavigationTest {

    @Test
    fun topLevelDestinations_containsAllCanonicalTabs() {
        val destinations = TopLevelDestination.entries

        assertEquals(5, destinations.size)
        assertEquals("Hoy", TopLevelDestination.HOME.title)
        assertEquals("Diario", TopLevelDestination.DIARY.title)
        assertEquals("Registrar", TopLevelDestination.ADD.title)
        assertEquals("Progreso", TopLevelDestination.PROGRESS.title)
        assertEquals("Más", TopLevelDestination.MORE.title)
    }

    @Test
    fun topLevelDestinations_haveIconsAndDescriptions() {
        TopLevelDestination.entries.forEach { destination ->
            assertNotNull(destination.icon)
            assertNotNull(destination.contentDescription)
        }
    }
}
