package es.ulpgc.dacd.business.util;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class TechnologyNormalizer {

    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Set<String> ALLOWED_TECHS = new HashSet<>();

    static {
        ALIASES.put("js", "javascript");
        ALIASES.put("node.js", "javascript");
        ALIASES.put("nodejs", "javascript");
        ALIASES.put("es6", "javascript");
        ALIASES.put("vue.js", "vue");
        ALIASES.put("react.js", "react");
        ALIASES.put("reactjs", "react");
        ALIASES.put("angular.js", "angular");
        ALIASES.put("angularjs", "angular");

        ALIASES.put("py", "python");
        ALIASES.put("python-3.x", "python");
        ALIASES.put("python3", "python");

        ALIASES.put("c#", "csharp");
        ALIASES.put("c-sharp", "csharp");
        ALIASES.put("c++", "cpp");
        ALIASES.put("cplusplus", "cpp");

        ALIASES.put("ts", "typescript");
        ALIASES.put("golang", "go");

        ALIASES.put("k8s", "kubernetes");
        ALIASES.put("docker", "docker");
        ALIASES.put("amazon-web-services", "aws");
        ALIASES.put("google-cloud", "gcp");
        ALIASES.put("microsoft-azure", "azure");

        ALLOWED_TECHS.addAll(Set.of(
                "javascript", "python", "java", "csharp", "cpp", "php",
                "typescript", "ruby", "c", "swift", "go", "rust", "kotlin",
                "html", "css", "sql", "dart", "scala", "r", "lua",
                "angular", "react", "vue", "docker", "kubernetes", "aws",
                "azure", "gcp", "mongodb", "postgresql", "mysql"
        ));
    }

    public static String normalize(String tech) {
        if (tech == null) return "unknown";
        String clean = tech.trim().toLowerCase();

        if (ALIASES.containsKey(clean)) {
            clean = ALIASES.get(clean);
        }

        return clean;
    }
    public static boolean isMonitored(String normalizedTech) {
        return ALLOWED_TECHS.contains(normalizedTech);
    }
}
