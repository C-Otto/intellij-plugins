// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie

import com.intellij.grazie.jlanguage.Lang
import com.intellij.grazie.remote.GrazieRemote
import com.intellij.grazie.utils.ifTrue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.util.lang.UrlClassLoader
import org.languagetool.language.Language
import org.languagetool.language.Languages
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

object GrazieDynamic {
  private val myDynClassLoaders by lazy {
    for (file in dynamicFolder.walk().filter { file -> file.isFile && Lang.values().all { it.remote.file != file } }) {
      file.delete()
    }
    hashSetOf<ClassLoader>(
      UrlClassLoader.build()
        .parent(GraziePlugin.classLoader)
        .urls(GrazieRemote.allAvailableLocally().map { it.remote.file.toURI().toURL() }).get()
    )
  }

  fun addDynClassLoader(classLoader: ClassLoader) = myDynClassLoaders.add(classLoader)

  private val dynClassLoaders: Set<ClassLoader>
    get() = myDynClassLoaders.toSet()


  val dynamicFolder: File
    get() = File(PathManager.getSystemPath(), "grazie").apply {
      mkdir()
    }

  fun loadLang(lang: Lang): Language? {
    lang.remote.langsClasses.forEach { className ->
      (loadClass("org.languagetool.language.$className")?.newInstance() as Language?)?.let { Languages.add(it) }
    }
    return Languages.get().find { it::class.java.simpleName == lang.className }
  }

  fun loadClass(className: String): Class<*>? = forClassLoader {
    try {
      Class.forName(className, true, it)
    }
    catch (e: ClassNotFoundException) {
      null
    }
  }

  fun getResourceAsStream(path: String): InputStream? = forClassLoader { it.getResourceAsStream(path) } ?:
        (path.startsWith("/") && ApplicationManager.getApplication().isUnitTestMode)
          .ifTrue { forClassLoader { it.getResourceAsStream(path.drop(1)) } }

  fun getResource(path: String): URL? = forClassLoader { it.getResource(path) } ?:
        (path.startsWith("/") && ApplicationManager.getApplication().isUnitTestMode)
          .ifTrue { forClassLoader { it.getResource(path.drop(1)) } }

  fun getResourceBundle(baseName: String, locale: Locale) = forClassLoader {
    try {
      ResourceBundle.getBundle(baseName, locale, it).takeIf { bundle -> bundle.locale.language == locale.language }
    }
    catch (e: MissingResourceException) {
      null
    }
  }

  private fun <T : Any> forClassLoader(body: (ClassLoader) -> T?): T? = body(GraziePlugin.classLoader) ?: dynClassLoaders
    .asSequence()
    .mapNotNull {
      body(it)
    }.firstOrNull()
}
