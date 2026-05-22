package com.buylan.cryst.util

import android.content.Context
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import org.eclipse.tm4e.core.registry.IThemeSource

fun textEditorStartup(context: Context, isThemeDark: Boolean) {

    //edited form sora editor

    try {
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(context.assets)
        )

        val themeRegistry = ThemeRegistry.getInstance()
        val name = "2026-light" // 主题名称
        val themeAssetsPath = "textmate/theme/$name.json"
        themeRegistry.loadTheme(
            ThemeModel(
                IThemeSource.fromInputStream(
                    FileProviderRegistry.getInstance().tryGetInputStream(themeAssetsPath),
                    themeAssetsPath,
                    null
                ),
                name
            ).apply {
                isDark = isThemeDark
            }
        )
        GrammarRegistry.getInstance().loadGrammars("languages.json")
    } catch (e: Exception) {
        println(e)
    }
}