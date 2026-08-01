package com.medtrack.mobile.architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.readLines
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainBoundaryTest {
    @Test
    fun `domain does not import framework or data layer`() {
        val domainRoot = listOf(
            Path.of("src/main/java/com/medtrack/mobile/domain"),
            Path.of("app/src/main/java/com/medtrack/mobile/domain"),
        ).firstOrNull { Files.isDirectory(it) }
        requireNotNull(domainRoot) { "Diretorio da camada domain nao encontrado" }
        val forbiddenPrefixes = listOf(
            "import android.",
            "import androidx.",
            "import com.medtrack.mobile.data.",
            "import okhttp3.",
            "import retrofit2.",
        )

        val domainFiles = Files.walk(domainRoot).use { paths ->
            paths.filter { it.isRegularFile() && it.extension == "kt" }
                .toList()
        }
        val violations = domainFiles.flatMap { file ->
            file.readLines()
                .filter { line -> forbiddenPrefixes.any(line::startsWith) }
                .map { line -> "${domainRoot.relativize(file)}: $line" }
        }

        assertTrue("Imports proibidos no dominio:\n${violations.joinToString("\n")}", violations.isEmpty())
    }
}
