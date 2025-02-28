package com.example.report;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KarateReportConsolidator {

    public static void consolidateReports(String karateReportsDir) throws IOException {
        // Get all HTML report files
        List<Path> reportFiles = Files.walk(Paths.get(karateReportsDir))
                .filter(path -> path.toString().endsWith(".html") && !path.getFileName().toString().equals("karate-summary.html"))
                .collect(Collectors.toList());
        
        // Parse the summary report first
        Path summaryPath = Paths.get(karateReportsDir, "karate-summary.html");
        Document summaryDoc = Jsoup.parse(new File(summaryPath.toString()), "UTF-8");
        
        // Create a new HTML document for the consolidated report
        Document consolidatedDoc = Jsoup.parse("<!DOCTYPE html><html><head><title>Consolidated Karate Report</title></head><body></body></html>");
        Element head = consolidatedDoc.head();
        Element body = consolidatedDoc.body();
        
        // Copy styles from summary
        Elements styles = summaryDoc.select("style");
        for (Element style : styles) {
            head.appendChild(style.clone());
        }
        
        // Add additional styling for the consolidated report
        Element additionalStyle = consolidatedDoc.createElement("style");
        additionalStyle.text(
            ".nav-tabs { margin-bottom: 20px; border-bottom: 1px solid #ddd; }" +
            ".nav-tabs > li { display: inline-block; margin-bottom: -1px; }" +
            ".nav-tabs > li > a { display: block; padding: 10px 15px; border: 1px solid transparent; border-radius: 4px 4px 0 0; text-decoration: none; }" +
            ".nav-tabs > li.active > a { color: #555; background-color: #fff; border: 1px solid #ddd; border-bottom-color: transparent; }" +
            ".tab-content > .tab-pane { display: none; }" +
            ".tab-content > .active { display: block; }" +
            "#search-box { margin: 20px 0; padding: 10px; }" +
            "#search-input { padding: 8px; width: 300px; }" +
            ".feature-link { margin-bottom: 5px; padding: 5px; display: block; }" +
            ".search-highlight { background-color: yellow; }"
        );
        head.appendChild(additionalStyle);
        
        // Add the summary section
        Element summarySection = consolidatedDoc.createElement("div");
        summarySection.attr("id", "summary");
        summarySection.addClass("container");
        
        // Copy the summary content
        Element summaryContent = summaryDoc.select("div.container").first();
        if (summaryContent != null) {
            summarySection.appendChild(summaryContent.clone());
        }
        
        body.appendChild(summarySection);
        
        // Add search functionality
        Element searchBox = consolidatedDoc.createElement("div");
        searchBox.attr("id", "search-box");
        searchBox.html("<input type='text' id='search-input' placeholder='Search across all tests...'>");
        body.appendChild(searchBox);
        
        // Create tabs for features
        Element tabsContainer = consolidatedDoc.createElement("div");
        tabsContainer.addClass("container");
        
        Element tabsList = consolidatedDoc.createElement("ul");
        tabsList.addClass("nav-tabs");
        
        Element tabContent = consolidatedDoc.createElement("div");
        tabContent.addClass("tab-content");
        
        // Process each feature report
        List<String> featureNames = new ArrayList<>();
        for (int i = 0; i < reportFiles.size(); i++) {
            Path reportPath = reportFiles.get(i);
            String featureName = reportPath.getFileName().toString().replace(".html", "");
            featureNames.add(featureName);
            
            Document featureDoc = Jsoup.parse(new File(reportPath.toString()), "UTF-8");
            
            // Create tab for this feature
            Element tabListItem = consolidatedDoc.createElement("li");
            if (i == 0) {
                tabListItem.addClass("active");
            }
            
            Element tabLink = consolidatedDoc.createElement("a");
            tabLink.attr("href", "#" + featureName);
            tabLink.attr("data-toggle", "tab");
            tabLink.text(featureName);
            tabListItem.appendChild(tabLink);
            tabsList.appendChild(tabListItem);
            
            // Create tab content for this feature
            Element tabPane = consolidatedDoc.createElement("div");
            tabPane.attr("id", featureName);
            tabPane.addClass("tab-pane");
            if (i == 0) {
                tabPane.addClass("active");
            }
            
            // Extract and include the feature content
            Element featureContent = featureDoc.select("div.container").first();
            if (featureContent != null) {
                tabPane.appendChild(featureContent.clone());
            }
            
            tabContent.appendChild(tabPane);
        }
        
        tabsContainer.appendChild(tabsList);
        tabsContainer.appendChild(tabContent);
        body.appendChild(tabsContainer);
        
        // Add JavaScript for tab switching and search
        Element script = consolidatedDoc.createElement("script");
        script.html(
            "document.addEventListener('DOMContentLoaded', function() {\n" +
            "  // Tab switching functionality\n" +
            "  var tabs = document.querySelectorAll('.nav-tabs li a');\n" +
            "  \n" +
            "  tabs.forEach(function(tab) {\n" +
            "    tab.addEventListener('click', function(e) {\n" +
            "      e.preventDefault();\n" +
            "      \n" +
            "      // Remove active class from all tabs\n" +
            "      document.querySelectorAll('.nav-tabs li').forEach(function(item) {\n" +
            "        item.classList.remove('active');\n" +
            "      });\n" +
            "      \n" +
            "      // Add active class to clicked tab\n" +
            "      this.parentElement.classList.add('active');\n" +
            "      \n" +
            "      // Hide all tab content\n" +
            "      document.querySelectorAll('.tab-pane').forEach(function(pane) {\n" +
            "        pane.classList.remove('active');\n" +
            "      });\n" +
            "      \n" +
            "      // Show clicked tab content\n" +
            "      var target = this.getAttribute('href').substring(1);\n" +
            "      document.getElementById(target).classList.add('active');\n" +
            "    });\n" +
            "  });\n" +
            "  \n" +
            "  // Search functionality\n" +
            "  var searchInput = document.getElementById('search-input');\n" +
            "  \n" +
            "  searchInput.addEventListener('keyup', function() {\n" +
            "    var searchTerm = this.value.toLowerCase();\n" +
            "    \n" +
            "    if (searchTerm.length < 3) {\n" +
            "      // Reset all highlighting\n" +
            "      document.querySelectorAll('.search-highlight').forEach(function(el) {\n" +
            "        var text = el.textContent;\n" +
            "        el.replaceWith(text);\n" +
            "      });\n" +
            "      return;\n" +
            "    }\n" +
            "    \n" +
            "    // Search all content\n" +
            "    document.querySelectorAll('.tab-pane').forEach(function(pane) {\n" +
            "      var content = pane.innerHTML;\n" +
            "      \n" +
            "      // Reset highlighting first\n" +
            "      pane.querySelectorAll('.search-highlight').forEach(function(el) {\n" +
            "        var text = el.textContent;\n" +
            "        el.replaceWith(text);\n" +
            "      });\n" +
            "      \n" +
            "      // Add new highlighting\n" +
            "      if (content.toLowerCase().includes(searchTerm)) {\n" +
            "        // Show this tab\n" +
            "        var tabId = pane.getAttribute('id');\n" +
            "        document.querySelector('a[href=\"#' + tabId + '\"]').click();\n" +
            "        \n" +
            "        // Highlight matches\n" +
            "        highlightText(pane, searchTerm);\n" +
            "      }\n" +
            "    });\n" +
            "  });\n" +
            "  \n" +
            "  function highlightText(element, term) {\n" +
            "    var nodes = element.childNodes;\n" +
            "    \n" +
            "    for (var i = 0; i < nodes.length; i++) {\n" +
            "      var node = nodes[i];\n" +
            "      \n" +
            "      if (node.nodeType === 3) { // Text node\n" +
            "        var text = node.nodeValue;\n" +
            "        var index = text.toLowerCase().indexOf(term);\n" +
            "        \n" +
            "        if (index >= 0) {\n" +
            "          var span = document.createElement('span');\n" +
            "          span.className = 'search-highlight';\n" +
            "          \n" +
            "          var before = document.createTextNode(text.substring(0, index));\n" +
            "          span.textContent = text.substring(index, index + term.length);\n" +
            "          var after = document.createTextNode(text.substring(index + term.length));\n" +
            "          \n" +
            "          var parent = node.parentNode;\n" +
            "          parent.insertBefore(before, node);\n" +
            "          parent.insertBefore(span, node);\n" +
            "          parent.insertBefore(after, node);\n" +
            "          parent.removeChild(node);\n" +
            "          \n" +
            "          i += 2; // Skip the nodes we just inserted\n" +
            "        }\n" +
            "      } else if (node.nodeType === 1) { // Element node\n" +
            "        // Skip script and style elements\n" +
            "        if (node.tagName !== 'SCRIPT' && node.tagName !== 'STYLE') {\n" +
            "          highlightText(node, term);\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "});"
        );
        body.appendChild(script);
        
        // Write the consolidated report to a file
        String outputPath = karateReportsDir + "/consolidated-karate-report.html";
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(consolidatedDoc.outerHtml());
        }
        
        System.out.println("Consolidated report created: " + outputPath);
    }
}
