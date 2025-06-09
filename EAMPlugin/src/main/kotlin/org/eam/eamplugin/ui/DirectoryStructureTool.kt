package com.example.directorychecker.ui

import com.example.directorychecker.DirectoryStructureChecker
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * 目录结构检查结果工具窗口
 */
object DirectoryStructureTool {
    const val TOOL_WINDOW_ID = "Directory Structure Checker"

    private var tableModel: DefaultTableModel? = null
    private var resultTable: JTable? = null
    private var pathLabel: JLabel? = null

    /**
     * 显示检查结果
     */
    fun showResults(project: Project, issues: List<DirectoryStructureChecker.DirectoryStructureIssue>, checkedPath: String) {
        val toolWindowManager = com.intellij.openapi.wm.ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID) ?: return

        // 更新路径标签
        pathLabel?.text = "检查路径: $checkedPath"

        // 更新表格数据
        tableModel?.setRowCount(0)
        for (issue in issues) {
            tableModel?.addRow(
                arrayOf(
                    issue.severity.toString(),
                    issue.path,
                    issue.message
                )
            )
        }

        // 显示工具窗口
        toolWindow.show()
    }

    /**
     * 工具窗口工厂
     */
    class Factory : ToolWindowFactory {
        override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
            // 创建表格模型
            tableModel = DefaultTableModel(
                arrayOf("严重程度", "路径", "问题描述"),
                0
            )

            // 创建表格
            resultTable = JTable(tableModel)
            resultTable?.autoCreateRowSorter = true

            // 创建滚动面板
            val scrollPane = JScrollPane(resultTable)

            // 创建路径标签
            pathLabel = JLabel("检查路径: ")

            // 创建面板
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
            panel.add(pathLabel)
            panel.add(scrollPane)

            // 添加内容到工具窗口
            val contentFactory = ContentFactory.getInstance()
            val content = contentFactory.createContent(panel, "", false)
            toolWindow.contentManager.addContent(content)
        }
    }
}
