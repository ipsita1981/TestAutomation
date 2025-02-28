package com.example.report;

import com.intuit.karate.Results;
import com.intuit.karate.core.Feature;
import com.intuit.karate.core.FeatureResult;
import com.intuit.karate.core.Scenario;
import com.intuit.karate.core.ScenarioResult;
import com.intuit.karate.core.Step;
import com.intuit.karate.core.StepResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SingleFileHtmlReporter {
    
    public static void generateReport(Results results) {
        StringBuilder html = new StringBuilder();
        
        // Start HTML document with embedded CSS and JavaScript
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("  <meta charset=\"UTF-8\">\n")
            .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("  <title>Karate Test Report</title>\n")
            .append("  <style>\n")
            .append("    body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append("    .summary { background-color: #f8f9fa; padding: 15px; margin-bottom: 20px; border-radius: 5px; }\n")
            .append("    .feature { margin-bottom: 30px; border: 1px solid #ddd; border-radius: 5px; overflow: hidden; }\n")
            .append("    .feature-header { background-color: #007bff; color: white; padding: 10px; }\n")
            .append("    .scenario { margin: 15px; border: 1px solid #eee; border-radius: 5px; }\n")
            .append("    .scenario-header { background-color: #f1f1f1; padding: 10px; display: flex; justify-content: space-between; }\n")
            .append("    .steps { padding: 10px; }\n")
            .append("    .step { margin-bottom: 5px; padding: 5px; border-left: 3px solid #ccc; }\n")
            .append("    .pass { background-color: #d4edda; border-left-color: #28a745; }\n")
            .append("    .fail { background-color: #f8d7da; border-left-color: #dc3545; }\n")
            .append("    .tags { font-size: 0.8em; color: #6c757d; }\n")
            .append("    .tag { display: inline-block; background-color: #e9ecef; padding: 2px 5px; margin-right: 5px; border-radius: 3px; }\n")
            .append("    .toggle-btn { background: none; border: none; cursor: pointer; color: #007bff; }\n")
            .append("    .hidden { display: none; }\n")
            .append("    .step-details pre { background-color: #f8f9fa; padding: 10px; border-radius: 5px; overflow: auto; }\n")
            .append("    .search-box { margin-bottom: 15px; }\n")
            .append("    .search-box input { padding: 8px; width: 300px; }\n")
            .append("    .logo { max-height: 50px; margin-right: 15px; }\n")
            .append("    .header { display: flex; align-items: center; }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n");
        
        // Embed logo as base64 (optional)
        try {
            String logoPath = "src/test/resources/logo.png"; // Path to your logo
            if (Files.exists(Paths.get(logoPath))) {
                byte[] logoBytes = Files.readAllBytes(Paths.get(logoPath));
                String base64Logo = Base64.getEncoder().encodeToString(logoBytes);
                html.append("  <div class=\"header\">\n")
                    .append("    <img src=\"data:image/png;base64,").append(base64Logo).append("\" class=\"logo\" alt=\"Logo\">\n")
                    .append("    <h1>Karate Test Report</h1>\n")
                    .append("  </div>\n");
            } else {
                html.append("  <h1>Karate Test Report</h1>\n");
            }
        } catch (IOException e) {
            html.append("  <h1>Karate Test Report</h1>\n");
        }
        
        // Summary section
        html.append("  <div class=\"summary\">\n")
            .append("    <p><strong>Total Features:</strong> ").append(results.getFeatureCount()).append("</p>\n")
            .append("    <p><strong>Scenarios:</strong> ").append(results.getScenarioCount()).append("</p>\n")
            .append("    <p><strong>Passed:</strong> ").append(results.getPassCount()).append("</p>\n")
            .append("    <p><strong>Failed:</strong> ").append(results.getFailCount()).append("</p>\n")
            .append("    <p><strong>Duration:</strong> ").append(formatDuration(results.getDuration())).append("</p>\n")
            .append("  </div>\n");
        
        // Search box
        html.append("  <div class=\"search-box\">\n")
            .append("    <input type=\"text\" id=\"searchInput\" placeholder=\"Search test cases...\" onkeyup=\"searchTests()\">\n")
            .append("  </div>\n");
        
        // Feature details
        List<FeatureResult> featureResults = results.getFeatureResults();
        for (FeatureResult featureResult : featureResults) {
            Feature feature = featureResult.getFeature();
            
            html.append("  <div class=\"feature\">\n")
                .append("    <div class=\"feature-header\">\n")
                .append("      <h2>").append(feature.getName()).append("</h2>\n");
            
            // Feature tags
            List<String> featureTags = feature.getTags();
            if (featureTags != null && !featureTags.isEmpty()) {
                html.append("      <div class=\"tags\">\n");
                for (String tag : featureTags) {
                    html.append("        <span class=\"tag\">").append(tag).append("</span>\n");
                }
                html.append("      </div>\n");
            }
            
            html.append("    </div>\n");
            
            // Scenarios
            List<ScenarioResult> scenarioResults = featureResult.getScenarioResults();
            for (ScenarioResult scenarioResult : scenarioResults) {
                Scenario scenario = scenarioResult.getScenario();
                boolean passed = !scenarioResult.isFailed();
                
                html.append("    <div class=\"scenario\">\n")
                    .append("      <div class=\"scenario-header\">\n")
                    .append("        <h3>").append(scenario.getName()).append("</h3>\n")
                    .append("        <span style=\"color: ").append(passed ? "green" : "red").append(";\">")
                    .append(passed ? "PASSED" : "FAILED").append("</span>\n")
                    .append("      </div>\n");
                
                // Scenario tags
                List<String> scenarioTags = scenario.getTags();
                if (scenarioTags != null && !scenarioTags.isEmpty()) {
                    html.append("      <div class=\"tags\">\n");
                    for (String tag : scenarioTags) {
                        html.append("        <span class=\"tag\">").append(tag).append("</span>\n");
                    }
                    html.append("      </div>\n");
                }
                
                // Steps
                html.append("      <div class=\"steps\">\n");
                List<StepResult> stepResults = scenarioResult.getStepResults();
                for (int i = 0; i < stepResults.size(); i++) {
                    StepResult stepResult = stepResults.get(i);
                    Step step = stepResult.getStep();
                    boolean stepPassed = stepResult.isPass();
                    
                    html.append("        <div class=\"step ").append(stepPassed ? "pass" : "fail").append("\">\n")
                        .append("          <p>").append(step.getPrefix()).append(" ").append(step.getText()).append("</p>\n");
                    
                    // Include request/response details for API steps if available
                    if (stepResult.getResult() != null || stepResult.getErrorMessage() != null) {
                        String stepId = "step-" + featureResult.getFeature().getRelativePath().replace("/", "-") + "-" 
                                      + scenario.getName().replace(" ", "-") + "-" + i;
                        
                        html.append("          <button class=\"toggle-btn\" onclick=\"toggleDetails('").append(stepId).append("')\">Show/Hide Details</button>\n")
                            .append("          <div id=\"").append(stepId).append("\" class=\"step-details hidden\">\n");
                        
                        if (stepResult.getResult() != null) {
                            html.append("            <h4>Result:</h4>\n")
                                .append("            <pre>").append(escapeHtml(stepResult.getResult().toString())).append("</pre>\n");
                        }
                        
                        if (stepResult.getErrorMessage() != null) {
                            html.append("            <h4>Error:</h4>\n")
                                .append("            <pre>").append(escapeHtml(stepResult.getErrorMessage())).append("</pre>\n");
                        }
                        
                        html.append("          </div>\n");
                    }
                    
                    html.append("        </div>\n");
                }
                html.append("      </div>\n")
                    .append("    </div>\n");
            }
            
            html.append("  </div>\n");
        }
        
        // Add JavaScript functions
        html.append("<script>\n")
            .append("function toggleDetails(id) {\n")
            .append("  var element = document.getElementById(id);\n")
            .append("  if (element.classList.contains('hidden')) {\n")
            .append("    element.classList.remove('hidden');\n")
            .append("  } else {\n")
            .append("    element.classList.add('hidden');\n")
            .append("  }\n")
            .append("}\n\n")
            .append("function searchTests() {\n")
            .append("  var input, filter, scenarios, i, txtValue;\n")
            .append("  input = document.getElementById('searchInput');\n")
            .append("  filter = input.value.toUpperCase();\n")
            .append("  scenarios = document.getElementsByClassName('scenario');\n")
            .append("  for (i = 0; i < scenarios.length; i++) {\n")
            .append("    txtValue = scenarios[i].textContent || scenarios[i].innerText;\n")
            .append("    if (txtValue.toUpperCase().indexOf(filter) > -1) {\n")
            .append("      scenarios[i].style.display = '';\n")
            .append("      scenarios[i].parentElement.style.display = '';\n")
            .append("    } else {\n")
            .append("      scenarios[i].style.display = 'none';\n")
            .append("    }\n")
            .append("  }\n")
            .append("  // Hide features with no visible scenarios\n")
            .append("  var features = document.getElementsByClassName('feature');\n")
            .append("  for (i = 0; i < features.length; i++) {\n")
            .append("    var visibleScenarios = features[i].querySelectorAll('.scenario:not([style*=\"display: none\"])');\n")
            .append("    if (visibleScenarios.length === 0) {\n")
            .append("      features[i].style.display = 'none';\n")
            .append("    } else {\n")
            .append("      features[i].style.display = '';\n")
            .append("    }\n")
            .append("  }\n")
            .append("}\n")
            .append("</script>\n");
        
        html.append("</body>\n")
            .append("</html>");
        
        // Write the HTML to a file
        try {
            File reportDir = new File("target/single-file-report");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }
            FileWriter writer = new FileWriter(new File(reportDir, "karate-report.html"));
            writer.write(html.toString());
            writer.close();
            System.out.println("Single file HTML report generated at: " + reportDir.getAbsolutePath() + "/karate-report.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static String formatDuration(long nanos) {
        long millis = nanos / 1000000;
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        return String.format("%02d:%02d:%02d.%03d", 
                hours, minutes % 60, seconds % 60, millis % 1000);
    }
    
    private static String escapeHtml(String html) {
        return html.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
