package dev.ronaldnguyen.portfolio.rag;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds related terms to a query so that, for example, "When can he start?"
 * still finds the availability chunk. Expanded terms get a lower weight than
 * the words the visitor actually typed.
 */
public final class QueryExpander {

    static final double EXPANSION_WEIGHT = 0.4;

    private static final Map<String, String> RAW = new LinkedHashMap<>();

    static {
        RAW.put("thesis", "bachelorarbeit masterarbeit thesis");
        RAW.put("bachelorarbeit", "bachelor thesis");
        RAW.put("masterarbeit", "master thesis");
        RAW.put("abschlussarbeit", "thesis bachelor master");
        RAW.put("available", "availability opportunity starting april 2027");
        RAW.put("availability", "available opportunity starting april 2027");
        RAW.put("start", "starting availability april 2027");
        RAW.put("starting", "start availability april 2027");
        RAW.put("verfügbar", "availability opportunity starting april 2027");
        RAW.put("hire", "availability opportunity contact");
        RAW.put("fullstack", "frontend backend web application angular aspnet");
        RAW.put("frontend", "angular react tailwind ngrx typescript");
        RAW.put("backend", "aspnet jakarta java sql server");
        RAW.put("web", "angular react aspnet fullstack");
        RAW.put("csharp", "dotnet aspnet wpf");
        RAW.put("dotnet", "csharp aspnet wpf");
        RAW.put("experience", "work working developer internship role");
        RAW.put("erfahrung", "experience work developer");
        RAW.put("work", "working experience developer job");
        RAW.put("job", "working student developer internship");
        RAW.put("company", "gmbh working developer");
        RAW.put("current", "present working student");
        RAW.put("currently", "present working student studying");
        RAW.put("ai", "llm machine learning rag prompt engineering annotation");
        RAW.put("ki", "ai llm machine learning");
        RAW.put("llm", "llm ai rag prompt engineering");
        RAW.put("ml", "machine learning ai");
        RAW.put("education", "university degree bachelor master abitur");
        RAW.put("study", "university studying degree master bachelor");
        RAW.put("studied", "university degree bachelor");
        RAW.put("studying", "university master degree");
        RAW.put("university", "education degree bachelor master");
        RAW.put("degree", "bachelor master education university");
        RAW.put("grade", "gpa grade final");
        RAW.put("gpa", "grade");
        RAW.put("note", "grade");
        RAW.put("language", "programming languages german english");
        RAW.put("speak", "german english native fluent");
        RAW.put("sprache", "german english language");
        RAW.put("skill", "skills programming frameworks tools");
        RAW.put("tech", "programming languages frameworks tools");
        RAW.put("stack", "frameworks programming languages tools");
        RAW.put("project", "github repository projects");
        RAW.put("repo", "github repository");
        RAW.put("repository", "github project");
        RAW.put("contact", "email linkedin github reach");
        RAW.put("email", "contact mail");
        RAW.put("reach", "contact email linkedin");
        RAW.put("live", "location based jena germany");
        RAW.put("location", "based jena germany");
        RAW.put("devops", "docker kubernetes azure git");
        RAW.put("database", "postgresql sql server supabase firebase");
        RAW.put("mobile", "jetpack compose kotlin multiplatform");
        RAW.put("android", "jetpack compose kotlin");
        RAW.put("semiconductor", "wafers optical inspection confovis");
        RAW.put("teaching", "teaching assistant imperative programming");
    }

    private static final Map<String, List<String>> EXPANSIONS = new HashMap<>();

    static {
        RAW.forEach((key, value) -> {
            List<String> keyTokens = Tokenizer.tokenize(key);
            if (!keyTokens.isEmpty()) {
                EXPANSIONS.put(keyTokens.get(0), Tokenizer.tokenize(value));
            }
        });
    }

    private QueryExpander() {
    }

    /** Weighted query features: typed words (count) plus expansions (low weight). */
    public static Map<String, Double> expand(List<String> tokens) {
        Map<String, Double> features = LocalEmbedder.features(tokens);
        for (String token : tokens) {
            for (String extra : EXPANSIONS.getOrDefault(token, List.of())) {
                features.merge(extra, EXPANSION_WEIGHT, Math::max);
            }
        }
        return features;
    }
}
