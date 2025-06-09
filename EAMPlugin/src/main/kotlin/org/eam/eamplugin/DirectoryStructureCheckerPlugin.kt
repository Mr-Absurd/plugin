package com.example.directorychecker

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * 目录结构检查插件主类
 */
@Service(Service.Level.PROJECT)
class DirectoryStructureCheckerPlugin(private val project: Project) {
    private val checker = DirectoryStructureChecker()
    private val settings = DirectoryStructureSettings.getInstance(project)
    
    /**
     * 检查指定目录的结构是否符合规则
     */
    fun checkDirectory(root: VirtualFile): List<DirectoryStructureIssue> {
        val rule = settings.state.rule
        return checker.checkStructure(root, rule)
    }
    
    /**
     * 获取当前项目的根目录
     */
    fun getProjectRoot(): VirtualFile? {
        return project.baseDir
    }
}
