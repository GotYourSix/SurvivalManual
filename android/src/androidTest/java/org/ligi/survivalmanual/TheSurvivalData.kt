package org.ligi.survivalmanual

import android.support.test.InstrumentationRegistry
import junit.framework.Assert.fail
import org.assertj.core.api.Assertions.assertThat
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser
import org.junit.Test
import org.ligi.survivalmanual.model.NavigationEntryMap
import org.ligi.survivalmanual.model.SurvivalContent
import org.ligi.survivalmanual.ui.PRODUCT_MAP

class TheSurvivalData {

    val survivalContent = SurvivalContent(InstrumentationRegistry.getTargetContext().assets)


    @Test
    fun weCanLoadAllEntriesFromNavigation() {
        NavigationEntryMap.forEach {
            val url = it.entry.url
            val tested = survivalContent.getMarkdown(url)
            if (tested == null) {
                fail("could not load $url")
            }

            val flavour = CommonMarkFlavourDescriptor()
            val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(tested!!)

            val elementCollectingVisitor = ElementCollectingVisitor(MarkdownElementTypes.LINK_DESTINATION)
            elementCollectingVisitor.visitNode(parsedTree)

            val unresolvedLinks = elementCollectingVisitor.elementList.map { it.getTextInNode(tested).toString() }.filter { !it.startsWith("#") }
                    .filter { !it.startsWith("http") }
                    .filter { !survivalContent.hasFile(it)}
                    .filter { survivalContent.getMarkdown(it) == null }
                    .filter { !PRODUCT_MAP.containsKey(it) }

            if(unresolvedLinks.isNotEmpty()) {
                fail("unresolved links in $url:$unresolvedLinks")
            }
        }

    }

    class ElementCollectingVisitor(val type: IElementType) : RecursiveVisitor() {

        var elementList = mutableListOf<ASTNode>()

        override fun visitNode(node: ASTNode) {
            if (node.type == type) {
                elementList.add(node)
            }
            super.visitNode(node)
        }

    }


    @Test
    fun weGetNullForUnknownURL() {
        val tested = survivalContent.getMarkdown("YOLO")
        assertThat(tested).isNull()
    }

}
