package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * production Java ソースに対する規約検査（CNF-001 / 002 / 003 / 004 / 006 / 007）。
 *
 * <p>ファイルシステムを知らない。呼び出し側が {@link SourceFile} を与える。
 */
public final class JavaSourceRules {

    /** 常に禁止する型名の語尾。文脈次第で妥当な語（Processor 等）は機械では拒否しない。 */
    private static final Set<String> FORBIDDEN_TYPE_SUFFIXES =
            Set.of("Manager", "Helper", "Util", "Utils", "Common");

    /** 常に禁止するパッケージ名の構成要素。 */
    private static final Set<String> FORBIDDEN_PACKAGE_SEGMENTS =
            Set.of("utils", "helpers", "managers", "misc", "common");

    private static final Pattern PACKAGE_DECLARATION = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");

    private static final Pattern TYPE_DECLARATION =
            Pattern.compile("\\b(?:class|interface|enum|record|@interface)\\s+(\\w+)");

    private static final Pattern METHOD_DECLARATION =
            Pattern.compile("^\\s*(?:(?:public|protected|private|static|final|abstract|synchronized|native|default|strictfp)\\s+)*"
                    + "[\\w$<>\\[\\],.?\\s]+?\\s+(\\w+)\\s*\\(");

    private static final Set<String> NOT_METHOD_NAMES =
            Set.of("if", "for", "while", "switch", "catch", "return", "synchronized", "new", "try", "do", "else");

    private static final Pattern SUPPRESSION = Pattern.compile("@SuppressWarnings");

    private static final Pattern WAIVER_REFERENCE = Pattern.compile("//\\s*Waiver:\\s*(WVR-\\d{4})");

    private static final Pattern TASK_MARKER = Pattern.compile("\\b(TODO|FIXME)\\b");

    private static final Pattern ISSUE_REFERENCE = Pattern.compile("\\(#\\d+\\)");

    /** UI 状態の反映を許すメソッド名の接頭辞。 */
    private static final String RENDER_METHOD_PREFIX = "render";

    /** 反映経路を 1 本に固定したい UI 呼び出し。 */
    private static final List<String> RENDER_ONLY_CALLS = List.of("setEnabled(", "setAlwaysOnTop(");

    /** 直接組み立ててはいけないテキスト部品。作る場所を 1 つに固定する（CNF-012 / SWG-006）。 */
    private static final List<String> TEXT_COMPONENTS = List.of("new JLabel(", "new JTextField(");

    /** テキスト部品を組み立ててよい唯一のファイル。 */
    private static final String TEXT_RENDERING_FILE = "TextRendering.java";

    /** 画面に出す文言を書いてよい唯一のファイル。 */
    private static final String UI_TEXT_FILE = "UiText.java";

    /** 開発者へ向けた文（例外の説明）だけは、文言の検査から外す。 */
    private static final String THROW_STATEMENT = "throw ";

    private JavaSourceRules() {}

    public static Result check(SourceFile file) {
        List<Violation> violations = new ArrayList<>();
        List<String> referencedWaivers = new ArrayList<>();

        boolean uiSource = file.path().startsWith("ui/");
        boolean block = false;
        int depth = 0;
        int topLevelTypes = 0;
        String primaryType = null;
        String previousLine = "";
        Deque<Method> methods = new ArrayDeque<>();

        for (int index = 0; index < file.lines().size(); index++) {
            int lineNumber = index + 1;
            String raw = file.lines().get(index);
            CodeText text = CodeText.scan(raw, block);
            block = text.inBlockComment();
            String code = text.code();

            checkPackage(file, violations, lineNumber, code);
            checkTaskMarker(file, violations, lineNumber, text.comment());
            checkSuppression(file, violations, referencedWaivers, new Suppression(lineNumber, code, raw, previousLine));

            if (code.contains("default:") || code.contains("default ->")) {
                violations.add(new Violation(
                        "CNF-003",
                        file.path(),
                        lineNumber,
                        "switch に default を書かない。網羅性検査を無効化する"));
            }

            Matcher typeMatcher = TYPE_DECLARATION.matcher(code);
            while (typeMatcher.find()) {
                String typeName = typeMatcher.group(1);
                if (depth == 0) {
                    topLevelTypes++;
                    if (primaryType == null) {
                        primaryType = typeName;
                    }
                }
                checkTypeName(file, violations, lineNumber, typeName);
            }

            String declaredMethod = methodNameOf(code);
            int depthBeforeLine = depth;
            depth += countOf(code, '{') - countOf(code, '}');
            if (declaredMethod != null && code.contains("{")) {
                methods.push(new Method(declaredMethod, depthBeforeLine));
            }
            while (!methods.isEmpty() && depth <= methods.peek().openedAtDepth()) {
                methods.pop();
            }

            if (uiSource) {
                checkRenderOnlyCall(file, violations, lineNumber, code, methods.peek());
                checkTextComponent(file, violations, lineNumber, code);
                checkDisplayText(file, violations, new Literal(lineNumber, code, text.literals()));
            }
            // 🔴 直前「行」であってコード行ではない。waiver は `// Waiver: WVR-NNNN` という
            //    コメント行なので、コード行だけを覚えると永久に見つからない（Issue #26）。
            if (!raw.isBlank()) {
                previousLine = raw;
            }
        }

        if (topLevelTypes > 1) {
            violations.add(Violation.atFile(
                    "CNF-007", file.path(), "トップレベル型が " + topLevelTypes + " 個ある。1 ファイル 1 主要宣言"));
        }
        if (primaryType != null && !file.fileName().equals(primaryType + ".java")) {
            violations.add(Violation.atFile(
                    "CNF-007", file.path(), "ファイル名が主要型 " + primaryType + " と一致しない"));
        }
        return new Result(List.copyOf(violations), List.copyOf(referencedWaivers));
    }

    private static void checkPackage(SourceFile file, List<Violation> violations, int lineNumber, String code) {
        Matcher matcher = PACKAGE_DECLARATION.matcher(code);
        if (!matcher.find()) {
            return;
        }
        for (String segment : matcher.group(1).split("\\.")) {
            if (FORBIDDEN_PACKAGE_SEGMENTS.contains(segment)) {
                violations.add(new Violation(
                        "CNF-001", file.path(), lineNumber, "禁止されたパッケージ名の構成要素: " + segment));
            }
        }
    }

    private static void checkTypeName(SourceFile file, List<Violation> violations, int lineNumber, String typeName) {
        for (String suffix : FORBIDDEN_TYPE_SUFFIXES) {
            if (typeName.equals(suffix) || typeName.endsWith(suffix)) {
                violations.add(new Violation(
                        "CNF-001", file.path(), lineNumber, "禁止された総称型名: " + typeName + "（役割を名前で語る）"));
                return;
            }
        }
    }

    private static void checkTaskMarker(SourceFile file, List<Violation> violations, int lineNumber, String comment) {
        if (TASK_MARKER.matcher(comment).find() && !ISSUE_REFERENCE.matcher(comment).find()) {
            violations.add(new Violation(
                    "CNF-006", file.path(), lineNumber, "TODO / FIXME には Issue 番号 (#N) を書く"));
        }
    }

    private static void checkSuppression(
            SourceFile file, List<Violation> violations, List<String> referencedWaivers, Suppression suppression) {
        if (!SUPPRESSION.matcher(suppression.code()).find()) {
            return;
        }
        // 🔴 抑制の中身は raw 行で見る。CodeText は文字列リテラルの中身を空白に潰すので、
        //    code 側を見ると @SuppressWarnings("all") の "all" が消えて検出できない（Issue #26）。
        if (suppression.raw().contains("\"all\"")) {
            violations.add(new Violation(
                    "CNF-002",
                    file.path(),
                    suppression.lineNumber(),
                    "@SuppressWarnings(\"all\") は waiver でも許可されない"));
            return;
        }
        Matcher waiver = WAIVER_REFERENCE.matcher(suppression.previousLine());
        if (!waiver.find()) {
            violations.add(new Violation(
                    "CNF-002",
                    file.path(),
                    suppression.lineNumber(),
                    "@SuppressWarnings の直前行に // Waiver: WVR-NNNN が必要"));
            return;
        }
        referencedWaivers.add(waiver.group(1));
    }

    /** 抑制 1 か所を見るために要る文脈。 */
    private record Suppression(int lineNumber, String code, String raw, String previousLine) {}

    /**
     * テキスト部品を直接組み立てていないか（CNF-012）。
     *
     * <p>Swing の文字描画ヒントは環境から渡されるもので、渡されない環境ではアンチエイリアスが
     * 効かない。部品ごとにヒントを付けて回ると必ず付け忘れるので、作る場所を 1 つに固定する。
     */
    private static void checkTextComponent(SourceFile file, List<Violation> violations, int lineNumber, String code) {
        if (file.fileName().equals(TEXT_RENDERING_FILE)) {
            return;
        }
        for (String construction : TEXT_COMPONENTS) {
            if (code.contains(construction)) {
                violations.add(new Violation(
                        "CNF-012",
                        file.path(),
                        lineNumber,
                        construction + " を直接書かない。TextRendering を通す（SWG-006）"));
            }
        }
    }

    /**
     * 画面に出す文言をリテラルで書いていないか（CNF-013）。
     *
     * <p>文言は {@code UiText} に集める。言語ごとにファイルを分けず 1 定数が両方を持つので、
     * そこを通れば片方の言語を忘れられない。**リテラルで直接書くと、その仕組みを迂回できる。**
     *
     * <p>🔴 実際に迂回した。言語の選択肢だけを {@code List.of("日本語", "English")} と書いていて、
     * 英語 UI で豆腐（□□□）になった。文言の検査は緑のままだった（Issue #42）。
     *
     * <p>例外の説明文だけは外す。あれは利用者ではなく、開発者に向けた文である。
     */
    private static void checkDisplayText(SourceFile file, List<Violation> violations, Literal literal) {
        if (file.fileName().equals(UI_TEXT_FILE) || literal.code().contains(THROW_STATEMENT)) {
            return;
        }
        if (containsJapanese(literal.contents())) {
            violations.add(new Violation(
                    "CNF-013",
                    file.path(),
                    literal.lineNumber(),
                    "画面に出す文言をリテラルで書かない。UiText を通す（FR-048）"));
        }
    }

    /** 1 行ぶんのリテラルと、その行のコード。 */
    private record Literal(int lineNumber, String code, String contents) {}

    private static boolean containsJapanese(String text) {
        for (int index = 0; index < text.length(); index++) {
            Character.UnicodeBlock block = Character.UnicodeBlock.of(text.charAt(index));
            if (Character.UnicodeBlock.HIRAGANA.equals(block)
                    || Character.UnicodeBlock.KATAKANA.equals(block)
                    || Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)
                    || Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(block)) {
                return true;
            }
        }
        return false;
    }

    private static void checkRenderOnlyCall(
            SourceFile file, List<Violation> violations, int lineNumber, String code, Method enclosing) {
        for (String call : RENDER_ONLY_CALLS) {
            if (!code.contains(call)) {
                continue;
            }
            if (enclosing != null && enclosing.name().startsWith(RENDER_METHOD_PREFIX)) {
                continue;
            }
            String where = enclosing == null ? "メソッド外" : enclosing.name();
            violations.add(new Violation(
                    "CNF-004",
                    file.path(),
                    lineNumber,
                    call + " を " + where + " で呼んでいる。UI 状態の反映は render* からのみ"));
        }
    }

    private static String methodNameOf(String code) {
        Matcher matcher = METHOD_DECLARATION.matcher(code);
        if (!matcher.find()) {
            return null;
        }
        String name = matcher.group(1);
        if (NOT_METHOD_NAMES.contains(name)) {
            return null;
        }
        return name;
    }

    private static int countOf(String text, char target) {
        int count = 0;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == target) {
                count++;
            }
        }
        return count;
    }

    private record Method(String name, int openedAtDepth) {}

    /** 1 ファイルの検査結果。参照された waiver ID は台帳検査（CNF-009）へ渡す。 */
    public record Result(List<Violation> violations, List<String> referencedWaivers) {}
}
