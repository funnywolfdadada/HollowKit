package com.funnywolf.plugin.classfilter

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.ImmutableSet
import kotlinx.coroutines.*
import org.gradle.api.Project
import org.json.JSONObject
import java.io.File
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/6
 */
class FilterTransform(p: Project): Transform() {

    private val configFile = File(p.projectDir, "class_filter.json")

    private val configDeferred = if (configFile.exists()) {
        GlobalScope.async(Dispatchers.IO) {
            parseConfigFile(configFile)
        }
    } else {
        null
    }

    override fun getName(): String = "ClassFilter"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> = TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> = ImmutableSet.of(QualifiedContent.Scope.EXTERNAL_LIBRARIES)

    override fun isIncremental(): Boolean = false

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        if (configDeferred == null) {
            println("No config file ${configFile.absolutePath}")
            return
        }
        runBlocking {
            val configMap = configDeferred.await()
            if (configMap.isEmpty()) {
                println("Config is empty")
                return@runBlocking
            }
            val start = System.currentTimeMillis()
            println("$name start at ${start / 1000.0} -----------------------")
            val output = transformInvocation.outputProvider
            val entries = configMap.entries
            val deferred = ArrayList<Deferred<Any>>()
            for (input in transformInvocation.inputs) {
                for (jar in input.jarInputs) {
                    deferred.add(async(Dispatchers.Default) {
                        filterClass(
                            jar.file,
                            output.getContentLocation(jar.name, jar.contentTypes, jar.scopes, Format.JAR),
                            entries.firstOrNull { jar.name.startsWith(it.key) }?.value
                        )
                    })
                }
            }
            deferred.forEach {
                it.await()
            }
            println("$name end, cost ${(System.currentTimeMillis() - start) / 1000.0} -----------------------")
        }
    }

    private fun parseConfigFile(file: File): Map<String, List<String>> {
        val map = HashMap<String, ArrayList<String>>()
        val json = JSONObject(file.readText())
        for (key in json.keys()) {
            val array = json.optJSONArray(key) ?: continue
            val list = map[key] ?: ArrayList<String>().also {
                map[key] = it
            }
            for (i in 0 until array.length()) {
                list.add(array.optString(i) ?: continue)
            }
            if (list.isEmpty()) {
                map.remove(key)
            }
        }
        return map
    }

    private fun filterClass(input: File, output: File, filter: List<String>?) {
        val inputJar = JarInputStream(input.inputStream())
        val outputJar = JarOutputStream(output.outputStream())
        while (true) {
            val entry = inputJar.nextJarEntry ?: break
            if (filter?.firstOrNull { entry.name.startsWith(it) } != null) {
                println("Filter class ${entry.name}")
                continue
            }
            outputJar.putNextEntry(entry)
            inputJar.copyTo(outputJar)
            outputJar.closeEntry()
            inputJar.closeEntry()
        }

        outputJar.finish()
        outputJar.close()
        inputJar.close()
    }

}
