package com.buylan.cryst.util

import android.content.Context
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IThemeSource

fun textEditorStartup(context: Context, isThemeDark: Boolean) {
    try {
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(context.assets)
        )
        GrammarRegistry.getInstance().loadGrammars("languages.json")

        setEditorTheme(isThemeDark)
    } catch (e: Exception) {
        println(e)
    }
}

fun setEditorTheme(isDarkTheme: Boolean) {
    val name = if (!isDarkTheme) "2026-light" else "2026-dark"
    val themeAssetsPath = "textmate/theme/$name.json"
    ThemeRegistry.getInstance().loadTheme(
        ThemeModel(
            IThemeSource.fromInputStream(
                FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath),
                themeAssetsPath,
                null
            ),
            name
        ).apply {
            isDark = isDarkTheme
        }
    )
}