// Top-level build file where you can add configuration options common to all subprojects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

tasks.register("qualityCheck") {
    group = "verification"
    description = "Executa format check, análise estática, lint, testes e cobertura do app."
    dependsOn(
        ":app:ktlintCheck",
        ":app:detekt",
        ":app:lintDebug",
        ":app:testDebugUnitTest",
        ":app:koverVerifyDebug",
        ":app:koverXmlReportDebug",
        ":app:koverHtmlReportDebug",
        "checkSecrets",
    )
}

tasks.register("checkSecrets") {
    group = "verification"
    description = "Verifica padrões comuns de segredos versionados em arquivos de texto."

    doLast {
        val patterns = mapOf(
            "private key" to Regex("-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
            "AWS access key" to Regex("AKIA[0-9A-Z]{16}"),
            "JWT" to Regex("eyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+"),
        )
        val textExtensions = setOf(
            "gradle",
            "kts",
            "kt",
            "java",
            "xml",
            "toml",
            "properties",
            "md",
            "yml",
            "yaml",
            "json",
        )
        val findings = fileTree(rootDir) {
            exclude(".git/**", ".gradle/**", ".idea/**", ".kotlin/**", "**/build/**", "local.properties")
        }.files
            .asSequence()
            .filter { it.isFile && it.extension in textExtensions }
            .flatMap { file ->
                val content = file.readText()
                patterns.asSequence()
                    .filter { (_, pattern) -> pattern.containsMatchIn(content) }
                    .map { (label, _) -> "${file.relativeTo(rootDir)}: possível $label" }
            }
            .toList()

        check(findings.isEmpty()) {
            "Possíveis segredos encontrados:\n${findings.joinToString("\n")}"
        }
    }
}
