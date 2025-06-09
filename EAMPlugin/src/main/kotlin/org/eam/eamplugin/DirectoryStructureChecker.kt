package com.example.directorychecker

import com.intellij.openapi.vfs.VirtualFile
import java.util.*

/**
 * 目录结构检查器 - 负责定义规则和检查逻辑
 */
class DirectoryStructureChecker {
    /**
     * 目录结构规则
     */
    data class Rule(
        val requiredDirectories: List<String> = listOf(),
        val forbiddenDirectories: List<String> = listOf(),
        val requiredFiles: Map<String, List<String>> = mapOf(),
        val forbiddenFiles: Map<String, List<String>> = mapOf(),
        val fileExtensions: Map<String, List<String>> = mapOf(),
        val directoryOrder: List<String> = listOf()
    )
    
    /**
     * 检查结果
     */
    data class DirectoryStructureIssue(
        val path: String,
        val message: String,
        val severity: Severity
    )
    
    enum class Severity { ERROR, WARNING, INFO }
    
    /**
     * 检查目录结构是否符合规则
     */
    fun checkStructure(root: VirtualFile, rule: Rule): List<DirectoryStructureIssue> {
        val issues = mutableListOf<DirectoryStructureIssue>()
        
        // 检查根目录下的必需目录
        for (requiredDir in rule.requiredDirectories) {
            if (!hasSubdirectory(root, requiredDir)) {
                issues.add(
                    DirectoryStructureIssue(
                        root.path,
                        "缺少必需目录: $requiredDir",
                        Severity.ERROR
                    )
                )
            }
        }
        
        // 检查根目录下的禁止目录
        for (forbiddenDir in rule.forbiddenDirectories) {
            if (hasSubdirectory(root, forbiddenDir)) {
                issues.add(
                    DirectoryStructureIssue(
                        root.path,
                        "存在禁止目录: $forbiddenDir",
                        Severity.ERROR
                    )
                )
            }
        }
        
        // 检查必需文件
        for ((dirPath, files) in rule.requiredFiles) {
            val dir = if (dirPath.isEmpty()) root else findSubdirectory(root, dirPath)
            if (dir != null) {
                for (file in files) {
                    if (!hasFile(dir, file)) {
                        issues.add(
                            DirectoryStructureIssue(
                                dir.path,
                                "缺少必需文件: $file",
                                Severity.ERROR
                            )
                        )
                    }
                }
            } else {
                issues.add(
                    DirectoryStructureIssue(
                        root.path,
                        "未找到目录: $dirPath",
                        Severity.ERROR
                    )
                )
            }
        }
        
        // 检查禁止文件
        for ((dirPath, files) in rule.forbiddenFiles) {
            val dir = if (dirPath.isEmpty()) root else findSubdirectory(root, dirPath)
            if (dir != null) {
                for (file in files) {
                    if (hasFile(dir, file)) {
                        issues.add(
                            DirectoryStructureIssue(
                                dir.path,
                                "存在禁止文件: $file",
                                Severity.ERROR
                            )
                        )
                    }
                }
            }
        }
        
        // 检查文件扩展名
        for ((dirPath, extensions) in rule.fileExtensions) {
            val dir = if (dirPath.isEmpty()) root else findSubdirectory(root, dirPath)
            if (dir != null) {
                for (child in dir.children) {
                    if (!child.isDirectory) {
                        val ext = child.extension?.toLowerCase(Locale.ROOT) ?: ""
                        if (extensions.isNotEmpty() && !extensions.contains(ext)) {
                            issues.add(
                                DirectoryStructureIssue(
                                    child.path,
                                    "不允许的文件扩展名: .$ext",
                                    Severity.WARNING
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // 检查目录顺序
        if (rule.directoryOrder.isNotEmpty()) {
            val actualOrder = root.children
                .filter { it.isDirectory }
                .map { it.name }
                .toList()
            
            val expectedOrder = rule.directoryOrder
            
            // 检查必需目录是否按顺序存在
            var lastIndex = -1
            for (dir in expectedOrder) {
                val index = actualOrder.indexOf(dir)
                if (index != -1) {
                    if (index < lastIndex) {
                        issues.add(
                            DirectoryStructureIssue(
                                root.path,
                                "目录顺序不正确: $dir 应该在 ${expectedOrder[lastIndex]} 之后",
                                Severity.WARNING
                            )
                        )
                    }
                    lastIndex = index
                }
            }
        }
        
        return issues
    }
    
    /**
     * 检查目录是否包含指定名称的子目录
     */
    private fun hasSubdirectory(dir: VirtualFile, name: String): Boolean {
        return dir.findChild(name)?.isDirectory == true
    }
    
    /**
     * 查找子目录（支持多级路径）
     */
    private fun findSubdirectory(root: VirtualFile, path: String): VirtualFile? {
        val parts = path.split("/")
        var current = root
        
        for (part in parts) {
            if (part.isEmpty()) continue
            
            val child = current.findChild(part)
            if (child == null || !child.isDirectory) {
                return null
            }
            current = child
        }
        
        return current
    }
    
    /**
     * 检查目录是否包含指定名称的文件
     */
    private fun hasFile(dir: VirtualFile, name: String): Boolean {
        return dir.findChild(name)?.isDirectory == false
    }
}
