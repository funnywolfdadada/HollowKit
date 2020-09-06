package com.funnywolf.plugin.classfilter

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author https://github.com/funnywolfdadada
 * @since 2020/9/6
 */
class ClassFilterPlugin: Plugin<Project> {

    override fun apply(p: Project) {
        p.extensions.getByType(AppExtension::class.java)
            .registerTransform(FilterTransform(p))
    }

}