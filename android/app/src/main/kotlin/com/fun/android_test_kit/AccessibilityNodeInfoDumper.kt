package com.`fun`.android_test_kit


import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.util.Xml
import android.view.accessibility.AccessibilityNodeInfo
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.StringWriter
import java.util.UUID
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 *
 * @hide
 */
object AccessibilityNodeInfoDumper {
    private val LOGTAG = AccessibilityNodeInfoDumper::class.java.simpleName
    private val NAF_EXCLUDED_CLASSES = arrayOf(android.widget.GridView::class.java.name, android.widget.GridLayout::class.java.name, android.widget.ListView::class.java.name, android.widget.TableLayout::class.java.name)
    /**
     * Using [AccessibilityNodeInfo] this method will walk the layout hierarchy
     * and generates an xml dump to the location specified by `dumpFile`
     * @param root The root accessibility node.
     * @param rotation The rotaion of current display
     * @param width The pixel width of current display
     * @param height The pixel height of current display
     */
    fun dumpWindowXmlString(root: AccessibilityNodeInfo?, rotation: Int,
                            width: Int, height: Int): String? {
        if (root == null) {
            return null
        }
        val startTime = SystemClock.uptimeMillis()
        val stringWriter = StringWriter()

        try {
            val serializer = Xml.newSerializer()
            serializer.setOutput(stringWriter)
            serializer.startDocument("UTF-8", true)
            serializer.startTag("", "hierarchy")
            serializer.attribute("", "rotation", Integer.toString(rotation))
            dumpNodeRec(root, serializer, 0, width, height, false)
            serializer.endTag("", "hierarchy")
            serializer.endDocument()

        } catch (e: IOException) {
            Log.e(LOGTAG, "failed to dump window to file", e)
        }


        val endTime = SystemClock.uptimeMillis()
        Log.w(LOGTAG, "Fetch time: " + (endTime - startTime) + "ms")
        return stringWriter.toString()
    }

    fun filterEmoji(source: String?): String? {
        if (source != null) {
            val regEx = "[\\p{P}\\p{Z}a-zA-Z0-9\\u4e00-\\u9fa5]"
            val p = Pattern.compile(regEx)
            val m = p.matcher(source)
            val sb = StringBuffer()
            while (m.find()) {
                sb.append(m.group())
            }
            return sb.toString()
        }
        return source
    }

    @Throws(IOException::class)
    fun dumpNodeRec(node: AccessibilityNodeInfo, serializer: XmlSerializer, index: Int,
                            width: Int, height: Int, skipNext: Boolean) {
        serializer.startTag("", "node")
        if (!nafExcludedClass(node) && !nafCheck(node))
            serializer.attribute("", "NAF", java.lang.Boolean.toString(true))
        serializer.attribute("", "index", Integer.toString(index))
        serializer.attribute("", "text", safeCharSeqToString(node.text))
        serializer.attribute("", "resource-id", safeCharSeqToString(node.viewIdResourceName))
        serializer.attribute("", "class", safeCharSeqToString(node.className))
        serializer.attribute("", "package", safeCharSeqToString(node.packageName))
        serializer.attribute("", "content-desc", safeCharSeqToString(node.contentDescription))
        serializer.attribute("", "checkable", java.lang.Boolean.toString(node.isCheckable))
        serializer.attribute("", "checked", java.lang.Boolean.toString(node.isChecked))
        serializer.attribute("", "clickable", java.lang.Boolean.toString(node.isClickable))
        serializer.attribute("", "enabled", java.lang.Boolean.toString(node.isEnabled))
        serializer.attribute("", "focusable", java.lang.Boolean.toString(node.isFocusable))
        serializer.attribute("", "focused", java.lang.Boolean.toString(node.isFocused))
        serializer.attribute("", "scrollable", java.lang.Boolean.toString(node.isScrollable))
        serializer.attribute("", "long-clickable", java.lang.Boolean.toString(node.isLongClickable))
        serializer.attribute("", "password", java.lang.Boolean.toString(node.isPassword))
        serializer.attribute("", "selected", java.lang.Boolean.toString(node.isSelected))
        serializer.attribute("", "bounds", AccessibilityNodeInfoHelper.getVisibleBoundsInScreen(
                node, width, height).toShortString())


        val eid = node.hashCode().toString();
        serializer.attribute("", "element-id", eid);

        if(!MainActivity.knowElements.containsKey(node.hashCode().toString())){
            MainActivity.knowElements.put(node.hashCode().toString(), node);
        }else{
            Log.d("MainActivityDump", "hit");
        }

        if(!skipNext) {
            val count = node.childCount
            for (i in 0 until count) {
                val child = node.getChild(i)
                if (child != null) {
                    if (child.isVisibleToUser) {
                        dumpNodeRec(child, serializer, i, width, height, false)
                        // shit happened ?
                        //child.recycle()
                    } else {
                        Log.i(LOGTAG, String.format("Skipping invisible child: %s", child.toString()))
                    }
                } else {
                    Log.i(LOGTAG, String.format("Null child %d/%d, parent: %s",
                            i, count, node.toString()))
                }
            }
        }
        serializer.endTag("", "node")
    }

    /**
     * The list of classes to exclude my not be complete. We're attempting to
     * only reduce noise from standard layout classes that may be falsely
     * configured to accept clicks and are also enabled.
     *
     * @param node
     * @return true if node is excluded.
     */
    private fun nafExcludedClass(node: AccessibilityNodeInfo): Boolean {
        val className = safeCharSeqToString(node.className)
        for (excludedClassName in NAF_EXCLUDED_CLASSES) {
            if (className.endsWith(excludedClassName))
                return true
        }
        return false
    }

    /**
     * We're looking for UI controls that are enabled, clickable but have no
     * text nor content-description. Such controls configuration indicate an
     * interactive control is present in the UI and is most likely not
     * accessibility friendly. We refer to such controls here as NAF controls
     * (Not Accessibility Friendly)
     *
     * @param node
     * @return false if a node fails the check, true if all is OK
     */
    private fun nafCheck(node: AccessibilityNodeInfo): Boolean {
        val isNaf = (node.isClickable && node.isEnabled
                && safeCharSeqToString(node.contentDescription).isEmpty()
                && safeCharSeqToString(node.text).isEmpty())
        return if (!isNaf) true else childNafCheck(node)
        // check children since sometimes the containing element is clickable
        // and NAF but a child's text or description is available. Will assume
        // such layout as fine.
    }

    /**
     * This should be used when it's already determined that the node is NAF and
     * a further check of its children is in order. A node maybe a container
     * such as LinerLayout and may be set to be clickable but have no text or
     * content description but it is counting on one of its children to fulfill
     * the requirement for being accessibility friendly by having one or more of
     * its children fill the text or content-description. Such a combination is
     * considered by this dumper as acceptable for accessibility.
     *
     * @param node
     * @return false if node fails the check.
     */
    private fun childNafCheck(node: AccessibilityNodeInfo): Boolean {
        val childCount = node.childCount
        for (x in 0 until childCount) {
            val childNode = node.getChild(x)
            if (!safeCharSeqToString(childNode.contentDescription).isEmpty() || !safeCharSeqToString(childNode.text).isEmpty())
                return true
            if (childNafCheck(childNode))
                return true
        }
        return false
    }

    private fun safeCharSeqToString(cs: CharSequence?): String {
        return cs?.let { stripInvalidXMLChars(it) } ?: ""
    }

    private fun stripInvalidXMLChars(cs: CharSequence): String {
        val ret = StringBuffer()
        var ch: Char
        /* http://www.w3.org/TR/xml11/#charsets
        [#x1-#x8], [#xB-#xC], [#xE-#x1F], [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF],
        [#x1FFFE-#x1FFFF], [#x2FFFE-#x2FFFF], [#x3FFFE-#x3FFFF],
        [#x4FFFE-#x4FFFF], [#x5FFFE-#x5FFFF], [#x6FFFE-#x6FFFF],
        [#x7FFFE-#x7FFFF], [#x8FFFE-#x8FFFF], [#x9FFFE-#x9FFFF],
        [#xAFFFE-#xAFFFF], [#xBFFFE-#xBFFFF], [#xCFFFE-#xCFFFF],
        [#xDFFFE-#xDFFFF], [#xEFFFE-#xEFFFF], [#xFFFFE-#xFFFFF],
        [#x10FFFE-#x10FFFF].
         */
        for (i in 0 until cs.length) {
            ch = cs[i]
            if (ch.toInt() >= 0x1 && ch.toInt() <= 0x8 || ch.toInt() >= 0xB && ch.toInt() <= 0xC || ch.toInt() >= 0xE && ch.toInt() <= 0x1F ||
                    ch.toInt() >= 0x7F && ch.toInt() <= 0x84 || ch.toInt() >= 0x86 && ch.toInt() <= 0x9f ||
                    ch.toInt() >= 0xFDD0 && ch.toInt() <= 0xFDDF || ch.toInt() >= 0x1FFFE && ch.toInt() <= 0x1FFFF ||
                    ch.toInt() >= 0x2FFFE && ch.toInt() <= 0x2FFFF || ch.toInt() >= 0x3FFFE && ch.toInt() <= 0x3FFFF ||
                    ch.toInt() >= 0x4FFFE && ch.toInt() <= 0x4FFFF || ch.toInt() >= 0x5FFFE && ch.toInt() <= 0x5FFFF ||
                    ch.toInt() >= 0x6FFFE && ch.toInt() <= 0x6FFFF || ch.toInt() >= 0x7FFFE && ch.toInt() <= 0x7FFFF ||
                    ch.toInt() >= 0x8FFFE && ch.toInt() <= 0x8FFFF || ch.toInt() >= 0x9FFFE && ch.toInt() <= 0x9FFFF ||
                    ch.toInt() >= 0xAFFFE && ch.toInt() <= 0xAFFFF || ch.toInt() >= 0xBFFFE && ch.toInt() <= 0xBFFFF ||
                    ch.toInt() >= 0xCFFFE && ch.toInt() <= 0xCFFFF || ch.toInt() >= 0xDFFFE && ch.toInt() <= 0xDFFFF ||
                    ch.toInt() >= 0xEFFFE && ch.toInt() <= 0xEFFFF || ch.toInt() >= 0xFFFFE && ch.toInt() <= 0xFFFFF ||
                    ch.toInt() >= 0x10FFFE && ch.toInt() <= 0x10FFFF)
                ret.append(".")
            else
                ret.append(ch)
        }
        return ret.toString()
    }
}