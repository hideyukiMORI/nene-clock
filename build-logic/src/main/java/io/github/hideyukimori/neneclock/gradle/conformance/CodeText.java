package io.github.hideyukimori.neneclock.gradle.conformance;

/**
 * Java ソースの 1 行から「コードとして意味のある部分」だけを取り出す。
 *
 * <p>文字列リテラル・文字リテラル・コメントの中身を空白に潰すことで、後段の規則が
 * リテラル中の単語に反応する誤検知を防ぐ。完全な字句解析ではなく、本リポジトリの
 * 整形済みソースに対して十分な近似である（判断の根拠は ADR 0001）。
 */
public final class CodeText {

    private final String code;
    private final String comment;
    private final boolean inBlockComment;

    private CodeText(String code, String comment, boolean inBlockComment) {
        this.code = code;
        this.comment = comment;
        this.inBlockComment = inBlockComment;
    }

    public String code() {
        return code;
    }

    /** コメント部分（`//` 以降とブロックコメント本文）。TODO 検査などが使う。 */
    public String comment() {
        return comment;
    }

    public boolean inBlockComment() {
        return inBlockComment;
    }

    public static CodeText scan(String line, boolean startsInBlockComment) {
        StringBuilder codeOut = new StringBuilder(line.length());
        StringBuilder commentOut = new StringBuilder();
        boolean block = startsInBlockComment;
        boolean inString = false;
        boolean inChar = false;
        int index = 0;
        while (index < line.length()) {
            char current = line.charAt(index);
            char next = index + 1 < line.length() ? line.charAt(index + 1) : '\0';
            if (block) {
                if (current == '*' && next == '/') {
                    block = false;
                    index += 2;
                } else {
                    commentOut.append(current);
                    index++;
                }
                continue;
            }
            if (inString || inChar) {
                if (current == '\\') {
                    index += 2;
                    continue;
                }
                if ((inString && current == '"') || (inChar && current == '\'')) {
                    inString = false;
                    inChar = false;
                    codeOut.append(current);
                    index++;
                    continue;
                }
                codeOut.append(' ');
                index++;
                continue;
            }
            if (current == '/' && next == '/') {
                commentOut.append(line.substring(index + 2));
                break;
            }
            if (current == '/' && next == '*') {
                block = true;
                index += 2;
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == '\'') {
                inChar = true;
            }
            codeOut.append(current);
            index++;
        }
        return new CodeText(codeOut.toString(), commentOut.toString(), block);
    }
}
