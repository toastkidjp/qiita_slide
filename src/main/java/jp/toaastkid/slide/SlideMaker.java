package jp.toaastkid.slide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.Sets;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Qiita article to reveal's Slide.
 * @author Toast kid
 *
 */
public class SlideMaker {

    /** line separator. */
    private static final String LINE_SEPARATOR = System.lineSeparator();

    /** replace regex. */
    private static final Pattern PARAM_TEMPLATE_PATTERN
        = Pattern.compile("\\$\\{(.+?)\\}", Pattern.DOTALL);

    /** reveal's theme. */
    private static final ImmutableSet<String> THEMES = Sets.immutable.of(
            "black", "league", "night", "simple", "solarized",
            "beige", "blood",  "moon", "serif", "sky", "white");

    /**
     * main methohd.
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {

        if (args.length < 1) {
            System.out.println("You should pass two arguments.");
            System.out.println("args[0]……Qiita Article's URL");
            System.out.println("args[1]……Theme name");
            System.out.println("You can use theme ");
            System.out.println(THEMES);
            return;
        }

        final String url = args[0];
        final Optional<String> optional = fetch(url);
        if (!optional.isPresent()) {
            return;
        }
        final String[] fetch = optional.get().split("\n");

        final Map<String, String> params = Maps.fixedSize.of(
                "title",   "slide",
                "content", convertArticle2Slide(fetch).get(),
                "theme",   args.length < 2 ? "black" : findTheme(args[1])
                );

        final Path write = Files.write(
                Paths.get("slide.html"),
                bindArgs("slide.html", params).getBytes(StandardCharsets.UTF_8)
                );
        System.out.println("Generate slide html to " + write.toString());
    }

    /**
     * find reveal's theme.
     * @param theme theme name
     * @return theme name
     */
    private static String findTheme(final String theme) {
        if (theme == null || theme.isEmpty()) {
            return "black";
        }
        final String lowerCase = theme.toLowerCase();
        return THEMES.contains(lowerCase) ? lowerCase : "black";
    }

    /**
     * convert article.
     * @param contents
     * @return
     */
    public static Optional<String> convertArticle2Slide(final String[] contents) {
        final MutableList<String> converted = Lists.mutable.empty();
        final StringBuilder sb = new StringBuilder();

        boolean isContent = false;
        for (final String line : contents) {
            if (line.contains("articleBody")) {
                isContent = true;
                continue;
            }
            if (line.contains("<div class=\"hidden\">")) {
                break;
            }
            if (!isContent) {
                continue;
            }
            if ((line.contains("<hr>") || line.matches("^<h[1-6r]>.*")) && sb.length() != 0) {
                converted.add(String.format("<section>%s</section>", sb.toString()));
                sb.setLength(0);
            }
            final String trimmed = line.startsWith("<") ? line.replace("<hr>", "") : line;
            if (trimmed.length() != 0) {
                sb.append(trimmed.replaceFirst("<span id=.* class=\"fragment\"></span>", ""))
                  .append(LINE_SEPARATOR);
            }
        }
        if (sb.length() != 0) {
            converted.add(String.format("<section>%s</section>", sb.toString()));
        }
        return Optional.of(converted.makeString(LINE_SEPARATOR));
    }

    /**
     * fetch qiita article.
     * @param url
     * @return
     */
    private static Optional<String> fetch(final String url) {
        final OkHttpClient client = new OkHttpClient();
        final Request req = new Request.Builder().url(url).build();
        try {
            return Optional.of(client.newCall(req).execute().body().string());
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * read resource with parameter.
     * @param pathToTemplate
     * @param params
     * @return
     * @throws IOException
     */
    private static final String bindArgs(
            final String pathToTemplate,
            final Map<String, String> params
            ) throws IOException {
        final String lineSeparator = System.lineSeparator();
        final List<String> templates = readLinesFromStream(pathToTemplate);
        final StringBuilder convertedText = new StringBuilder();
        templates.forEach(template -> {
            if (template.contains("${")) {
                Matcher matcher = PARAM_TEMPLATE_PATTERN.matcher(template);
                while (matcher.find()) {
                    final String key = matcher.group(1);
                    if (!params.containsKey(key)) {
                        continue;
                    }
                    String value = params.get(key);
                    if (value == null) {
                        continue;
                    }
                    if (value.contains("$") && !value.contains("\\$")) {
                        value = value.replace("$", "\\$");
                    }
                    //System.out.println("value = " + value);
                    template = matcher.replaceFirst(value);
                    matcher = PARAM_TEMPLATE_PATTERN.matcher(template);
                }
            }
            convertedText.append(template).append(lineSeparator);
        });
        return convertedText.toString();
    }

    /**
     * read resource's content from stream.
     * @param filePath
     * @return
     */
    public static List<String> readLinesFromStream(final String filePath) {
        final List<String> resSet = new ArrayList<String>(100);
        final InputStream in = SlideMaker.class.getClassLoader().getResourceAsStream(filePath);
        try (final BufferedReader fileReader
                = new BufferedReader(new InputStreamReader(in , StandardCharsets.UTF_8));) {
            String str = fileReader.readLine();
            while (str != null) {
                resSet.add(str);
                str = fileReader.readLine();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return resSet;
    }

}
