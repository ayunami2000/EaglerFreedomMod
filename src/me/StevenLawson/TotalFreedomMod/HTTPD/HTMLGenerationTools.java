package me.StevenLawson.TotalFreedomMod.HTTPD;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class HTMLGenerationTools
{
    private HTMLGenerationTools()
    {
        throw new AssertionError();
    }

    public static String paragraph(String data)
    {
        return "<p>" + escapeHtml(data) + "</p>\r\n";
    }

    public static String heading(String data, int level)
    {
        return "<h" + level + ">" + escapeHtml(data) + "</h" + level + ">\r\n";
    }

    public static <K, V> String list(Map<K, V> map)
    {
        StringBuilder output = new StringBuilder();

        output.append("<ul>\r\n");

        Iterator<Map.Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<K, V> entry = it.next();
            output.append("<li>").append(escapeHtml(entry.getKey().toString() + " = " + entry.getValue().toString())).append("</li>\r\n");
        }

        output.append("</ul>\r\n");

        return output.toString();
    }

    public static <T> String list(Collection<T> list)
    {
        StringBuilder output = new StringBuilder();

        output.append("<ul>\r\n");

        for (T entry : list)
        {
            output.append("<li>").append(escapeHtml(entry.toString())).append("</li>\r\n");
        }

        output.append("</ul>\r\n");

        return output.toString();
    }
}
