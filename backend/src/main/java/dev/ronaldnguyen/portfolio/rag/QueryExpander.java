package dev.ronaldnguyen.portfolio.rag;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adds related terms to a query so that, for example, "Does he know .NET?"
 * still finds the C# chunks. Expanded terms get a lower weight than
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
        RAW.put("hire", "contact email linkedin");
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

        // German questions against the English CV: map common words to its vocabulary.
        RAW.put("programmiersprache", "programming languages");
        RAW.put("programmiersprachen", "programming languages");
        RAW.put("programmieren", "programming languages developer");
        RAW.put("sprachen", "languages german english programming");
        RAW.put("kenntnisse", "skills programming frameworks tools");
        RAW.put("fähigkeiten", "skills programming frameworks tools");
        RAW.put("technologien", "frameworks programming languages tools");
        RAW.put("werkzeuge", "tools frameworks");
        RAW.put("praktikum", "internship developer");
        RAW.put("werkstudent", "working student developer");
        RAW.put("arbeit", "work working experience developer job");
        RAW.put("arbeitet", "working present developer");
        RAW.put("gearbeitet", "worked working experience developer");
        RAW.put("beruf", "work developer job experience");
        RAW.put("firma", "gmbh company working developer");
        RAW.put("unternehmen", "gmbh company working developer");
        RAW.put("aktuell", "present working student currently");
        RAW.put("derzeit", "present working student currently");
        RAW.put("studium", "university studying degree master bachelor education");
        RAW.put("studiert", "university studying degree master bachelor");
        RAW.put("universität", "university education degree");
        RAW.put("uni", "university education degree");
        RAW.put("ausbildung", "education university degree abitur");
        RAW.put("abschluss", "degree bachelor master grade");
        RAW.put("projekte", "github repository projects");
        RAW.put("projekt", "github repository project");
        RAW.put("kontakt", "contact email linkedin github");
        RAW.put("kontaktieren", "contact email linkedin");
        RAW.put("erreichen", "contact email linkedin");
        RAW.put("wohnt", "location based jena germany");
        RAW.put("wohnort", "location based jena germany");
        RAW.put("sprechen", "german english native fluent languages");
        RAW.put("spricht", "german english native fluent languages");
        RAW.put("datenbank", "database postgresql sql server");
        RAW.put("datenbanken", "database postgresql sql server");
        RAW.put("lehre", "teaching assistant tutor");
        RAW.put("tutor", "teaching assistant");
        RAW.put("künstliche", "ai llm machine learning");
        RAW.put("intelligenz", "ai llm machine learning");
        RAW.put("thema", "thesis title topic");
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
